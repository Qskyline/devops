package com.skyline.service.devops.dao;

import com.skyline.service.devops.entity.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineDao extends JpaRepository<MachineEntity, String> {
    List<MachineEntity> findByIp(String ip);
}
