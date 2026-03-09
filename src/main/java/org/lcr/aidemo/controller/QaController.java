package org.lcr.aidemo.controller;

import lombok.RequiredArgsConstructor;
import org.lcr.aidemo.entity.Kb;
import org.lcr.aidemo.entity.dto.MsgDto;
import org.lcr.aidemo.repository.KbRepository;
import org.lcr.aidemo.repository.QaRepository;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/qa")
@RequiredArgsConstructor
public class QaController {

    private final KbRepository kbRepo;
    private final QaRepository qaRepo;

    /* GET /api/admin/qa -> List<KbDTO> */
//    @GetMapping
//    public List<MsgDto> list() {
//        return qaRepo.findAll()
//                .stream()
//                .map(k -> new MsgDto(k.getId(), k.getQ(), k.getA(), null, null, null))
//                .collect(Collectors.toList());
//    }

    @GetMapping
    public Page<MsgDto> list(@RequestParam(required = false) String userName,
                             @RequestParam(required = false) Long convId,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "15") int size) {

        // 日期 → 当日 00:00 / 23:59:59
        LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime end   = endDate   == null ? null : endDate.atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "time"));
        return qaRepo.search(userName, convId, start, end, pageable);
    }

    /* POST /api/admin/kb -> KbDTO */
    @PostMapping
    public MsgDto save(@RequestBody MsgDto dto) {
        Kb k = dto.getId() == null ? new Kb() : kbRepo.findById(dto.getId()).orElse(new Kb());
        k.setQ(dto.getQ());
        k.setA(dto.getA());
        k = kbRepo.save(k);
        return new MsgDto(k.getId(), k.getQ(), k.getA(), null, null, null);
    }

    /* DELETE /api/admin/kb/{id} -> void */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        kbRepo.deleteById(id);
    }

}