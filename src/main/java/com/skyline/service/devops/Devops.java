package com.skyline.service.devops;

import com.skyline.platform.core.Run;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@ServletComponentScan
@SpringBootApplication
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class Devops {
    public static void main(String[] args) {
        Class[] classes = new Class[2];
        classes[0] = Run.class;
        classes[1]= Devops.class;
        SpringApplication.run(classes , args);
    }
}