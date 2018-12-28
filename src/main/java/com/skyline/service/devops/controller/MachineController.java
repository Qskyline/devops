package com.skyline.service.devops.controller;

import com.skyline.platform.core.controller.BaseController;
import com.skyline.platform.core.model.ResponseModel;
import com.skyline.platform.core.model.ResponseStatus;
import com.skyline.platform.core.service.UserService;
import com.skyline.service.devops.entity.MachineEntity;
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

import java.util.ArrayList;
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
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR_PARAMS, errMsg);
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
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR_PARAMS, errMsg);
        }

        if (StringUtils.isBlank(loginType)
                || StringUtils.isBlank(ip)
                || StringUtils.isBlank(loginUser)
                || StringUtils.isBlank(loginPassword)
                || port < 22) {
            String errMsg = "Can not fetch enough params.";
            logger.error(errMsg);
            return new ResponseModel(ResponseStatus.OPERATION_ERROR_PARAMS,errMsg);
        }

        ArrayList<String> tags;
        if (StringUtils.isEmpty(param_tags)) {
            tags = null;
        } else {
            String[] _tags = param_tags.split(",");
            tags = new ArrayList();
            for (String tag : _tags) {
                if (StringUtils.isEmpty(tag)) continue;
                tags.add(tag);
            }
        }

        try {
            return doIt(machineService.addMachine(loginType, ip, port, loginUser, loginPassword, loginCmd, activeSudoRoot, activeSuRoot, rootPassword, rootCmd, "", tags));
        } catch (Exception e) {
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR);
        }
    }

    @RequestMapping(value = {"/security/getAllMachine.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.POST})
    public ResponseModel getAllMachine() {
        boolean isAdminUser = userService.hasRole("admin");
        List<MachineEntity> machines;
        JSONArray result = new JSONArray();
        try {

            if (isAdminUser) machines = machineService.getAllMachine();
            else machines = machineService.getCurrentUserAllMachine();
            for (MachineEntity machine : machines) {
                List<String> tags = machineService.getMachineTagNames(machine.getId());
                String str_tags = "";
                if (tags != null && tags.size() > 0) {
                    for (String tag : tags) {
                        str_tags += tag + ",";
                    }
                }
                if (str_tags.length() > 0) {
                    str_tags = str_tags.substring(0, str_tags.length() - 1);
                }

                JSONObject json = new JSONObject();
                json.put("ip", machine.getIp());
                json.put("sshPort", machine.getSshPort());
                json.put("loginUser", machine.getLoginUser());
                json.put("tags", str_tags);
                if (isAdminUser) json.put("belong", machineService.getMachineUser(machine.getId()));
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
        String[] tags = new String[] {"redis", "zookeeper", "mysql", "mesos", "kbs", "rabbitmq", "marathon", "etcd", "elasticsearch", "kafka", "zipkin", "logstash"};
        return new ResponseModel(tags);
    }
}
