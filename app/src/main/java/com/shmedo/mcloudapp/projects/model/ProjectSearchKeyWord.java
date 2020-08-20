package com.shmedo.mcloudapp.projects.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/13 <br/>
 * 描述：     项目搜索关键字历史记录实体类
 */
@Entity
public class ProjectSearchKeyWord {
    @Id
    private Long id;//添加在 sqllite 中作为主键
    private int userId;//添加在 sqllite 中，标识用户

    @Unique
    private String keyWord;//项目名称

    public ProjectSearchKeyWord(int userId, String keyWord) {
        this.userId = userId;
        this.keyWord = keyWord;
    }

    @Generated(hash = 1299851651)
    public ProjectSearchKeyWord(Long id, int userId, String keyWord) {
        this.id = id;
        this.userId = userId;
        this.keyWord = keyWord;
    }

    @Generated(hash = 345908594)
    public ProjectSearchKeyWord() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
