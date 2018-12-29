package com.skyline.service.devops.entity;

import com.skyline.platform.core.entity.User;

/**
 * @ClassName MachineInfoDecrypt
 * @Description TODO
 * @Author skyline
 * @Date 2018/12/28 10:43
 * Version 1.0
 **/
public class MachineInfoDecrypt {
    //不可空
    private String id;
    private String loginType;
    private String ip;
    private String sshPort;
    private String loginUser;
    private String loginPassword;
    User user;
    private String status;

    //可空
    String tags;
    private String description;

    //可空,admin专属
    private String loginUserCmd;
    private String isActiveSudoRoot;
    private String isActiveSuRoot;
    private String rootPassword;
    private String rootCmd;

    public MachineInfoDecrypt(String id, String loginType, String ip, String sshPort, String loginUser, String loginPassword, User user, String status, String tags, String description, String loginUserCmd, String isActiveSudoRoot, String isActiveSuRoot, String rootPassword, String rootCmd) {
        this.id = id;
        this.loginType = loginType;
        this.ip = ip;
        this.sshPort = sshPort;
        this.loginUser = loginUser;
        this.loginPassword = loginPassword;
        this.user = user;
        this.status = status;
        this.tags = tags;
        this.description = description;
        this.loginUserCmd = loginUserCmd;
        this.isActiveSudoRoot = isActiveSudoRoot;
        this.isActiveSuRoot = isActiveSuRoot;
        this.rootPassword = rootPassword;
        this.rootCmd = rootCmd;
    }

    public MachineInfoDecrypt(String loginType, String ip, String sshPort, String loginUser, String loginPassword, User user, String status, String tags, String description) {
        this.loginType = loginType;
        this.ip = ip;
        this.sshPort = sshPort;
        this.loginUser = loginUser;
        this.loginPassword = loginPassword;
        this.user = user;
        this.status = status;
        this.tags = tags;
        this.description = description;
    }

    public MachineInfoDecrypt() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSshPort() {
        return sshPort;
    }

    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLoginUserCmd() {
        return loginUserCmd;
    }

    public void setLoginUserCmd(String loginUserCmd) {
        this.loginUserCmd = loginUserCmd;
    }

    public String getIsActiveSudoRoot() {
        return isActiveSudoRoot;
    }

    public void setIsActiveSudoRoot(String isActiveSudoRoot) {
        this.isActiveSudoRoot = isActiveSudoRoot;
    }

    public String getIsActiveSuRoot() {
        return isActiveSuRoot;
    }

    public void setIsActiveSuRoot(String isActiveSuRoot) {
        this.isActiveSuRoot = isActiveSuRoot;
    }

    public String getRootPassword() {
        return rootPassword;
    }

    public void setRootPassword(String rootPassword) {
        this.rootPassword = rootPassword;
    }

    public String getRootCmd() {
        return rootCmd;
    }

    public void setRootCmd(String rootCmd) {
        this.rootCmd = rootCmd;
    }
}
