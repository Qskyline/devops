package com.skyline.service.devops.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "devops_machine")
public class MachineEntity {
    //必选
    private String id;
    private String ip;
    private String sshPort;
    private String loginType;
    private String loginUser;
    private String loginPassword;
    private String userId;

    //可选
    private String loginUserCmd;
    private String isActiveSudoRoot;
    private String isActiveSuRoot;
    private String rootPassword;
    private String rootCmd;
    private String description;
    private String status;

    //加密使用
    private String rowKey;
    private String fingerprint;

    public MachineEntity(String ip,
                         String sshPort,
                         String loginType,
                         String loginUser,
                         String loginPassword,
                         String userId) {
        this.ip = ip;
        this.sshPort = sshPort;
        this.loginType = loginType;
        this.loginUser = loginUser;
        this.loginPassword = loginPassword;
        this.userId = userId;
    }

    public MachineEntity() {
    }

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

    @Column(name = "ip", length = 200, nullable = false)
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    @Column(name = "ssh_port", length = 200, nullable = false)
    public String getSshPort() {
        return sshPort;
    }
    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
    }

    @Column(name = "login_type", length = 200, nullable = false)
    public String getLoginType() {
        return loginType;
    }
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    @Column(name = "login_user", length = 500, nullable = false)
    public String getLoginUser() {
        return loginUser;
    }
    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    @Column(name = "login_password", length = 500, nullable = false)
    public String getLoginPassword() {
        return loginPassword;
    }
    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    @Column(name = "login_cmd", length = 2000, nullable = false)
    public String getLoginUserCmd() {
        return loginUserCmd;
    }
    public void setLoginUserCmd(String loginUserCmd) {
        this.loginUserCmd = loginUserCmd;
    }

    @Column(name = "active_sudo_root", length = 100, nullable = false)
    public String getIsActiveSudoRoot() {
        return isActiveSudoRoot;
    }
    public void setIsActiveSudoRoot(String isActiveSudoRoot) {
        this.isActiveSudoRoot = isActiveSudoRoot;
    }

    @Column(name = "active_su_root", length = 100, nullable = false)
    public String getIsActiveSuRoot() {
        return isActiveSuRoot;
    }
    public void setIsActiveSuRoot(String isActiveSuRoot) {
        this.isActiveSuRoot = isActiveSuRoot;
    }

    @Column(name = "root_password", length = 500, nullable = false)
    public String getRootPassword() {
        return rootPassword;
    }
    public void setRootPassword(String rootPassword) {
        this.rootPassword = rootPassword;
    }

    @Column(name = "root_cmd", length = 2000, nullable = false)
    public String getRootCmd() {
        return rootCmd;
    }
    public void setRootCmd(String rootCmd) {
        this.rootCmd = rootCmd;
    }

    @Column(name = "description", length = 2000, nullable = false)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "status", length = 100, nullable = false)
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "row_key", length = 500, nullable = false)
    public String getRowKey() {
        return rowKey;
    }
    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    @Column(name = "fingerprint", length = 6000, nullable = false)
    public String getFingerprint() {
        return fingerprint;
    }
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Column(name = "user_id", length = 500, nullable = false)
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}