package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AccountService accountService;

    public ChatService(AccountService accountService) {
        this.accountService = accountService;
    }

    public String chat(Account account, String userMessage) {
        List<Transaction> recent = accountService.getTransactionHistory(account);
        String context = buildContext(account, recent);

        Map<String, Object> request = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", context),
                Map.of("role", "user", "content", userMessage)
            ),
            "stream", false
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(
                ollamaUrl + "/api/chat", request, Map.class
            );
            if (response != null && response.containsKey("message")) {
                Map<String, String> message = (Map<String, String>) response.get("message");
                return message.get("content");
            }
            return "Sorry, I couldn't process that.";
        } catch (Exception e) {
            return "AI assistant is unavailable. Please make sure Ollama is running.";
        }
    }

    private String buildContext(Account account, List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        
        // Strict system prompt to prevent hallucinations
        sb.append("You are a banking assistant. Follow these rules strictly:\n");
        sb.append("1. ONLY answer questions using the data provided below\n");
        sb.append("2. If asked about data you don't have, say 'I don't have that information'\n");
        sb.append("3. NEVER make up numbers, dates, or transaction details\n");
        sb.append("4. Keep responses under 3 sentences\n");
        sb.append("5. Be helpful and friendly\n");
        sb.append("\n=== CUSTOMER DATA (USE ONLY THIS) ===\n");
        sb.append("Username: ").append(account.getUsername()).append("\n");
        sb.append("Current Balance: $").append(account.getBalance()).append("\n");
        sb.append("Account ID: ").append(account.getId()).append("\n");

        if (!transactions.isEmpty()) {
            sb.append("\nRecent Transactions (last ").append(Math.min(transactions.size(), 5)).append("):\n");
            int limit = Math.min(transactions.size(), 5);
            for (int i = 0; i < limit; i++) {
                Transaction t = transactions.get(i);
                sb.append("- ").append(t.getType())
                  .append(": $").append(t.getAmount())
                  .append(" on ").append(t.getTimestamp().toLocalDate())
                  .append("\n");
            }
        } else {
            sb.append("\nNo transactions yet.\n");
        }
        
        sb.append("=== END OF DATA ===\n");
        sb.append("\nAnswer the user's question using ONLY the data above.");

        return sb.toString();
    }
}
