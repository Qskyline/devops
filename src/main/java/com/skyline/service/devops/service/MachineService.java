package com.skyline.service.devops.service;

import com.skyline.platform.core.dao.UserDao;
import com.skyline.platform.core.entity.User;
import com.skyline.platform.core.service.UserService;
import com.skyline.service.devops.dao.MachineDao;
import com.skyline.service.devops.dao.TagDao;
import com.skyline.service.devops.entity.MachineEntity;
import com.skyline.service.devops.entity.MachineInfoDecrypt;
import com.skyline.service.devops.entity.TagEntity;
import com.skyline.util.SecurityUtil;
import com.skyline.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
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
                if ((tagEntities == null || tagEntities.size() == 0) && (tags == null || tags.size() == 0)) {
                    isAlreadExist = true;
                } else if (tags != null && tagEntities != null && tagEntities.size() == tags.size()) {
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
        MachineInfoDecrypt machine = new MachineInfoDecrypt(
                ip,
                String.valueOf(port),
                loginType,
                logingUser,
                loginPassword,
                user);
        machine.setLoginUserCmd(loginCmd);
        machine.setIsActiveSudoRoot(String.valueOf(activeSudoRoot));
        machine.setIsActiveSuRoot(String.valueOf(activeSuRoot));
        machine.setRootPassword(rootPassword);
        machine.setRootCmd(rootCmd);
        machine.setDescription(desc);
        machine.setStatus("normal");

        MachineEntity machineEntity = lockMachineInfo(machine, desKey);
        if (machineEntity == null) {
            logger.error("Lock machine info failed.");
            return false;
        }

        machineDao.save(machineEntity);
        if (tags != null && tags.size() > 0) {
            for (String tag : tags) {
                TagEntity tagEntity = new TagEntity(tag, SecurityUtil.desEncrpt(machineEntity.getId(), desKey));
                tagDao.save(tagEntity);
            }
        }
        return true;
    }

    public List<MachineInfoDecrypt> getAllMachine() {
        List<MachineEntity> machines = machineDao.findAll();
        return parseMachineInfo(machines, desKey);
    }

    public List<MachineInfoDecrypt> getCurrentUserAllMachine() {
        User user = userService.getCurrentUser();
        List<MachineInfoDecrypt> machines = parseMachineInfo(machineDao.findAll(), desKey);
        ArrayList<MachineInfoDecrypt> result = new ArrayList<>();
        for (MachineInfoDecrypt machine : machines) {
            if (machine.getUser().getId().equals(user.getId())) result.add(machine);
        }
        return result;
    }

    private MachineEntity lockMachineInfo(MachineInfoDecrypt machineInfoDecrypt, String key) {
        MachineEntity machineEntity = new MachineEntity();

        try {
            String rowKey = String.valueOf(new Random().nextDouble());

            machineEntity.setIp(SecurityUtil.desEncrpt(machineInfoDecrypt.getIp(), rowKey));
            machineEntity.setSshPort(SecurityUtil.desEncrpt(machineInfoDecrypt.getSshPort(), rowKey));
            machineEntity.setLoginType(SecurityUtil.desEncrpt(machineInfoDecrypt.getLoginType(), rowKey));
            machineEntity.setLoginUser(SecurityUtil.desEncrpt(machineInfoDecrypt.getLoginUser(), rowKey));
            machineEntity.setLoginPassword(SecurityUtil.desEncrpt(machineInfoDecrypt.getLoginPassword(), rowKey));
            machineEntity.setUserId(SecurityUtil.desEncrpt(machineInfoDecrypt.getUser().getId(), rowKey));

            String defaultValue = machineInfoDecrypt.getLoginUserCmd();
            if (StringUtils.isEmpty(defaultValue)) defaultValue = "q-null";
            machineEntity.setLoginUserCmd(SecurityUtil.desEncrpt(defaultValue, rowKey));

            defaultValue = machineInfoDecrypt.getIsActiveSudoRoot();
            if (StringUtils.isEmpty(defaultValue)) defaultValue = "false";
            machineEntity.setIsActiveSudoRoot(SecurityUtil.desEncrpt(defaultValue, rowKey));

            defaultValue = machineInfoDecrypt.getIsActiveSuRoot();
            if (StringUtils.isEmpty(defaultValue)) defaultValue = "false";
            machineEntity.setIsActiveSuRoot(SecurityUtil.desEncrpt(defaultValue, rowKey));

            defaultValue = machineInfoDecrypt.getRootPassword();
            if (StringUtils.isEmpty(defaultValue)) defaultValue = "q-null";
            machineEntity.setRootPassword(SecurityUtil.desEncrpt(defaultValue, rowKey));

            defaultValue = machineInfoDecrypt.getRootCmd();
            if (StringUtils.isEmpty(defaultValue)) defaultValue = "q-null";
            machineEntity.setRootCmd(SecurityUtil.desEncrpt(defaultValue, rowKey));

            defaultValue = machineInfoDecrypt.getDescription();
            if (StringUtils.isEmpty(defaultValue)) defaultValue = "q-null";
            machineEntity.setDescription(SecurityUtil.desEncrpt(defaultValue, rowKey));

            defaultValue = machineInfoDecrypt.getStatus();
            if (StringUtils.isEmpty(defaultValue)) defaultValue = "normal";
            machineEntity.setStatus(SecurityUtil.desEncrpt(defaultValue, rowKey));

            machineEntity.setRowKey(SecurityUtil.desEncrpt(rowKey, key));

            String fingerprint =
                    machineEntity.getIp()
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
        } catch (Exception e) {
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return null;
        }
        return machineEntity;
    }

    private MachineInfoDecrypt parseMachineInfo(MachineEntity machineEntity, String key) {
        String fingerprint =
                machineEntity.getIp()
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

        MachineInfoDecrypt machineInfoDecrypt = new MachineInfoDecrypt();
        try {
            //校验数据准确性
            String fingerprint_check = SecurityUtil.Md5(SecurityUtil.desEncrpt(fingerprint, key));
            if (!fingerprint_check.equals(machineEntity.getFingerprint())) {
                logger.error("Detective illegal MachineInfo Changing! The machineId is {}", machineEntity.getId());
                return null;
            }

            //解密
            String rowKey = SecurityUtil.desDecrpt(machineEntity.getRowKey(), key);

            machineInfoDecrypt.setIp(SecurityUtil.desDecrpt(machineEntity.getIp(), rowKey));
            machineInfoDecrypt.setSshPort(SecurityUtil.desDecrpt(machineEntity.getSshPort(), rowKey));
            machineInfoDecrypt.setLoginType(SecurityUtil.desDecrpt(machineEntity.getLoginType(), rowKey));
            machineInfoDecrypt.setLoginUser(SecurityUtil.desDecrpt(machineEntity.getLoginUser(), rowKey));
            machineInfoDecrypt.setLoginPassword(SecurityUtil.desDecrpt(machineEntity.getLoginPassword(), rowKey));
            String userId = SecurityUtil.desDecrpt(machineEntity.getUserId(), rowKey);
            Optional<User> user = userDao.findById(userId);
            if (!user.isPresent()) {
                throw new Exception("Can not fetch the Machine User");
            }
            machineInfoDecrypt.setUser(user.get());

            String loginUserCmd = SecurityUtil.desDecrpt(machineEntity.getLoginUserCmd(), rowKey);
            if (loginUserCmd.equals("q-null")) loginUserCmd = null;
            machineInfoDecrypt.setLoginUserCmd(loginUserCmd);

            machineInfoDecrypt.setIsActiveSudoRoot(SecurityUtil.desDecrpt(machineEntity.getIsActiveSudoRoot(), rowKey));
            machineInfoDecrypt.setIsActiveSuRoot(SecurityUtil.desDecrpt(machineEntity.getIsActiveSuRoot(), rowKey));

            String rootPassword = SecurityUtil.desDecrpt(machineEntity.getRootPassword(), rowKey);
            if (rootPassword.equals("q-null")) rootPassword = null;
            machineInfoDecrypt.setRootPassword(rootPassword);

            String rootCmd = SecurityUtil.desDecrpt(machineEntity.getRootCmd(), rowKey);
            if (rootCmd.equals("q-null")) rootCmd = null;
            machineInfoDecrypt.setRootCmd(rootCmd);

            String description = SecurityUtil.desDecrpt(machineEntity.getDescription(), rowKey);
            if (description.equals("q-null")) description = null;
            machineInfoDecrypt.setDescription(description);

            machineInfoDecrypt.setStatus(SecurityUtil.desDecrpt(machineEntity.getStatus(), rowKey));

            String machineId = SecurityUtil.desEncrpt(machineEntity.getId(), key);
            List<TagEntity> tagEntities =tagDao.findByMachineId(machineId);
            String tags = "";
            if (tagEntities != null && tagEntities.size() > 0) {
                for (TagEntity tag : tagEntities) {
                    tags += (tag.getName() + ",");
                }
            }
            if (!StringUtils.isEmpty(tags)) {
                tags = tags.substring(0, tags.length() - 1);
            } else {
                tags = null;
            }
            machineInfoDecrypt.setTags(tags);
        } catch (Exception e) {
            logger.error(StringUtil.getExceptionStackTraceMessage(e));
            return null;
        }

       return machineInfoDecrypt;
    }

    private List<MachineInfoDecrypt> parseMachineInfo(List<MachineEntity> machines, String key) {
        ArrayList<MachineInfoDecrypt> result = new ArrayList<>();
        for (MachineEntity machine : machines) {
            MachineInfoDecrypt machineInfoDecrypt = parseMachineInfo(machine, key);
            if (machineInfoDecrypt != null) result.add(machineInfoDecrypt);
        }
        return result;
    }

    public void importMachineInfoFromKeypass(String url, String userId) throws DocumentException {
        ArrayList<String> groupNames = new ArrayList<>();
        groupNames.add("kingdee-product");
        groupNames.add("shenzhen-dmz");
        groupNames.add("neiwang");
        groupNames.add("huaweiyun");

        ArrayList<MachineEntity> machineEntities = new ArrayList<>();

        SAXReader reader = new SAXReader();
        Document doc = reader.read(url);
        List<Element> groups = doc.getRootElement().element("Root").element("Group").elements("Group");
        for (Element group : groups) {
            for (String groupName : groupNames) {
                if (group.element("Name").getText().equals(groupName)) {
                    System.out.println("====================" + group.element("Name").getText() + "====================");
                    List<Element> entrys = group.elements("Entry");
                    for (Element e : entrys) {
                        List<Element> temp = e.elements("String");
                        String desc = null;
                        String ip = null;
                        String password = null;
                        String port = null;
                        String user = null;
                        for (Element t : temp) {
                            String key = t.element("Key").getText();
                            if (key.equals("Notes")) {
                                desc = t.element("Value").getText();
                            } else if (key.equals("Password")) {
                                password = t.element("Value").getText();
                            } else if (key.equals("Title")) {
                                ip = t.element("Value").getText();
                            } else if (key.equals("URL")) {
                                port = t.element("Value").getText();
                            } else if (key.equals("UserName")) {
                                user = t.element("Value").getText();
                            }
                        }
                        if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(password) || StringUtils.isEmpty(port) || StringUtils.isEmpty(user)) continue;

                    }
                }
            }
        }
    }
}
