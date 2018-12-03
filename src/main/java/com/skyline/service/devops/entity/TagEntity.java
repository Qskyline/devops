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
    private MachineEntity machine;

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

    @ManyToOne(targetEntity=MachineEntity.class, fetch=FetchType.LAZY)
    @JoinColumn(name="machine_id", referencedColumnName="id", nullable=false, foreignKey=@ForeignKey(name="fk_machine_machineTag"))
    public MachineEntity getMachine() {
        return machine;
    }
    public void setMachine(MachineEntity machine) {
        this.machine = machine;
    }
}
