package org.lcr.aidemo.service;

import lombok.RequiredArgsConstructor;
import org.lcr.aidemo.entity.User;
import org.lcr.aidemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public void register(String name, String phone, String rawPwd) {
        if (repo.findByPhone(phone).isPresent()) {
            throw new RuntimeException("用户已存在(手机号重复注册)");
        }
        User u = new User();
        u.setName(name);
        u.setPhone(phone);
        u.setPassword(encoder.encode(rawPwd));
//        u.setPicture();
        u.setType(0);
        repo.save(u);
    }
}