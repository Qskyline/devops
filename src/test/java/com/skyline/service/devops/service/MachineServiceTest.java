package com.skyline.service.devops.service;

import com.skyline.service.devops.MySpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(MySpringJUnit4ClassRunner.class)
@SpringBootTest
public class MachineServiceTest {
    @Autowired
    MachineService machineService;

    @Test
    public void addMachine() {
    }

    @Test
    public void getAllMachine() {
    }
}