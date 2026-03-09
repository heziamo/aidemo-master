package org.lcr.aidemo.service;


import lombok.RequiredArgsConstructor;
import org.lcr.aidemo.entity.User;
import org.lcr.aidemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// service/UserDetailsServiceImpl.java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User u = repo.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("手机号不存在：" + phone));
        // 根据用户类型设置权限
        String authority = u.getType() == 1 ? "ROLE_ADMIN" : "ROLE_USER";

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getPhone())
                .password(u.getPassword())
                .authorities(authority) // 动态设置权限
                .build();
    }

//    @Override
//    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
//        User u = repo.findByPhone(phone)
//                .orElseThrow(() -> new UsernameNotFoundException("手机号不存在：" + phone));
//
//        String authority = u.getType() == 1 ? "ADMIN" : "USER";
//
//        return new CustomUserDetails(u.getPhone(), u.getPassword(),
//                Collections.singletonList(new SimpleGrantedAuthority(authority)), u);
//    }
}
