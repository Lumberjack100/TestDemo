package com.shmedo.mcloudapp.user.model;

import java.io.Serializable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/7/7 <br/>
 * 描述：     公司信息
 */
public class CompanyInfo implements Serializable {
    /**
     * id : 1
     * shortName : 上海米度
     * fullName : 上海米度测控科技有限公司
     * parentID : null
     * desc : 上海米度测控科技有限公司成立于2012年2月3日，是一家集GNSS产品研发、生产、销售、服务于一体的综合性高新技术企业。主要业务有高精度GNSS定位产品生产与销售，智能安全监测系统及整体解决方案提供，北斗卫星通讯运营服务，数据平台建设与运营服务。
     * address : 联航路1188弄32号楼5层
     * phone : 021-33923627
     * legalPerson : 李玮煜
     * scale : 2
     * industry : 互联网科技
     * nature : 0
     * webSite : http://www.shmedo.cn
     * level : 0
     * createUserID : 1
     * createTime : 2018-01-29 00:00:00
     * updateUserID : 104
     * updateTime : 2020-05-04 09:59:39
     * hasChild : true
     */
    private int id;
    private String shortName;
    private String fullName;
    private int parentID;
    private String desc;
    private int level;
    private int createUserID;
    private String createTime;
    private int updateUserID;
    private String updateTime;
    private boolean hasChild;
    private boolean delete;
    private String address;
    private String phone;
    private String legalPerson;
    private String scale;
    private String industry;
    private String nature;
    private String webSite;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLegalPerson() {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson) {
        this.legalPerson = legalPerson;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCreateUserID() {
        return createUserID;
    }

    public void setCreateUserID(int createUserID) {
        this.createUserID = createUserID;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getUpdateUserID() {
        return updateUserID;
    }

    public void setUpdateUserID(int updateUserID) {
        this.updateUserID = updateUserID;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }
}
