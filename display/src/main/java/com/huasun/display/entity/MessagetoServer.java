package com.huasun.display.entity;

/**
 * author:songwenming
 * Date:2020/3/5
 * Description:
 */
public class MessagetoServer {
    private int code;//0:人完成射击，1：设备正常，2：
    private int group_index;
    private String traineeId;
    private int target_index;
    private int trainee_status;
    private int device_status;
    public int getGroup_index() {
        return group_index;
    }

    public void setGroup_index(int group_index) {
        this.group_index = group_index;
    }

    public int getTarget_index() {
        return target_index;
    }

    public void setTarget_index(int target_index) {
        this.target_index = target_index;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getTrainee_status() {
        return trainee_status;
    }

    public void setTrainee_status(int trainee_status) {
        this.trainee_status = trainee_status;
    }

    public int getDevice_status() {
        return device_status;
    }

    public void setDevice_status(int device_status) {
        this.device_status = device_status;
    }

    public String getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(String traineeId) {
        this.traineeId = traineeId;
    }
}
