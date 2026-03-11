package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.ChatMessage;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AccountService accountService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(AccountService accountService, ChatMessageRepository chatMessageRepository) {
        this.accountService = accountService;
        this.chatMessageRepository = chatMessageRepository;
    }

    public String chat(Account account, String userMessage) {
        // Save user message
        chatMessageRepository.save(new ChatMessage(account.getId(), "user", userMessage));
        
        // Get chat history
        List<ChatMessage> history = chatMessageRepository.findByAccountIdOrderByTimestampAsc(account.getId());
        
        List<Transaction> recent = accountService.getTransactionHistory(account);
        String context = buildContext(account, recent);

        // Build messages with history (last 10 messages)
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", context));
        
        // Add recent history
        int startIdx = Math.max(0, history.size() - 10);
        for (int i = startIdx; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            messages.add(Map.of("role", msg.getRole(), "content", msg.getMessage()));
        }

        Map<String, Object> request = Map.of(
            "model", model,
            "messages", messages,
            "stream", false
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(
                ollamaUrl + "/api/chat", request, Map.class
            );
            if (response != null && response.containsKey("message")) {
                Map<String, String> message = (Map<String, String>) response.get("message");
                String botReply = message.get("content");
                
                // Save bot response
                chatMessageRepository.save(new ChatMessage(account.getId(), "assistant", botReply));
                
                return botReply;
            }
            return "Sorry, I couldn't process that.";
        } catch (Exception e) {
            return "AI assistant is unavailable. Please make sure Ollama is running.";
        }
    }
    
    public List<ChatMessage> getChatHistory(Account account) {
        return chatMessageRepository.findByAccountIdOrderByTimestampAsc(account.getId());
    }

    private String buildContext(Account account, List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful banking assistant for BankApp. ");
        sb.append("Keep answers short and friendly (2-3 sentences max). ");
        sb.append("\n\nCustomer details:");
        sb.append("\n- Username: ").append(account.getUsername());
        sb.append("\n- Balance: $").append(account.getBalance());
        sb.append("\n- Account ID: ").append(account.getId());

        if (!transactions.isEmpty()) {
            sb.append("\n\nRecent transactions:");
            int limit = Math.min(transactions.size(), 5);
            for (int i = 0; i < limit; i++) {
                Transaction t = transactions.get(i);
                sb.append("\n- ").append(t.getType())
                  .append(": $").append(t.getAmount())
                  .append(" on ").append(t.getTimestamp().toLocalDate());
            }
        } else {
            sb.append("\n\nNo transactions yet.");
        }

        return sb.toString();
    }
}
