package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


public interface PointService {
    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    UserPoint getUserPoint(long id);

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    List<PointHistory> getPointHistory(long userId);

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    UserPoint chargePoint(long userId, long amount);

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    UserPoint usePoint(long userId, long amount);


}
