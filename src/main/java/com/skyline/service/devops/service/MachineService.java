package com.skyline.service.devops.service;

import com.skyline.platform.core.dao.UserDao;
import com.skyline.platform.core.entity.User;
import com.skyline.platform.core.service.UserService;
import com.skyline.service.devops.dao.MachineDao;
import com.skyline.service.devops.dao.TagDao;
import com.skyline.service.devops.entity.MachineEntity;
import com.skyline.service.devops.entity.TagEntity;
import com.skyline.util.SecurityUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class MachineService {
    @Autowired
    private MachineDao machineDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private UserService userService;
    @Autowired
    private UserDao userDao;

    private Logger logger = LoggerFactory.getLogger(MachineService.class);
    private String desKey = "123242432443243";

    @Transactional
    public boolean addMachine(String loginType,
                           String ip,
                           int port,
                           String logingUser,
                           String loginPassword,
                           String loginCmd,
                           boolean activeSudoRoot,
                           boolean activeSuRoot,
                           String rootPassword,
                           String rootCmd,
                           String desc,
                           ArrayList<String> tags) throws Exception {

        //获取登陆用户信息
        User user = userService.getCurrentUser();

        //检查机器是否已经存在
        List<MachineEntity> machineEntitys = machineDao.findByIp(ip);
        boolean isAlreadExist = false;
        if (machineEntitys != null && machineEntitys.size() > 0) {
            for (int i = 0; i< machineEntitys.size(); i++) {
                MachineEntity machine = machineEntitys.get(i);
                List<TagEntity> tagEntities = tagDao.findByMachineId(machine.getId());
                if (tagEntities.size() == tags.size()) {
                    boolean flag = false;
                    for (String tag : tags) {
                        boolean isFind = false;
                        for (TagEntity _tag : tagEntities) {
                            if (_tag.getName().equals(tag)) {
                                isFind = true;
                                break;
                            }
                        }
                        if (!isFind) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        isAlreadExist = true;
                        break;
                    }
                }
            }
        }
        if (isAlreadExist) {
            logger.error("Machine already exist.");
            return false;
        }

        //添加机器
        MachineEntity machine = new MachineEntity(
                ip,
                String.valueOf(port),
                loginType,
                logingUser,
                loginPassword,
                user.getId());
        machine.setLoginUserCmd(loginCmd);
        machine.setIsActiveSudoRoot(String.valueOf(activeSudoRoot));
        machine.setIsActiveSuRoot(String.valueOf(activeSuRoot));
        machine.setRootPassword(rootPassword);
        machine.setRootCmd(rootCmd);
        machine.setDescription(desc);
        machine.setStatus("normal");

        try {
            lockMachineInfo(machine, desKey);
        } catch (Exception e) {
            throw new Exception("Lock machine info failed.");
        }
        machineDao.save(machine);
        for (String tag : tags) {
            TagEntity tagEntity = new TagEntity(tag, SecurityUtil.desEncrpt(machine.getId(), desKey));
            tagDao.save(tagEntity);
        }
        return true;
    }

    @Transactional
    public List<MachineEntity> getAllMachine() throws Exception {
        List<MachineEntity> machines = machineDao.findAll();
        return parseMachineInfo(machines, desKey);
    }

    @Transactional
    public List<MachineEntity> getCurrentUserAllMachine() throws Exception {
        User user = userService.getCurrentUser();
        List<MachineEntity> machines = machineDao.findByUserId(SecurityUtil.desDecrpt(user.getId(),desKey));
        return parseMachineInfo(machines, desKey);
    }

    public User getMachineUser(String machineId) throws Exception {
        Optional<MachineEntity> machine = machineDao.findById(machineId);
        if (!machine.isPresent()) return null;
        if (!parseMachineInfo(machine.get(), desKey)) return null;
        String userId = machine.get().getUserId();
        Optional<User> user = userDao.findById(userId);
        if (!user.isPresent()) return null;
        return user.get();
    }

    private void lockMachineInfo(MachineEntity machineEntity, String key) throws Exception {
        String rowKey = String.valueOf(new Random().nextDouble());

        machineEntity.setIp(SecurityUtil.desEncrpt(machineEntity.getIp(), rowKey));
        machineEntity.setSshPort(SecurityUtil.desEncrpt(machineEntity.getSshPort(), rowKey));
        machineEntity.setLoginType(SecurityUtil.desEncrpt(machineEntity.getLoginType(), rowKey));
        machineEntity.setLoginUser(SecurityUtil.desEncrpt(machineEntity.getLoginUser(), rowKey));
        machineEntity.setLoginPassword(SecurityUtil.desEncrpt(machineEntity.getLoginPassword(), rowKey));
        machineEntity.setUserId(SecurityUtil.desEncrpt(machineEntity.getUserId(), rowKey));

        String defaultValue = "q-null";
        if (StringUtils.isEmpty(machineEntity.getLoginUserCmd())) machineEntity.setLoginUserCmd(defaultValue);
        machineEntity.setLoginUserCmd(SecurityUtil.desEncrpt(machineEntity.getLoginUserCmd(), rowKey));

        defaultValue = "false";
        if (StringUtils.isEmpty(machineEntity.getIsActiveSudoRoot())) machineEntity.setIsActiveSudoRoot(defaultValue);
        machineEntity.setIsActiveSudoRoot(SecurityUtil.desEncrpt(machineEntity.getIsActiveSudoRoot(), rowKey));

        defaultValue = "false";
        if (StringUtils.isEmpty(machineEntity.getIsActiveSuRoot())) machineEntity.setIsActiveSuRoot(defaultValue);
        machineEntity.setIsActiveSuRoot(SecurityUtil.desEncrpt(machineEntity.getIsActiveSuRoot(), rowKey));

        defaultValue = "q-null";
        if (StringUtils.isEmpty(machineEntity.getRootPassword())) machineEntity.setRootPassword(defaultValue);
        machineEntity.setRootPassword(SecurityUtil.desEncrpt(machineEntity.getRootPassword(), rowKey));

        defaultValue = "q-null";
        if (StringUtils.isEmpty(machineEntity.getRootCmd())) machineEntity.setRootCmd(defaultValue);
        machineEntity.setRootCmd(SecurityUtil.desEncrpt(machineEntity.getRootCmd(), rowKey));

        defaultValue = "q-null";
        if (StringUtils.isEmpty(machineEntity.getDescription())) machineEntity.setDescription(defaultValue);
        machineEntity.setDescription(SecurityUtil.desEncrpt(machineEntity.getDescription(), rowKey));

        defaultValue = "normal";
        if (StringUtils.isEmpty(machineEntity.getStatus())) machineEntity.setStatus(defaultValue);
        machineEntity.setStatus(SecurityUtil.desEncrpt(machineEntity.getStatus(), rowKey));

        machineEntity.setRowKey(SecurityUtil.desEncrpt(rowKey, key));

        String fingerprint =
                machineEntity.getId()
                + machineEntity.getIp()
                + machineEntity.getSshPort()
                + machineEntity.getLoginType()
                + machineEntity.getLoginUser()
                + machineEntity.getLoginPassword()
                + machineEntity.getUserId()
                + machineEntity.getLoginUserCmd()
                + machineEntity.getIsActiveSudoRoot()
                + machineEntity.getIsActiveSuRoot()
                + machineEntity.getRootPassword()
                + machineEntity.getRootCmd()
                + machineEntity.getDescription()
                + machineEntity.getStatus()
                + machineEntity.getRowKey();

        machineEntity.setFingerprint(SecurityUtil.Md5(SecurityUtil.desEncrpt(fingerprint, key)));
    }

    private boolean parseMachineInfo(MachineEntity machineEntity, String key) throws Exception {
        String fingerprint =
                machineEntity.getId()
                        + machineEntity.getIp()
                        + machineEntity.getSshPort()
                        + machineEntity.getLoginType()
                        + machineEntity.getLoginUser()
                        + machineEntity.getLoginPassword()
                        + machineEntity.getUserId()
                        + machineEntity.getLoginUserCmd()
                        + machineEntity.getIsActiveSudoRoot()
                        + machineEntity.getIsActiveSuRoot()
                        + machineEntity.getRootPassword()
                        + machineEntity.getRootCmd()
                        + machineEntity.getDescription()
                        + machineEntity.getStatus()
                        + machineEntity.getRowKey();

        String fingerprint_check = SecurityUtil.Md5(SecurityUtil.desEncrpt(fingerprint, key));
        if (!fingerprint_check.equals(machineEntity.getFingerprint())) {
            logger.error("Detective illegal MachineInfo Changing! The machineId is {}", machineEntity.getId());
            return false;
        }

        String rowKey = SecurityUtil.desDecrpt(machineEntity.getRowKey(), key);
        machineEntity.setRowKey(rowKey);

        machineEntity.setIp(SecurityUtil.desDecrpt(machineEntity.getIp(), rowKey));
        machineEntity.setSshPort(SecurityUtil.desDecrpt(machineEntity.getSshPort(), rowKey));
        machineEntity.setLoginType(SecurityUtil.desDecrpt(machineEntity.getLoginType(), rowKey));
        machineEntity.setLoginUser(SecurityUtil.desDecrpt(machineEntity.getLoginUser(), rowKey));
        machineEntity.setLoginPassword(SecurityUtil.desDecrpt(machineEntity.getLoginPassword(), rowKey));
        machineEntity.setUserId(SecurityUtil.desDecrpt(machineEntity.getUserId(), rowKey));

        String loginUserCmd = SecurityUtil.desDecrpt(machineEntity.getLoginUserCmd(), rowKey);
        if (loginUserCmd.equals("q-null")) loginUserCmd = null;
        machineEntity.setLoginUserCmd(loginUserCmd);

        machineEntity.setIsActiveSudoRoot(SecurityUtil.desDecrpt(machineEntity.getIsActiveSudoRoot(), rowKey));
        machineEntity.setIsActiveSuRoot(SecurityUtil.desDecrpt(machineEntity.getIsActiveSuRoot(), rowKey));

        String rootPassword = SecurityUtil.desDecrpt(machineEntity.getRootPassword(), rowKey);
        if (rootPassword.equals("q-null")) rootPassword = null;
        machineEntity.setRootPassword(rootPassword);

        String rootCmd = SecurityUtil.desDecrpt(machineEntity.getRootCmd(), rowKey);
        if (rootCmd.equals("q-null")) rootCmd = null;
        machineEntity.setRootCmd(rootCmd);

        String description = SecurityUtil.desDecrpt(machineEntity.getDescription(), rowKey);
        if (description.equals("q-null")) description = null;
        machineEntity.setDescription(description);

       machineEntity.setStatus(SecurityUtil.desDecrpt(machineEntity.getStatus(), rowKey));

       return true;
    }

    private List<MachineEntity> parseMachineInfo(List<MachineEntity> machines, String key) throws Exception {
        ArrayList<MachineEntity> result = new ArrayList<>();
        for (MachineEntity machine : machines) {
            if (parseMachineInfo(machine, key)) result.add(machine);
        }
        return result;
    }
}
