package org.lcr.aidemo.repository;


import org.lcr.aidemo.entity.Qa;
import org.lcr.aidemo.entity.dto.MsgDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QaRepository extends JpaRepository<Qa, Long> {
    List<Qa> findByChatIdOrderByTime(Long chatId);
    List<Qa> findAllById(Long ids);

    //条件查询
    @Query("select new org.lcr.aidemo.entity.dto.MsgDto(" +
            "   qa.id, qa.q, qa.a, c.id, u.name, qa.time) " +
            "from Qa qa " +
            "join Chat c on qa.chatId = c.id " +
            "join User u on c.userId = u.id " +
            "where (:userName is null or u.name like %:userName%) " +
            "  and (:convId   is null or c.id = :convId) " +
            "  and (:startTime is null or qa.time >= :startTime) " +
            "  and (:endTime   is null or qa.time <= :endTime)")
    Page<MsgDto> search(
            @Param("userName")  String  userName,
            @Param("convId")    Long    convId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime")   LocalDateTime endTime,
            Pageable pageable
    );

}