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
import java.util.regex.Pattern;

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

        allTags.add("kingdee");
        allTags.add("kingdee-dmz");
        allTags.add("kingdee-office");
        allTags.add("kingdee-inner");
        allTags.add("huawei-cloud");
    }

    @Transactional
    public void addMachine(MachineInfoDecrypt machineInfoDecrypt) throws Exception {
        //过滤非法tags
        String tags = null;
        if (StringUtils.isNotEmpty(machineInfoDecrypt.getTags())) tags = tagsFilter(machineInfoDecrypt.getTags().split(","));
        machineInfoDecrypt.setTags(tags);

        //检查机器是否已经存在
        if (isMachineAlreadyExist(machineInfoDecrypt)) {
            String error = "Machine already exist.";
            logger.error(error);
            throw new Exception(error);
        }

        //新增机器时禁止指定machineId
        if (StringUtils.isNotEmpty(machineInfoDecrypt.getId())) {
            String error = "If you want to add a new machine, the machineId must be empty.";
            logger.error(error);
            throw new Exception(error);
        }

        //保存机器信息
        saveMachine(machineInfoDecrypt, null);
    }

    @Transactional
    public void updateMachine(MachineInfoDecrypt machineInfoDecrypt) throws Exception {
        //获取machineEntity,以证明机器存在
        if (StringUtils.isEmpty(machineInfoDecrypt.getId())) {
            String error_msg = "If you want to update the Machine, please specify the machineId.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }
        Optional<MachineEntity> machineEntity = machineDao.findById(machineInfoDecrypt.getId());
        if (!machineEntity.isPresent()) {
            String error_msg = "Can not find the machine";
            logger.error(error_msg + "(id={})", machineInfoDecrypt.getId());
            throw new Exception(error_msg + "(id=" + machineInfoDecrypt.getId() + ")");
        }

        //过滤非法tags
        String tags = null;
        if (StringUtils.isNotEmpty(machineInfoDecrypt.getTags())) tags = tagsFilter(machineInfoDecrypt.getTags().split(","));
        machineInfoDecrypt.setTags(tags);

        //保存机器信息
        saveMachine(machineInfoDecrypt, machineEntity.get());
    }

    @Transactional
    public void saveMachine(MachineInfoDecrypt machineInfoDecrypt, MachineEntity machineEntity) throws Exception {
        //检查机器信息是否合法
        machineCheck(machineInfoDecrypt);

        //尝试获取machineEntity,以判断是否是更新操作
        MachineEntity machine = machineEntity;
        if (machine == null && StringUtils.isNotEmpty(machineInfoDecrypt.getId())) {
            Optional<MachineEntity> m = machineDao.findById(machineInfoDecrypt.getId());
            if (m.isPresent()) machine = m.get();
        }

        //如果是更新操作则删除原有machine的tags记录
        if (machine != null) tagDao.deleteByMachineId(SecurityUtil.desEncrpt(machine.getId(), desKey));

        //加密
        machine = lockMachineInfo(machineInfoDecrypt, machine, desKey);
        if (machine == null) {
            String error_msg = "Lock machine info failed.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }

        //保存machine
        machineDao.save(machine);

        //保存tags
        String tags = machineInfoDecrypt.getTags();
        if (StringUtils.isNotEmpty(tags)) {
            String[] arr_tags = tags.split(",");
            for (String tag : arr_tags) {
                TagEntity tagEntity = new TagEntity(tag, SecurityUtil.desEncrpt(machine.getId(), desKey));
                tagDao.save(tagEntity);
            }
        }
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

    public List<MachineInfoDecrypt> getMachinesByIp(String ip) {
        List<MachineInfoDecrypt> machines = getAllMachine();
        ArrayList<MachineInfoDecrypt> machineInfoDecrypts = new ArrayList<>();
        for (MachineInfoDecrypt m : machines) {
            if (ip.equals(m.getIp())) machineInfoDecrypts.add(m);
        }
        return machineInfoDecrypts;
    }

    public HashSet<String> getAllTags() {
        return this.allTags;
    }

    private boolean isMachineAlreadyExist(MachineInfoDecrypt machine) {
        List<MachineInfoDecrypt> machineInfoDecrypts = getMachinesByIp(machine.getIp());
        if (machineInfoDecrypts != null && machineInfoDecrypts.size() > 0) {
            for (int i = 0; i< machineInfoDecrypts.size(); i++) {
                if (compareMachine(machine, machineInfoDecrypts.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean compareMachine(MachineInfoDecrypt machine1, MachineInfoDecrypt machine2){
        if (!machine1.getIp().equals(machine2.getIp())) return false;
        String[] machine1_tags = null;
        String[] machine2_tags = null;
        boolean is_machine1_tags_empty = (machine1.getTags() == null || (machine1_tags = machine1.getTags().split(",")).length == 0);
        boolean is_machine2_tags_empty = (machine2.getTags() == null || (machine2_tags = machine2.getTags().split(",")).length == 0);
        if (is_machine1_tags_empty && is_machine2_tags_empty) {
            return true;
        } else if (!is_machine1_tags_empty && !is_machine2_tags_empty && machine1_tags.length == machine2_tags.length) {
            for (String machine1_tag : machine1_tags) {
                boolean isFind = false;
                for (String machine2_tag : machine2_tags) {
                    if (machine1_tag.equals(machine2_tag)) {
                        isFind = true;
                        break;
                    }
                }
                if (!isFind) return false;
            }
            return true;
        } else {
            return false;
        }
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

    private void machineCheck(MachineInfoDecrypt machineInfoDecrypt) throws Exception {
        // must params check
        if (StringUtils.isEmpty(machineInfoDecrypt.getLoginType())
                || StringUtils.isEmpty(machineInfoDecrypt.getIp())
                || StringUtils.isEmpty(machineInfoDecrypt.getLoginUser())
                || StringUtils.isEmpty(machineInfoDecrypt.getLoginPassword())
                || StringUtils.isEmpty(machineInfoDecrypt.getSshPort())) {
            String error_msg = "Can not fetch enough params.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }

        // ip check
        if (!StringUtil.isIp(machineInfoDecrypt.getIp())) {
            String error_msg = "IP checking failed.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }

        //loginType check
        String loginType = machineInfoDecrypt.getLoginType();
        if (!"password".equals(loginType) && !"key".equals(loginType)) {
            String error_msg = "The param \"loginType\" must be \"password\" or \"key\".";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }

        //port check
        String port = machineInfoDecrypt.getSshPort();
        Pattern pattern = Pattern.compile("^[0-9]+$");
        if (!pattern.matcher(port).matches() || port.startsWith("0") || Integer.valueOf(port) < 22) {
            String error_msg = "The \"port\" param must be numeric and great than or equal 22.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }

        //loginUser check
        pattern = Pattern.compile("^[a-z|A-Z|_|\\-]{3,}$");
        if (!pattern.matcher(machineInfoDecrypt.getLoginUser()).matches()) {
            String error_msg = "The \"loginUser\" param check failed.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }

        //activeSudoRoot and activeSuRoot check
        String activeSudoRoot = machineInfoDecrypt.getIsActiveSudoRoot();
        String activeSuRoot = machineInfoDecrypt.getIsActiveSuRoot();
        pattern = Pattern.compile("^(true)|(false)$");
        if (StringUtils.isNotEmpty(activeSudoRoot) && !pattern.matcher(activeSudoRoot).matches()) {
            String error_msg = "The params \"activeSudoRoot\" must be boolean.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }
        if (StringUtils.isNotEmpty(activeSuRoot) && !pattern.matcher(activeSuRoot).matches()) {
            String error_msg = "The params \"activeSuRoot\" must be boolean.";
            logger.error(error_msg);
            throw new Exception(error_msg);
        }
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

    private List<MachineInfoDecrypt> parseMachineInfo(List<MachineEntity> machines, String key) {
        ArrayList<MachineInfoDecrypt> result = new ArrayList<>();
        for (MachineEntity machine : machines) {
            MachineInfoDecrypt machineInfoDecrypt = parseMachineInfo(machine, key);
            if (machineInfoDecrypt != null) result.add(machineInfoDecrypt);
        }
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

            machineInfoDecrypt.setId(machineEntity.getId());
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

    public void importMachineInfoFromKeypass(String url) throws DocumentException {
        User user = userService.getCurrentUser();
        importMachineInfoFromKeypass(url, user);
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
                        if (StringUtils.isNotEmpty(note)) tags = tagsFilter(note.split("[，、,]"));

                        MachineInfoDecrypt machine = new MachineInfoDecrypt(
                                "password",
                                ip,
                                port,
                                user,
                                password,
                                currentUser,
                                "normal",
                                tags,
                                note);

                        if (isMachineAlreadyExist(machine)) continue;
                        try {
                            saveMachine(machine, null);
                            logger.info("Add/Update machine success by keypassFile! MachineIp={}", ip);
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
