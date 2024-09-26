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

        // 현재 유저 포인트 상태를 유지할 변수
        AtomicLong currentBalance = new AtomicLong(initialBalance);

        // selectById에 대한 Mock 설정
        when(userPointTable.selectById(userId))
                .thenAnswer(invocation -> {

            return new UserPoint(userId, currentBalance.get(), System.currentTimeMillis());
        });

        // insertOrUpdate에 대한 Mock 설정
        when(userPointTable.insertOrUpdate(eq(userId), anyLong()))
                .thenAnswer(invocation -> {
            Long newAmount = invocation.getArgument(1);
            currentBalance.set(newAmount);

            return new UserPoint(userId, newAmount, System.currentTimeMillis());
        });

        // ExecutorService 및 CountDownLatch 설정
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 동시에 여러 스레드에서 포인트 충전 요청 실행
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    pointService.chargePoint(userId, chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 최종 포인트 검증
        long expectedFinalBalance = initialBalance + (chargeAmount * numberOfThreads);
        UserPoint finalUserPoint = pointService.chargePoint(userId, 0); // 현재 포인트를 조회
        assertEquals(expectedFinalBalance, finalUserPoint.point());

        // 포인트 기록이 정확히 남았는지 확인
        verify(pointHistoryTable, times(numberOfThreads)).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }
}
