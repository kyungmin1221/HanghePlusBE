package io.hhplus.tdd.point;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.apache.catalina.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointServiceImpl pointService;


    /**
     * 특정 유저의 포인트 조회가 잘 되었는지를 검증하는 메소드
     */
    @Test
    @DisplayName("특정 유저의 포인트 조회 로직 확인")
    public void ValidUserTest() {

        long userId = 1L;

        // given
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 500, System.currentTimeMillis()));

        // when
        UserPoint userPoint = pointService.getUserPoint(userId);

        // then
        assertEquals(500, userPoint.point());
        verify(userPointTable).selectById(userId);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역 조회 기능 테스트
     */
    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역 조회 로직 확인")
    public void ValidUserPointTest() {

        long userId = 1L;

        // given
        List<PointHistory> mockHistory = List.of(
                new PointHistory(1L, userId, 200, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 100, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(mockHistory);

        // when
        List<PointHistory> result = pointService.getPointHistory(userId);

        // then
        assertEquals(2, result.size());
        assertEquals(200, result.get(0).amount());
        assertEquals(100, result.get(1).amount());

        verify(pointHistoryTable).selectAllByUserId(userId);
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능 테스트
     */
    @Test
    @DisplayName("특정 유저의 포인트를 충전하는 테스트")
    public void UserPointUseTest() {

        // given
        long userId = 1L;
        long amount = 200;

        UserPoint updateUserPoint = new UserPoint(userId, 500, System.currentTimeMillis());

        when(userPointTable.insertOrUpdate(userId, amount))
                .thenReturn(updateUserPoint);

        // when
        UserPoint result = pointService.chargePoint(userId, amount);

        System.out.println(updateUserPoint.point());
        System.out.println(result.point());

        // then
        assertEquals(updateUserPoint.point(), result.point());

    }


}