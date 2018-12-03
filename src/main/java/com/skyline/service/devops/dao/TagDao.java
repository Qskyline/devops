package com.skyline.service.devops.dao;

import com.skyline.service.devops.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagDao extends JpaRepository<TagEntity, String> {

}
