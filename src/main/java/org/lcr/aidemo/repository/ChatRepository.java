package org.lcr.aidemo.repository;

import org.lcr.aidemo.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserIdOrderByLastTimeDesc(Long userId);
}
