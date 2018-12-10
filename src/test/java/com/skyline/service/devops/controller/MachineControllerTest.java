package com.skyline.service.devops.controller;

import com.skyline.platform.core.Run;
import com.skyline.platform.core.model.ResponseModel;
import com.skyline.service.devops.Devops;
import com.skyline.service.devops.MySpringJUnit4ClassRunner;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(MySpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Devops.class, Run.class})
@WebAppConfiguration
@Transactional
public class MachineControllerTest {
    @Autowired
    MachineController machineController;

    @Test
    public void addMachine() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginType", "password");
        jsonObject.put("ip", "172.18.5.42");
        jsonObject.put("port", "22");
        jsonObject.put("loginUser", "kduser");
        jsonObject.put("loginPassword", "Kingdee@2018");
        jsonObject.put("tags","redis");

        System.out.println(machineController.addMachine(jsonObject).getErrMsg());
    }

    @Test
    public void getAllMachine() {
        ResponseModel responseModel = machineController.getAllMachine();
        System.out.println(responseModel.getErrMsg());
    }
}