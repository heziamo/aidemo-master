package org.lcr.aidemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lcr.aidemo.entity.Kb;
import org.lcr.aidemo.repository.KbRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String modelName;

    private final KbRepository kbRepo;
    private final StringRedisTemplate redis;
    private final RestTemplate restTemplate;
    // 手动创建 ObjectMapper 用于 JSON 序列化/反序列化
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KEY_PREFIX = "kb:single:";
    // 常量：缓存过期时间（1小时）
    private static final Duration TTL = Duration.ofHours(1);
    // 常量：线程池，用于异步处理 API 调用
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    // 新增：用于存储对话历史的 Redis key 前缀
    private static final String CHAT_HISTORY_PREFIX = "chat:history:";

    public SseEmitter generateAnswer(Long chatId, String question) {
        SseEmitter emitter = new SseEmitter(360_000L);

        // 1. 先查单次问题缓存（保持原有逻辑）
        String singleKey = KEY_PREFIX + question;
        String cachedAnswer = redis.opsForValue().get(singleKey);
        if (cachedAnswer != null) {
            sendAnswer(emitter, cachedAnswer);
            return emitter;
        }

        // 2. 查知识库（保持原有逻辑）
        cachedAnswer = kbRepo.findByQ(question).map(Kb::getA).orElse(null);
        if (cachedAnswer != null) {
            redis.opsForValue().set(singleKey, cachedAnswer, TTL);
            sendAnswer(emitter, cachedAnswer);
            return emitter;
        }

        // 3. 异步调用 DeepSeek API，并传入历史上下文
        executorService.execute(() -> callDeepSeekStreamWithHistory(emitter, chatId, question));

        return emitter;
    }

    private void callDeepSeekStreamWithHistory(SseEmitter emitter, Long chatId, String question) {
        try {
            log.info("Using DeepSeek API with chat history for chatId: {}, question: {}", chatId, question);

            // ------------------- 核心：获取历史对话 -------------------
            String historyKey = CHAT_HISTORY_PREFIX + chatId;
            List<String> historyJsonList = redis.opsForList().range(historyKey, 0, -1);
            List<Map<String, String>> messages = new ArrayList<>();

            // 可选：加系统提示词
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个聪明、友好的中文助手。请基于之前的对话上下文自然地回答。");
            messages.add(systemMsg);

            // 把历史加进去（假设历史存的是 JSON 格式：{"role":"user/assistant","content":"..."}）
            if (historyJsonList != null && !historyJsonList.isEmpty()) {
                for (String json : historyJsonList) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, String> msg = objectMapper.readValue(json, Map.class);
                        messages.add(msg);
                    } catch (Exception e) {
                        log.warn("Invalid history JSON: {}", json);
                    }
                }
            }

            // 加当前用户问题
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", question);
            messages.add(userMsg);

            // ------------------- 构建请求 -------------------
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);

            // 创建 HTTP 连接
            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);

            // 发送请求
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            try (var os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                // 错误处理（保持原有）
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        error.append(line);
                    }
                    log.error("DeepSeek API error - Status: {}, Response: {}", responseCode, error);
                    emitter.send("抱歉，AI 服务暂时不可用（错误码：" + responseCode + "）");
                    emitter.complete();
                    return;
                }
            }

            // ------------------- 处理流式响应 -------------------
            StringBuilder fullAnswer = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        if (!data.isEmpty()) {
                            try {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> chunk = objectMapper.readValue(data, Map.class);
                                List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                    if (delta != null && delta.containsKey("content")) {
                                        String content = (String) delta.get("content");
                                        if (content != null && !content.isBlank()) {
                                            fullAnswer.append(content);
                                            emitter.send(content);
                                        }
                                    }
                                }
                            } catch (Exception parseEx) {
                                log.debug("Failed to parse chunk: {}", data);
                            }
                        }
                    }
                }
            }

            // ------------------- 保存本次对话到历史 -------------------
            if (fullAnswer.length() > 0) {
                // 存用户问题
                Map<String, String> userEntry = new HashMap<>();
                userEntry.put("role", "user");
                userEntry.put("content", question);
                redis.opsForList().rightPush(historyKey, objectMapper.writeValueAsString(userEntry));

                // 存 AI 回答
                Map<String, String> assistantEntry = new HashMap<>();
                assistantEntry.put("role", "assistant");
                assistantEntry.put("content", fullAnswer.toString());
                redis.opsForList().rightPush(historyKey, objectMapper.writeValueAsString(assistantEntry));

                // 设置过期时间（可选）
                redis.expire(historyKey, Duration.ofDays(7));
            }

            emitter.complete();

        } catch (Exception e) {
            log.error("Error in DeepSeek stream call", e);
            try {
                emitter.send("抱歉，生成回答时出错：" + e.getMessage());
                emitter.complete();
            } catch (Exception ignored) {}
        }
    }

    private void sendAnswer(SseEmitter emitter, String answer) {
        try {
            // 建议改成一次发送完整内容，更流畅
            emitter.send(answer);
            emitter.complete();
        } catch (Exception e) {
            log.error("Error sending cached answer", e);
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {}
        }
    }
}