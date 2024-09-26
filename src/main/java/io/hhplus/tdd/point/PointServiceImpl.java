package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final long MAX_COUNT = 10_000L;

    // 세그먼트 단위로 락을 걸어줄 수 있음
    //
    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @Override
    public UserPoint getUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @Override
    public List<PointHistory> getPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @Override
    public UserPoint chargePoint(long userId, long amount) {
        // userId 를 키로 하는 락을 가져오고, 해당 키에 대한 락이 맵에 없으면 새로운 ReentrantLock 을 생성 -> 반환
        // ReentrantLock => 명시적인 락 확득/해제 제공 , 재진입이 가능하다는 특징 !!
        Lock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        try {

            System.out.println("충전 시작 : 유저 ID = " + userId + ", 액수 = " + amount + ", 현재 스레드 = " + Thread.currentThread().getName());

            UserPoint currentPoint = userPointTable.selectById(userId);

            // 최대 잔고 검증
            if (currentPoint.point() + amount > MAX_COUNT) {
                throw new IllegalArgumentException("최대 잔고를 초과할 수 없습니다.");
            }

            UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, currentPoint.point() + amount);
            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

            System.out.println("충전 종료 : 유저 ID = " + userId + ", 충전 후 = " + updateUserPoint.point() + ", 현재 스레드 = " + Thread.currentThread().getName());

            return updateUserPoint;
        } finally {
            lock.unlock();
        }
    }


    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @Override
    public UserPoint usePoint(long userId, long amount) {

        UserPoint currentPoint = userPointTable.selectById(userId);

        // 잔고 부족 검증
        if (currentPoint.point() < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        Long finalPoint = currentPoint.point() - amount;
        UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, finalPoint);
        pointHistoryTable.insert(userId, finalPoint, TransactionType.USE, System.currentTimeMillis());

        return updateUserPoint;
    }

}
