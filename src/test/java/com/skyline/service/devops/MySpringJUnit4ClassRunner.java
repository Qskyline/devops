package com.skyline.service.devops;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class MySpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {
    public MySpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }
}