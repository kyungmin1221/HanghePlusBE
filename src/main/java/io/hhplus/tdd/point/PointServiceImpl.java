package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

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
        UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, amount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE,  System.currentTimeMillis());

        return updateUserPoint;
    }


    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @Override
    public UserPoint usePoint(long userId, long amount) {
        UserPoint currentPoint = userPointTable.selectById(userId);
        if (currentPoint.point() < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        Long finalPoint = currentPoint.point() - amount;
        UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, finalPoint);
        pointHistoryTable.insert(userId, finalPoint, TransactionType.USE, System.currentTimeMillis());
        return updateUserPoint;
    }

}
