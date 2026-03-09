package org.lcr.aidemo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.lcr.aidemo.entity.Kb;
import org.lcr.aidemo.entity.Qa;
import org.lcr.aidemo.entity.dto.MsgDto;
import org.lcr.aidemo.repository.KbRepository;
import org.lcr.aidemo.repository.QaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbRepository kbRepo;
    private final QaRepository qaRepo;

    /* GET /api/admin/kb -> List<KbDTO> */
//    @GetMapping
//    public List<MsgDto> list() {
//
//        return kbRepo.findAll()
//                .stream()
//                .map(k -> new MsgDto(k.getId(), k.getQ(), k.getA(), null, null, null))
//                .collect(Collectors.toList());
//    }
    @GetMapping
    public Page<MsgDto> list(
            @RequestParam(defaultValue = "") String kw,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Specification<Kb> spec = (root, query, cb) -> {
            if (kw == null || kw.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(
                    cb.like(root.get("q"), "%" + kw + "%"),
                    cb.like(root.get("a"), "%" + kw + "%")
            );
        };

        return kbRepo.findAll(spec, pageable)
                .map(k -> new MsgDto(k.getId(), k.getQ(), k.getA(), null, null, null));
    }

    /* POST /api/admin/kb -> KbDTO */
    @PostMapping
    public List<MsgDto> save(@RequestBody List<Long> ids) {
        // 1. 一次性查出所有 id 对应的 QA
        List<Qa> qaList = qaRepo.findAllById(ids);
        // 2. 转换成 Kb 实体
        List<Kb> kbList = qaList.stream()
                .map(qa -> {
                    Kb kb = new Kb();
                    kb.setQ(qa.getQ());
                    kb.setA(qa.getA());
                    return kb;
                })
                .collect(Collectors.toList());

        // 3. 批量保存
        kbList = kbRepo.saveAll(kbList);

        // 4. 返回 DTO 列表给前端
        return kbList.stream()
                .map(k -> new MsgDto(k.getId(), k.getQ(), k.getA(), null, null, null))
                .collect(Collectors.toList());
    }

    @PostMapping("/update/{id}")
    public void update(@PathVariable Long id, @RequestBody Kb kb) {
        System.out.println(kb);
        Kb newkb = new Kb();
        newkb.setId(id);
        newkb.setQ(kb.getQ());
        newkb.setA(kb.getA());
        kbRepo.save(newkb);
    }

    @PostMapping("/update")
    public void add(@RequestBody Kb kb) {
        Kb newkb = new Kb();
        newkb.setQ(kb.getQ());
        newkb.setA(kb.getA());
        kbRepo.save(newkb);
    }

    /* DELETE /api/admin/kb/{id} -> void */
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        System.out.println(id);
        kbRepo.deleteById(id);
    }

    @PostMapping("/import")
    public void batchImport(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws Exception {
        InputStream is = file.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
        Kb[] entities = mapper.readValue(is, Kb[].class);

        for (Kb entity : entities) {
            kbRepo.save(entity);
        }
        response.addHeader("Content-Type", "text/plain; charset=utf-8");
        response.addHeader("Content-Disposition", "attachment; filename="+file.getOriginalFilename());
        try (OutputStream os = response.getOutputStream()) {
            os.write(("成功导入 " + entities.length + " 条记录").getBytes());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> export(HttpServletResponse response) throws IOException {
        List<Kb> list = kbRepo.findAll();
        ObjectMapper mapper = new ObjectMapper();

        // 设置响应头信息
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=kb_data.json");

        // 写入响应流
        try (OutputStream os = response.getOutputStream()) {
            mapper.writeValue(os, list);
            return ResponseEntity.ok().build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("导出 JSON 失败", e);
        }
    }
}