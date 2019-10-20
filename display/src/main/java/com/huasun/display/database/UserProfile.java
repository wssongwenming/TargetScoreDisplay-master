package com.huasun.display.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * author:songwenming
 * Date:2019/9/24
 * Description:
 */
@Entity(nameInDb = "user_profile")
public class UserProfile implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    private long userId=0;
    private String name=null;//姓名
    private String department=null;//部职别
    private String shooting_gun=null;//射击枪械
    private  int bullet_count=0;//子弹数量
    private String target_number=null;//靶位序号
    private String group_number=null;//组序号
    @Generated(hash = 845870119)
    public UserProfile(long userId, String name, String department,
            String shooting_gun, int bullet_count, String target_number,
            String group_number) {
        this.userId = userId;
        this.name = name;
        this.department = department;
        this.shooting_gun = shooting_gun;
        this.bullet_count = bullet_count;
        this.target_number = target_number;
        this.group_number = group_number;
    }
    @Generated(hash = 968487393)
    public UserProfile() {
    }
    public long getUserId() {
        return this.userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDepartment() {
        return this.department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getShooting_gun() {
        return this.shooting_gun;
    }
    public void setShooting_gun(String shooting_gun) {
        this.shooting_gun = shooting_gun;
    }
    public int getBullet_count() {
        return this.bullet_count;
    }
    public void setBullet_count(int bullet_count) {
        this.bullet_count = bullet_count;
    }
    public String getTarget_number() {
        return this.target_number;
    }
    public void setTarget_number(String target_number) {
        this.target_number = target_number;
    }
    public String getGroup_number() {
        return this.group_number;
    }
    public void setGroup_number(String group_number) {
        this.group_number = group_number;
    }
   
}
