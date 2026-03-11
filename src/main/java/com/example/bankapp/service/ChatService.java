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
        
        // ⭐ REFRESH account from database to get latest balance
        Account freshAccount = accountService.getAccountById(account.getId())
            .orElse(account); // Fallback to passed account if not found
        
        // Get chat history
        List<ChatMessage> history = chatMessageRepository.findByAccountIdOrderByTimestampAsc(account.getId());
        
        List<Transaction> recent = accountService.getTransactionHistory(freshAccount);
        String context = buildContext(freshAccount, recent);  // ⭐ Use fresh account

        // Build messages with history (last 4 messages only for tinyllama's 2048 token limit)
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", context));
        
        // Add recent history (reduced from 10 to 4 to prevent token limit issues)
        int startIdx = Math.max(0, history.size() - 4);
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
        sb.append("You are a helpful banking assistant. ");
        sb.append("Answer in 1-2 sentences. ");
        sb.append("Current balance: $").append(account.getBalance());

        if (!transactions.isEmpty()) {
            sb.append(". Recent: ");
            int limit = Math.min(transactions.size(), 3);
            for (int i = 0; i < limit; i++) {
                Transaction t = transactions.get(i);
                sb.append(t.getType()).append(" $").append(t.getAmount());
                if (i < limit - 1) sb.append(", ");
            }
        }

        return sb.toString();
    }


    public void clearChatHistory(Account account) {
        chatMessageRepository.deleteByAccountId(account.getId());
    }

}
