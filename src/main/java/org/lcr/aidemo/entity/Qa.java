package org.lcr.aidemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Qa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private String q;

    @Column(columnDefinition = "TEXT")
    private String a;
    private LocalDateTime time;
}