package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest
public class PointServiceConcurrencyIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @BeforeEach
    void setUp() {
        userPointTable.insertOrUpdate(1L, 1000L);
    }

    @Test
    @DisplayName("동시성 통합 테스트: 여러 포인트 충전 동시 요청 처리")
    void testConcurrentPointCharge() throws InterruptedException {
        long userId = 1L;
        int numberOfThreads = 10;
        long chargeAmount = 100L;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

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
        
        UserPoint finalUserPoint = userPointTable.selectById(userId);
        long expectedFinalBalance = 1000L + (chargeAmount * numberOfThreads);
        assertEquals(expectedFinalBalance, finalUserPoint.point(), "최종 잔액이 일치하지 않습니다.");

        System.out.println("최종 포인트 잔액: " + finalUserPoint.point());
    }
}