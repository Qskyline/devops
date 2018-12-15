package com.skyline.service.devops.service;

import com.skyline.platform.core.entity.User;
import com.skyline.platform.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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
        User user = userService.getUser(userName);
        return user;
    }
}
