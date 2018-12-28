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
    //必选
    private String ip;
    private String sshPort;
    private String loginType;
    private String loginUser;
    private String loginPassword;

    User user;

    //可选
    private String loginUserCmd;
    private String isActiveSudoRoot;
    private String isActiveSuRoot;
    private String rootPassword;
    private String rootCmd;
    private String description;
    private String status;

    String tags;

    public MachineInfoDecrypt(String ip, String sshPort, String loginType, String loginUser, String loginPassword, User user) {
        this.ip = ip;
        this.sshPort = sshPort;
        this.loginType = loginType;
        this.loginUser = loginUser;
        this.loginPassword = loginPassword;
        this.user = user;
    }

    public MachineInfoDecrypt() {
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

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
