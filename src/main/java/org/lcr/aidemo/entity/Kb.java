package org.lcr.aidemo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "kb")
@Data
public class Kb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String q;
    @Column(columnDefinition = "TEXT")
    private String a;
}