package com.skyline.service.devops.service;

import com.skyline.platform.core.entity.User;
import com.skyline.platform.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;

/**
 * @ClassName UserService
 * @Description TODO
 * @Author skyline
 * @Date 2018/12/15 16:09
 * Version 1.0
 **/
@Service
public class MyUserService {
    @Autowired
    private UserService userService;

    public User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = userDetails.getUsername();
        userDetails.getAuthorities();
        User user = userService.getUser(userName);
        return user;
    }

    public boolean hasRole(String roleName) {
        try {
            roleName = "role_" +roleName;
            Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            while (iterator.hasNext()) {
                if (roleName.equalsIgnoreCase(iterator.next().getAuthority())) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
