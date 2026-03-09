package org.lcr.aidemo.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgDto {
    private Long id;
    private String q;
    private String a;
    private Long chatId;
    private String userName;
    private LocalDateTime time;
}