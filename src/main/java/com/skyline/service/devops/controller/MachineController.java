package com.skyline.service.devops.controller;

import com.skyline.platform.core.controller.BaseController;
import com.skyline.platform.core.model.ResponseModel;
import com.skyline.platform.core.model.ResponseStatus;
import com.skyline.platform.core.service.UserService;
import com.skyline.service.devops.entity.MachineInfoDecrypt;
import com.skyline.service.devops.service.MachineService;
import com.skyline.util.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MachineController extends BaseController {
    @Autowired
    MachineService machineService;
    @Autowired
    UserService userService;

    Logger logger = LoggerFactory.getLogger(MachineController.class);

    @RequestMapping(value = {"/security/addMachine.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.POST})
    public ResponseModel addMachine(@RequestBody JSONObject args) {
        String loginType = (String) args.get("loginType");
        String ip = (String) args.get("ip");
        String port = String.valueOf(args.get("loginPort"));
        String loginUser = (String) args.get("loginUser");
        String loginPassword = (String) args.get("loginPassword");
        String loginCmd = (String) args.get("loginCmd");
        String activeSudoRoot = (String) args.get("activeSudoRoot");
        String activeSuRoot = (String) args.get("activeSuRoot");
        String rootPassword = (String) args.get("rootPassword");
        String rootCmd = (String) args.get("rootCmd");
        String param_tags = (String) args.get("tags");

        try {
            return doIt(machineService.addMachine(loginType, ip, port, loginUser, loginPassword, loginCmd, activeSudoRoot, activeSuRoot, rootPassword, rootCmd, "", param_tags));
        } catch (Exception e) {
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR);
        }
    }

    @RequestMapping(value = {"/security/getAllMachine.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.POST})
    public ResponseModel getAllMachine() {
        boolean isAdminUser = userService.hasRole("admin");
        List<MachineInfoDecrypt> machines;
        JSONArray result = new JSONArray();
        try {

            if (isAdminUser) machines = machineService.getAllMachine();
            else machines = machineService.getCurrentUserAllMachine();
            for (MachineInfoDecrypt machine : machines) {
                JSONObject json = new JSONObject();
                json.put("ip", machine.getIp());
                json.put("sshPort", machine.getSshPort());
                json.put("loginUser", machine.getLoginUser());
                json.put("tags", machine.getTags());
                if (isAdminUser) json.put("belong", machine.getUser().getUsername());
                result.add(json);
            }
        } catch (Exception e) {
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR);
        }
        return new ResponseModel(result);
    }

    @RequestMapping(value = {"/security/getAllTag.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.POST})
    public ResponseModel getAllTag() {
        return new ResponseModel(machineService.getAllTags());
    }

    /*@RequestMapping(value = {"/security/addMachineByKeypassFile.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.GET})
    public ResponseModel addMachineByKeypassFile() {
    }*/

}
