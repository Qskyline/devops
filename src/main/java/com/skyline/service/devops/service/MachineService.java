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

import java.util.*;

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

    private HashSet<String> allTags = new HashSet<>();

    public MachineService() {
        allTags.add("redis");
        allTags.add("zookeeper");
        allTags.add("mysql");
        allTags.add("mesos");
        allTags.add("kbs");
        allTags.add("rabbitmq");
        allTags.add("marathon");
        allTags.add("etcd");
        allTags.add("elasticsearch");
        allTags.add("kafka");
        allTags.add("zipkin");
        allTags.add("logstash");
    }

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
                              String str_tag) throws Exception {

        //获取登陆用户信息
        User user = userService.getCurrentUser();

        //获取tags
        String tags = null;
        if (!StringUtils.isEmpty(str_tag)) tags = tagsFilter(str_tag.split(","));

        //检查机器是否已经存在
        if (isMachineAlreadyExist(ip, tags) != null) {
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
        machine.setTags(tags);
        return changeMachine(machine, null);
    }

    public boolean changeMachine(String machineId,
                                 String loginType,
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
                                 String str_tag) throws Exception {
        Optional<MachineEntity> machineEntity = machineDao.findById(machineId);
        if (!machineEntity.isPresent()) {
            logger.error("Can not find the machine(id={})", machineId);
            return false;
        }

        String tags = null;
        if (!StringUtils.isEmpty(str_tag)) tags = tagsFilter(str_tag.split(","));

        User user = userService.getCurrentUser();
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
        machine.setTags(tags);

        return changeMachine(machine, machineEntity.get());
    }

    private boolean changeMachine(MachineInfoDecrypt machineInfoDecrypt, MachineEntity machineEntity) throws Exception {
        if (machineEntity != null) {
            tagDao.deleteByMachineId(SecurityUtil.desEncrpt(machineEntity.getId(), desKey));
        }
        MachineEntity machine = lockMachineInfo(machineInfoDecrypt, machineEntity, desKey);
        if (machine == null) {
            logger.error("Lock machine info failed.");
            return false;
        }

        machineDao.save(machine);
        String tags = machineInfoDecrypt.getTags();
        if (!StringUtils.isEmpty(tags)) {
            String[] arr_tags = tags.split(",");
            for (String tag : arr_tags) {
                TagEntity tagEntity = new TagEntity(tag, SecurityUtil.desEncrpt(machine.getId(), desKey));
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

    public HashSet<String> getAllTags() {
        return this.allTags;
    }

    private MachineEntity lockMachineInfo(MachineInfoDecrypt machineInfoDecrypt, MachineEntity machineEntity, String key) {
        if (machineEntity == null) machineEntity = new MachineEntity();

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

    private MachineEntity isMachineAlreadyExist(String ip, String str_tags) {
        List<MachineEntity> machineEntitys = machineDao.findByIp(ip);
        MachineEntity result = null;
        boolean isAlreadExist = false;
        if (machineEntitys != null && machineEntitys.size() > 0) {
            for (int i = 0; i< machineEntitys.size(); i++) {
                MachineEntity machine = machineEntitys.get(i);
                List<TagEntity> tagEntities = tagDao.findByMachineId(machine.getId());
                String[] tags = null;
                boolean isDbTagsEmpty = (tagEntities == null || tagEntities.size() == 0);
                boolean isParamTagsEmpty = (str_tags == null || (tags = str_tags.split(",")).length == 0);
                if (isDbTagsEmpty && isParamTagsEmpty) {
                    isAlreadExist = true;
                    result = machine;
                    break;
                } else if (!isDbTagsEmpty && !isParamTagsEmpty && tagEntities.size() == tags.length) {
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
                        result = machine;
                        break;
                    }
                }
            }
        }
        if (isAlreadExist) return result;
        else return null;
    }

    private String tagsFilter(String[] tags) {
        String result = "";
        for (String tag : tags) {
            if (allTags.contains(tag)) result += (tag + ",");
            else continue;
        }
        if (!StringUtils.isEmpty(result)) result = result.substring(0, result.length() - 1);
        if (StringUtils.isEmpty(result)) return null;
        return result;
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
        Optional<User> optionalUser = userDao.findById(userId);
        if (!optionalUser.isPresent()) {
            logger.error("Can not fetch the UserInfo.");
            return;
        }
        User currentUser = optionalUser.get();
        importMachineInfoFromKeypass(url, currentUser);
    }

    public void importMachineInfoFromKeypass(String url, User currentUser) throws DocumentException {
        if (currentUser == null) {
            logger.error("Can not fetch the UserInfo.");
            return;
        }

        ArrayList<String> groupNames = new ArrayList<>();
        groupNames.add("kingdee-product");
        groupNames.add("shenzhen-dmz");
        groupNames.add("neiwang");
        groupNames.add("huaweiyun");

        SAXReader reader = new SAXReader();
        Document doc = reader.read(url);
        List<Element> groups = doc.getRootElement().element("Root").element("Group").elements("Group");
        for (Element group : groups) {
            for (String groupName : groupNames) {
                if (group.element("Name").getText().equals(groupName)) {
                    List<Element> entrys = group.elements("Entry");
                    for (Element e : entrys) {
                        List<Element> temp = e.elements("String");
                        String note = "";
                        String ip = null;
                        String password = null;
                        String port = null;
                        String user = null;
                        for (Element t : temp) {
                            String key = t.element("Key").getText();
                            if (key.equals("Notes")) {
                                note = t.element("Value").getText();
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

                        String tags = null;
                        if (!StringUtils.isEmpty(note)) tags = tagsFilter(note.split("[，、,]"));

                        MachineInfoDecrypt machine = new MachineInfoDecrypt(
                                ip,
                                port,
                                "password",
                                user,
                                password,
                                currentUser);
                        machine.setDescription(note);
                        machine.setTags(tags);

                        MachineEntity machineEntity = isMachineAlreadyExist(ip, tags);
                        try {
                            if (changeMachine(machine, machineEntity)){
                                logger.info("Add/Update machine success by keypassFile! MachineIp={}", ip);
                            } else  {
                                logger.error("Add/Update machine failed by keypassFile! MachineIp={}", ip);
                            }
                        } catch (Exception e1) {
                            logger.error("Add/Update machine failed by keypassFile! MachineIp={}", ip);
                            logger.error(StringUtil.getExceptionStackTraceMessage(e1));
                        }
                    }
                }
            }
        }
    }
}
