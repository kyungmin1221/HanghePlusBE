package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("동시성 테스트")
public class PointServiceConcurrencyTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointServiceImpl pointService;

    @Test
    @DisplayName("동시성 테스트: 동시에 여러 포인트 충전 요청 처리")
    void testConcurrentPointCharge() throws InterruptedException {
        long userId = 1L;
        long initialBalance = 1000L;
        int numberOfThreads = 10;
        long chargeAmount = 100L;

        // 유저의 현재 포인트 잔액 저장
        // AtomicLong => thread-safe 하게 값을 변경할 수 있는 객체
        AtomicLong currentBalance = new AtomicLong(initialBalance);

        when(userPointTable.selectById(userId))
                .thenAnswer(invocation -> {

            return new UserPoint(userId, currentBalance.get(), System.currentTimeMillis());
        });

        when(userPointTable.insertOrUpdate(eq(userId), anyLong()))
                .thenAnswer(invocation -> {
            Long newAmount = invocation.getArgument(1);
            currentBalance.set(newAmount);

            return new UserPoint(userId, newAmount, System.currentTimeMillis());
        });

        // ThreadPool 을 관리해주는 인터페이스 , 고정된 크기의 스레드풀을 생성하여 동시에 여러 작업이 가능하도록 설정
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        // 다수의 스레드가 모두 작업을 완료할 때까지 기다릴 수 있는 동기화 도구
        // latch : 스레드들이 작업을 완료한 후 실행을 계속해서 할 수 있도록 제어하는 역할을 해준다.
        // numberOfThreads 값을 받아 스레드 수 만큼 카운트다운 수행
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    // 동시성 제어 확인
                    pointService.chargePoint(userId, chargeAmount);
                } finally {
                    // 스레드가 작업을 완료하면 latch 의 카운트를 다운시킴.
                    // 모든 스레드가 작업을 완료하기 전까지  latch.await() 는 대기상태
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 완료할 때 까지 대기
        // latch 가 0 이되면 모든 스레드가 작업을 마쳤다고 간주
        latch.await();
        // 스레드풀 종료
        executor.shutdown();


        long expectedFinalBalance = initialBalance + (chargeAmount * numberOfThreads);
        UserPoint finalUserPoint = pointService.chargePoint(userId, 0);
        assertEquals(expectedFinalBalance, finalUserPoint.point());

        verify(pointHistoryTable, times(numberOfThreads)).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }
}
