package com.skyline.service.devops.service;

import com.skyline.platform.core.entity.User;
import com.skyline.platform.core.service.UserService;
import com.skyline.service.devops.dao.MachineDao;
import com.skyline.service.devops.dao.TagDao;
import com.skyline.service.devops.entity.MachineEntity;
import com.skyline.service.devops.entity.TagEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MachineService {
    @Autowired
    private MachineDao machineDao;
    @Autowired
    private TagDao tagDao;
    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(MachineService.class);

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
                           ArrayList<String> tags) {

        //获取登陆用户信息
        User user = userService.getCurrentUser();

        //检查机器是否已经存在
        List<MachineEntity> machineEntitys = machineDao.findByIp(ip);
        boolean isAlreadExist = false;
        if (machineEntitys != null && machineEntitys.size() > 0) {
            for (int i = 0; i< machineEntitys.size(); i++) {
                MachineEntity machine = machineEntitys.get(i);
                List<TagEntity> tagEntities = machine.getTags();
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
                port,
                loginType,
                logingUser,
                loginPassword,
                loginCmd,
                activeSudoRoot,
                activeSuRoot,
                rootPassword,
                rootCmd,
                user);
        machineDao.save(machine);
        for (String tag : tags) {
            TagEntity tagEntity = new TagEntity(tag, machine);
            tagDao.save(tagEntity);
        }
        return true;
    }

    @Transactional
    public List<MachineEntity> getAllMachine() {
        return machineDao.findAll();
    }

    @Transactional
    public List<MachineEntity> getCurrentUserAllMachine() {
        User user = userService.getCurrentUser();
        return machineDao.findByUser(user);
    }
}
