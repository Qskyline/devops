package com.skyline.service.devops.entity;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.List;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "devops_machine")
public class MachineEntity {
    private String id;
    private String ip;
    private int sshPort;
    private String loginType;
    private String loginUser;
    private String loginPassword;
    private String loginUserCmd;
    private boolean isActiveSudoRoot;
    private boolean isActiveSuRoot;
    private String rootPassword;
    private String rootCmd;
    private List<TagEntity> tags;

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "machine_id")
    @GenericGenerator(name = "machine_id", strategy = "uuid")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "ip", length = 15, nullable = false)
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    @Column(name = "ssh_port", nullable = false)
    public int getSshPort() {
        return sshPort;
    }
    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    @Column(name = "login_type", length = 15, nullable = false)
    public String getLoginType() {
        return loginType;
    }
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    @Column(name = "login_user", length = 30, nullable = false)
    public String getLoginUser() {
        return loginUser;
    }
    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    @Column(name = "login_password", length = 2000, nullable = false)
    public String getLoginPassword() {
        return loginPassword;
    }
    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    @Column(name = "login_cmd", length = 2000)
    public String getLoginUserCmd() {
        return loginUserCmd;
    }
    public void setLoginUserCmd(String loginUserCmd) {
        this.loginUserCmd = loginUserCmd;
    }

    @Column(name = "active_sudo_root")
    @ColumnDefault("false")
    public boolean getIsActiveSudoRoot() {
        return isActiveSudoRoot;
    }
    public void setIsActiveSudoRoot(boolean isActiveSudoRoot) {
        this.isActiveSudoRoot = isActiveSudoRoot;
    }

    @Column(name = "active_su_root")
    @ColumnDefault("false")
    public boolean getIsActiveSuRoot() {
        return isActiveSuRoot;
    }
    public void setIsActiveSuRoot(boolean isActiveSuRoot) {
        this.isActiveSuRoot = isActiveSuRoot;
    }

    @Column(name = "root_password", length = 100)
    public String getRootPassword() {
        return rootPassword;
    }
    public void setRootPassword(String rootPassword) {
        this.rootPassword = rootPassword;
    }

    @Column(name = "root_cmd", length = 2000)
    public String getRootCmd() {
        return rootCmd;
    }
    public void setRootCmd(String rootCmd) {
        this.rootCmd = rootCmd;
    }

    @OneToMany(targetEntity=TagEntity.class, cascade=CascadeType.ALL, mappedBy="machine", fetch=FetchType.LAZY)
    public List<TagEntity> getTags() {
        return tags;
    }
    public void setTags(List<TagEntity> tags) {
        this.tags = tags;
    }
}