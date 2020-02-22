package com.skyline.service.devops.controller;

import com.skyline.platform.core.Run;
import com.skyline.service.devops.AppStart;
import com.skyline.service.devops.MySpringJUnit4ClassRunner;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.Cookie;

@RunWith(MySpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {AppStart.class, Run.class})
@WebAppConfiguration
@Transactional
public class MachineControllerTest {
    private MockMvc mockMvc;

    @Autowired
    MachineController machineController;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockUser(authorities = {"user"})
    public void addMachine() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginType", "password");
        jsonObject.put("ip", "172.18.5.109");
        jsonObject.put("loginPort", "22");
        jsonObject.put("loginUser", "kduser");
        jsonObject.put("loginPassword", "Kingdee@2018");
        jsonObject.put("tags","redis");

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .post("/security/addMachine.do")
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .cookie(new Cookie("SESSION", "MmU4MGJhMDgtYTQ2ZC00Y2QzLWE0MzItMzJkZTVjODEzYzlm"))
        ).andReturn();

        HandlerInterceptor[] interceptors = mvcResult.getInterceptors();
        for (HandlerInterceptor h : interceptors) {
            System.out.println(h.getClass().getName());
        }
        String responseString = mvcResult.getResponse().getContentAsString();
        System.out.println("返回内容："+responseString);
        int status = mvcResult.getResponse().getStatus();
        Assert.assertEquals("return status not equals 200", 200, status);
    }
}