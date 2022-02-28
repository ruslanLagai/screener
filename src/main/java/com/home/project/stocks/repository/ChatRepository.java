package com.home.project.stocks.repository;

import com.home.project.stocks.model.entity.TelegramChatEntity;
import com.home.project.stocks.model.telegram.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

/**
 * @author rlagay
 */
public interface ChatRepository extends JpaRepository<TelegramChatEntity, Long> {
    Set<TelegramChatEntity> findByStatus(ChatStatus chatStatus);
}
