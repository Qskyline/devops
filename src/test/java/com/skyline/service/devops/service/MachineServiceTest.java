package com.skyline.service.devops.service;

import com.skyline.service.devops.Devops;
import com.skyline.service.devops.MySpringJUnit4ClassRunner;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(MySpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Devops.class)
public class MachineServiceTest {
    @Autowired
    MachineService machineService;

    @Test
    public void addMachine() {
    }

    @Test
    public void getAllMachine() {
    }

    @Test
    public void importMachineInfoFromKeypass() {
        try {
            machineService.importMachineInfoFromKeypass("file:///Users/skyline/Downloads/kdngproduct.xml", "2c948a84680d488801680d4ff3ce0000");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}