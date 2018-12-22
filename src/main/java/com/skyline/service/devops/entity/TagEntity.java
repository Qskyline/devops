package com.skyline.service.devops.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "devops_machineTag")
public class TagEntity {
    private String id;
    private String name;
    private String machineId;

    public TagEntity(String name, String machineId) {
        this.name = name;
        this.machineId = machineId;
    }

    public TagEntity() {}

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "machine_tag_id")
    @GenericGenerator(name = "machine_tag_id", strategy = "uuid")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "name", length = 20, nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "machine_id", length = 200, nullable = false)
    public String getMachineId() {
        return machineId;
    }
    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }
}
