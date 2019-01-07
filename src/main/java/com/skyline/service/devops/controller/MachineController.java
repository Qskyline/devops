package com.skyline.service.devops.controller;

import com.skyline.platform.core.controller.BaseController;
import com.skyline.platform.core.entity.User;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@RestController
public class MachineController extends BaseController {
    @Autowired
    MachineService machineService;
    @Autowired
    UserService userService;

    Logger logger = LoggerFactory.getLogger(MachineController.class);

    @RequestMapping(value = {"/security/editMachine.do"}, produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.POST})
    public ResponseModel editMachine(@RequestBody JSONObject args) {
        //判断新增还是修改
        String id = (String) args.get("id");

        //必填
        String loginType = (String) args.get("loginType");
        String ip = (String) args.get("ip");
        String port = String.valueOf(args.get("loginPort"));
        String loginUser = (String) args.get("loginUser");
        String loginPassword = (String) args.get("loginPassword");

        //选填
        String tags = (String) args.get("tags");
        String desc = (String) args.get("desc");

        //admin选填
        String loginCmd = (String) args.get("loginCmd");
        String activeSudoRoot = (String) args.get("activeSudoRoot");
        String activeSuRoot = (String) args.get("activeSuRoot");
        String rootPassword = (String) args.get("rootPassword");
        String rootCmd = (String) args.get("rootCmd");

        User user = userService.getCurrentUser();

        MachineInfoDecrypt machine = new MachineInfoDecrypt(
                id,
                loginType,
                ip,
                port,
                loginUser,
                loginPassword,
                user,
                "normal",
                tags,
                desc,
                loginCmd,
                activeSudoRoot,
                activeSuRoot,
                rootPassword,
                rootCmd);

        try {
            if (StringUtils.isEmpty(id)) {
                machineService.addMachine(machine);
            } else {
                machineService.changeMachine(machine);
            }
            return new ResponseModel("success");
        } catch (Exception e) {
            String error_msg = StringUtil.getExceptionStackTraceMessage(e);
            logger.error(error_msg);
            return new ResponseModel(ResponseStatus.OPERATION_ERROR, e.getMessage());
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
                json.put("id", machine.getId());
                json.put("loginType", machine.getLoginType());
                json.put("ip", machine.getIp());
                json.put("sshPort", machine.getSshPort());
                json.put("loginUser", machine.getLoginUser());
                json.put("password", machine.getLoginPassword());
                json.put("tags", machine.getTags());
                json.put("desc", machine.getDescription());
                if (isAdminUser) {
                    json.put("belong", machine.getUser().getUsername());
                    json.put("loginCmd", machine.getLoginUserCmd());
                    json.put("isActiveSudoRoot", machine.getIsActiveSudoRoot());
                    json.put("isActiveSuRoot", machine.getIsActiveSuRoot());
                    json.put("rootPassword", machine.getRootPassword());
                    json.put("rootCmd", machine.getRootCmd());
                }
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

    @RequestMapping(value = {"/security/importMachine.do"}, method = {RequestMethod.POST})
    public ResponseModel importMachine(@RequestParam("file") MultipartFile file) {
        String keypassFilePath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + file.getOriginalFilename();
        String keypassXmlUrl = "file://" + keypassFilePath;
        try {
            OutputStream os=new FileOutputStream(keypassFilePath);
            InputStream is=file.getInputStream();

            byte[] buffer = new byte[5120];
            int len;
            while((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
           logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR, "FileNotFoundException");
        } catch (IOException e) {
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR, "IOException");
        }
        try {
            machineService.importMachineInfoFromKeypass(keypassXmlUrl);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return new ResponseModel(ResponseStatus.OPERATION_ERROR, e.getMessage());
        }
        return new ResponseModel("success");
    }
}
