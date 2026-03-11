package com.example.bankapp.repository;

import com.example.bankapp.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByAccountIdOrderByTimestampAsc(Long accountId);

    @Transactional
    void deleteByAccountId(Long accountId);
}
