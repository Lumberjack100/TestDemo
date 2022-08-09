package com.shmedo.core.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/8/4 <br/>
 * 描述：     调用蒲公英 API 2.0 检测更新接口返回数据实体类型
 */
public class CheckSoftModel {

    private int buildBuildVersion;//蒲公英生成的用于区分历史版本的build号
    private String forceUpdateVersion;//强制更新版本号（未设置强置更新默认为空）
    private String forceUpdateVersionNo;//强制更新的版本编号
    private boolean needForceUpdate;//	是否强制更新
    private boolean buildHaveNewVersion;//是否有新版本
    private String downloadURL;//应用安装地址
    //上传包的版本编号，默认为1 (即编译的版本号，一般来说，编译一次会
     //  变动一次这个版本号, 在 Android 上叫 Version Code。对于 iOS 来说，是字符串类型；对于 Android 来
     //  说是一个整数。例如：1001，28等。)
    private String buildVersionNo;
    private String buildVersion;//版本号, 默认为1.0 (是应用向用户宣传时候用到的标识，例如：1.1、8.2.1等。)
    private String buildShortcutUrl;//	应用短链接
    private String buildUpdateDescription;//应用更新说明

    public CheckSoftModel() {
    }

    public int getBuildBuildVersion() {
        return buildBuildVersion;
    }

    public void setBuildBuildVersion(int buildBuildVersion) {
        this.buildBuildVersion = buildBuildVersion;
    }

    public String getForceUpdateVersion() {
        return forceUpdateVersion;
    }

    public void setForceUpdateVersion(String forceUpdateVersion) {
        this.forceUpdateVersion = forceUpdateVersion;
    }

    public String getForceUpdateVersionNo() {
        return forceUpdateVersionNo;
    }

    public void setForceUpdateVersionNo(String forceUpdateVersionNo) {
        this.forceUpdateVersionNo = forceUpdateVersionNo;
    }

    public boolean isNeedForceUpdate() {
        return needForceUpdate;
    }

    public void setNeedForceUpdate(boolean needForceUpdate) {
        this.needForceUpdate = needForceUpdate;
    }

    public boolean isBuildHaveNewVersion() {
        return buildHaveNewVersion;
    }

    public void setBuildHaveNewVersion(boolean buildHaveNewVersion) {
        this.buildHaveNewVersion = buildHaveNewVersion;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getBuildVersionNo() {
        return buildVersionNo;
    }

    public void setBuildVersionNo(String buildVersionNo) {
        this.buildVersionNo = buildVersionNo;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public String getBuildShortcutUrl() {
        return buildShortcutUrl;
    }

    public void setBuildShortcutUrl(String buildShortcutUrl) {
        this.buildShortcutUrl = buildShortcutUrl;
    }

    public String getBuildUpdateDescription() {
        return buildUpdateDescription;
    }

    public void setBuildUpdateDescription(String buildUpdateDescription) {
        this.buildUpdateDescription = buildUpdateDescription;
    }
}
