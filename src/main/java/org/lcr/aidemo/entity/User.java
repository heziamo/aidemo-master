package org.lcr.aidemo.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Arrays;

// entity/User.java
@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "picture")
    private byte[] picture;

    @Column(name = "type")
    private int type;

}
