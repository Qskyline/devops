package com.skyline.service.devops.dao;

import com.skyline.service.devops.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagDao extends JpaRepository<TagEntity, String> {
    List<TagEntity> findByMachineId(String machineId);
    int deleteByMachineId(String MachineId);
}
