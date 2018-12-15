package com.skyline.service.devops.controller;

import com.skyline.platform.core.controller.BaseController;
import com.skyline.platform.core.model.ResponseModel;
import com.skyline.service.devops.entity.MachineEntity;
import com.skyline.service.devops.entity.TagEntity;
import com.skyline.service.devops.service.MachineService;
import com.skyline.util.ExceptionUtil;
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
import java.util.ArrayList;
import java.util.List;

@RestController
public class MachineController extends BaseController {
    @Autowired
    MachineService machineService;

    Logger logger = LoggerFactory.getLogger(MachineController.class);

    @RequestMapping(value = {"/security/addMachine.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.POST})
    public ResponseModel addMachine(@RequestBody JSONObject args) {
        String loginType = (String) args.get("loginType");
        String ip = (String) args.get("ip");
        String param_port = String.valueOf(args.get("loginPort"));
        String loginUser = (String) args.get("loginUser");
        String loginPassword = (String) args.get("loginPassword");
        String loginCmd = (String) args.get("loginCmd");
        String param_activeSudoRoot = (String) args.get("activeSudoRoot");
        String param_activeSuRoot = (String) args.get("activeSuRoot");
        String rootPassword = (String) args.get("rootPassword");
        String rootCmd = (String) args.get("rootCmd");
        String param_tags = (String) args.get("tags");

        int port = 0;
        try {
            if (StringUtils.isNotBlank(param_port)) {
                port = Integer.valueOf(param_port);
            }
        } catch (Exception e) {
            String errMsg =  "The \"port\" param must be numeric.";
            logger.error(errMsg);
            logger.error(ExceptionUtil.getStackTrace(e));
            return new ResponseModel(ResponseModel.Status.OPERATION_FAILED, errMsg);
        }

        boolean activeSudoRoot = false;
        boolean activeSuRoot = false;
        try {
            if(StringUtils.isNotBlank(param_activeSudoRoot)) {
                activeSudoRoot = Boolean.valueOf(param_activeSudoRoot);
            }
            if(StringUtils.isNotBlank(param_activeSuRoot)) {
                activeSuRoot = Boolean.valueOf(param_activeSuRoot);
            }
        } catch (Exception e) {
            String errMsg = "The params \"activeSudoRoot\" and(or) \"activeSuRoot\" must be boolean.";
            logger.error(errMsg);
            logger.error(ExceptionUtil.getStackTrace(e));
            return new ResponseModel(ResponseModel.Status.OPERATION_FAILED, errMsg);
        }

        if (StringUtils.isBlank(loginType)
                || StringUtils.isBlank(ip)
                || StringUtils.isBlank(loginUser)
                || StringUtils.isBlank(loginPassword)
                || port < 22
                || StringUtils.isBlank(param_tags)) {
            String errMsg = "Can not fetch enough params.";
            logger.error(errMsg);
            return new ResponseModel(ResponseModel.Status.OPERATION_FAILED,errMsg);
        }

        String[] _tags = param_tags.split(",");
        ArrayList tags = new ArrayList();
        for (String tag : _tags) {
            if (StringUtils.isBlank(tag)) continue;
            tags.add(tag);
        }

        return doIt(machineService.addMachine(loginType, ip, port, loginUser, loginPassword, loginCmd, activeSudoRoot, activeSuRoot, rootPassword, rootCmd, tags));
    }

    @RequestMapping(value = {"/security/getAllMachine.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.POST})
    public ResponseModel getAllMachine() {

        List<MachineEntity> machines = machineService.getCurrentUserAllMachine();
        JSONArray result = new JSONArray();
        for (MachineEntity machine : machines) {
            List<TagEntity> tags = machine.getTags();
            String str_tags = "";
            for (TagEntity tag : tags) {
                str_tags += tag.getName() + ",";
            }
            if (str_tags.length() > 0) {
                str_tags = str_tags.substring(0, str_tags.length() - 1);
            }

            JSONObject json = new JSONObject();
            json.put("ip", machine.getIp());
            json.put("sshPort", machine.getSshPort());
            json.put("loginUser", machine.getLoginUser());
            json.put("tags", str_tags);
            result.add(json);
        }
        return new ResponseModel(result);
    }
}
