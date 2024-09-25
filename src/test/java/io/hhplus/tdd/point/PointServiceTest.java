package io.hhplus.tdd.point;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    public void testGetUserPoint_ValidUser() {

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

   


}