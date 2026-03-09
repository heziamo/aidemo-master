package org.lcr.aidemo.controller;

import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.lcr.aidemo.entity.Chat;
import org.lcr.aidemo.entity.Qa;
import org.lcr.aidemo.entity.User;
import org.lcr.aidemo.entity.dto.ChatDto;
import org.lcr.aidemo.entity.dto.MsgDto;
import org.lcr.aidemo.repository.ChatRepository;
import org.lcr.aidemo.repository.QaRepository;
import org.lcr.aidemo.repository.UserRepository;
import org.lcr.aidemo.service.AnswerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRepository chatRepo;
    private final QaRepository qaRepo;
    private final UserRepository userRepo;
    private final AnswerService answerService;
    private final RestTemplate restTemplate;

    /* 1. 会话列表 */
    @GetMapping("/conversations")
    public List<ChatDto> list(@AuthenticationPrincipal UserDetails user) {
        String userphone = user.getUsername();
        User u = userRepo.findByPhone(userphone)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + userphone));

        return chatRepo.findByUserIdOrderByLastTimeDesc(u.getId())
                .stream().map(c -> {
                    ChatDto dto = new ChatDto();
                    dto.setId(c.getId());
                    dto.setTitle(c.getTitle());
                    dto.setLastMsgTime(c.getLastTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
                    return dto;
                }).collect(Collectors.toList());
    }

    /* 2. 新建会话 */
    @PostMapping("/conversations")
    public Chat create(@AuthenticationPrincipal UserDetails user) {
        String userphone = user.getUsername();
        User u = userRepo.findByPhone(userphone)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + userphone));

        Chat c = new Chat();
        c.setUserId(u.getId());
        c.setTitle("新对话");
        c.setLastTime(LocalDateTime.now());
        return chatRepo.save(c);
    }

    /* 3. 修改对话标题 */
    @PutMapping("/chat/{chatId}/update")
    public ResponseEntity<?> updateTitle(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails user) {

        String userPhone = user.getUsername();
        User u = userRepo.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + userPhone));

        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会话不存在"));

        // 只能改自己的会话
        if (!chat.getUserId().equals(u.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权修改他人会话");
        }

        String newTitle = body.get("title");
        if (newTitle == null || newTitle.trim().isEmpty()) {
            newTitle = "null";
        }

        chat.setTitle(newTitle.trim());
        chat.setLastTime(LocalDateTime.now());
        chatRepo.save(chat);
        return ResponseEntity.ok().build();
    }

    /* 3. 会话消息 */
    @GetMapping("/chat/{convId}")
    public List<MsgDto> messages(@PathVariable Long convId) {
        return qaRepo.findByChatIdOrderByTime(convId)
                .stream().map(q -> {
                    MsgDto dto = new MsgDto();
                    dto.setId(q.getId());
                    dto.setQ(q.getQ());
                    dto.setA(q.getA());
                    return dto;
                }).collect(Collectors.toList());
    }

    /* 4. 发送消息并生成回答 */
    @GetMapping("/chat/{convId}/answer")
    public SseEmitter getAnswer(@PathVariable Long convId,
                           @RequestParam String q) throws IOException {

        SseEmitter emitter = answerService.generateAnswer(convId, q); // 随机或调用 AI

        return emitter;
    }

    @PostMapping("/chat/{convId}/local")
    public Map<String, String> localAnswer(@PathVariable Long convId,
                                           @RequestBody Map<String,String> body) throws IOException {
        String q = body.get("q");

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")  // Ollama 服务地址
                .modelName("gemma2:2b")               // 模型名称
                .temperature(0.8)                  // 创造性 (0~1)
                .build();

        String answer = model.generate(q);
        System.out.println(answer);

        // 落库
        Qa qa = new Qa();
        qa.setChatId(convId);
        qa.setQ(q);
        qa.setA(answer);
        qa.setTime(LocalDateTime.now());
        qaRepo.save(qa);

        chatRepo.findById(convId).ifPresent(c -> {
            c.setLastTime(LocalDateTime.now());
            chatRepo.save(c);
        });

        Map<String, String> result = new HashMap<>();
        result.put("a", answer);
        return result;
    }

    @PostMapping("/chat/{convId}/save")
    public ResponseEntity<Void> saveQA(
            @PathVariable Long convId,
            @RequestBody Map<String,String> body) {

        String q = body.get("Q");
        String a = body.get("A");

        Qa qa = new Qa();
        qa.setChatId(convId);
        qa.setQ(q);
        qa.setA(a);
        qa.setTime(LocalDateTime.now());
        qaRepo.save(qa);
        System.out.println("qa"+qa);
        // 更新会话 last_time
        chatRepo.findById(convId).ifPresent(c -> {
            c.setLastTime(LocalDateTime.now());
            chatRepo.save(c);
        });

        return ResponseEntity.ok().build();
    }

}