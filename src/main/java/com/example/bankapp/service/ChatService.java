package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ChatService {

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiApiUrl;

    @Value("${gemini.model:gemini-3-flash-preview}")
    private String geminiModel;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${ai.timeout.connect-ms:3000}")
    private int connectTimeoutMs;

    @Value("${ai.timeout.read-ms:30000}")
    private int readTimeoutMs;

    @Value("${gemini.max-output-tokens:512}")
    private int geminiMaxOutputTokens;

    private final RestTemplateBuilder restTemplateBuilder;
    private final AccountService accountService;

    public ChatService(AccountService accountService, RestTemplateBuilder restTemplateBuilder) {
        this.accountService = accountService;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public String chat(Account account, String userMessage) {
        List<Transaction> recent = accountService.getTransactionHistory(account);
        String deterministicReply = tryBuildDeterministicReply(account, recent, userMessage);
        if (deterministicReply != null) {
            return deterministicReply;
        }

        String context = buildContext(account, recent);

        RestTemplate restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
            .setReadTimeout(Duration.ofMillis(readTimeoutMs))
            .build();

        try {
            return askGemini(restTemplate, context, userMessage);
        } catch (ResourceAccessException e) {
            return "AI assistant is taking longer than expected. Please try again in a few seconds.";
        } catch (Exception e) {
            return "AI assistant is unavailable. Please try again shortly.";
        }
    }

    private String tryBuildDeterministicReply(Account account, List<Transaction> recent, String userMessage) {
        String normalized = userMessage == null ? "" : userMessage.toLowerCase(Locale.ROOT);

        boolean asksBalance = normalized.contains("balance") || normalized.contains("current balance");
        if (asksBalance) {
            return "Hi " + account.getUsername() + "! Your current balance is $" + formatMoney(account.getBalance()) + ".";
        }

        boolean asksTransactions = normalized.contains("transaction")
            || normalized.contains("transactions")
            || normalized.contains("history")
            || normalized.contains("statement");
        if (asksTransactions) {
            if (recent.isEmpty()) {
                return "Hi " + account.getUsername() + "! You do not have any transactions yet.";
            }

            int limit = Math.min(recent.size(), 4);
            StringBuilder response = new StringBuilder("Hi ")
                .append(account.getUsername())
                .append("! Here are your recent transactions: ");

            for (int i = 0; i < limit; i++) {
                Transaction t = recent.get(i);
                if (i > 0) {
                    response.append("; ");
                }
                response.append(t.getTimestamp().toLocalDate())
                    .append(" ")
                    .append(t.getType())
                    .append(" $")
                    .append(formatMoney(t.getAmount()));
            }

            response.append(".");
            return response.toString();
        }

        return null;
    }

    private String formatMoney(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }

    private String askGemini(RestTemplate restTemplate, String context, String userMessage) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing");
        }

        Map<String, Object> request = Map.of(
            "system_instruction", Map.of(
                "parts", List.of(Map.of("text", context))
            ),
            "contents", List.of(
                Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
                )
            ),
            "generationConfig", Map.of(
                "temperature", 0.2,
                "maxOutputTokens", geminiMaxOutputTokens
            )
        );

        String endpoint = geminiApiUrl + "/" + geminiModel + ":generateContent?key=" + geminiApiKey;
        Map<String, Object> response = restTemplate.postForObject(endpoint, request, Map.class);

        if (response == null) {
            return "Sorry, I couldn't process that.";
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getOrDefault("candidates", Collections.emptyList());
        if (candidates.isEmpty()) {
            return "Sorry, I couldn't process that.";
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).getOrDefault("content", Collections.emptyMap());
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.getOrDefault("parts", Collections.emptyList());
        if (parts.isEmpty()) {
            return "Sorry, I couldn't process that.";
        }

        StringBuilder merged = new StringBuilder();
        for (Map<String, Object> part : parts) {
            Object text = part.get("text");
            if (text != null) {
                if (merged.length() > 0) {
                    merged.append("\n");
                }
                merged.append(text);
            }
        }

        return merged.length() == 0 ? "Sorry, I couldn't process that." : merged.toString();
    }

    private String buildContext(Account account, List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful banking assistant for BankApp. ");
        sb.append("Keep answers friendly and concise. ");
        sb.append("If the user asks for transactions, list them clearly with key details. ");
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
