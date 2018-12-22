package com.skyline.service.devops.service;

import com.skyline.service.devops.dao.TagDao;
import com.skyline.service.devops.entity.TagEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TagService
 * @Description TODO
 * @Author skyline
 * @Date 2018/12/22 17:18
 * Version 1.0
 **/
@Service
public class TagService {
    @Autowired
    TagDao tagDao;

    public List<String> getTagNameByMachineId(String machineId) {
        List<TagEntity> tagEntities =tagDao.findByMachineId(machineId);
        ArrayList<String> result = new ArrayList<>();
        for (TagEntity tag : tagEntities) {
            result.add(tag.getName());
        }
        return result;
    }
}
