package com.bcsb.rabbitmq.entity;

/**
 * author:songwenming
 * Date:2019/10/20
 * Description:
 */
public class UserDetail {
    int userId;//用户编号
    String name;   //姓名
    String department;//部职别
    String password;//密码
    String shooting_gun;//射击枪械
    String photo_path;//照片存储的路径
    int bullet_count;//子弹数
    String target_number;//靶位号
    String group_number;//编组好

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getShooting_gun() {
        return shooting_gun;
    }

    public void setShooting_gun(String shooting_gun) {
        this.shooting_gun = shooting_gun;
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public void setPhoto_path(String photo_path) {
        this.photo_path = photo_path;
    }

    public int getBullet_count() {
        return bullet_count;
    }

    public void setBullet_count(int bullet_count) {
        this.bullet_count = bullet_count;
    }

    public String getTarget_number() {
        return target_number;
    }

    public void setTarget_number(String target_number) {
        this.target_number = target_number;
    }

    public String getGroup_number() {
        return group_number;
    }

    public void setGroup_number(String group_number) {
        this.group_number = group_number;
    }
}
