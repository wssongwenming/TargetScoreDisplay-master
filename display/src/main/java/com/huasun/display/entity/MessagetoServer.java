package com.huasun.display.entity;

/**
 * author:songwenming
 * Date:2020/3/5
 * Description:
　private int deviceType;//靶机，采集，显靶分别为0，1，2
 private int deviceIndex;
 private int deviceId;
 *
 *
 */
public class MessagetoServer {
    private int code;//0:返回的数据为人员完成射击，1：设备，2：
    private int group_index;
    private String traineeId;
    private int target_index;//靶位号
    private int trainee_status;//人员状态
    private int device_status;//设备状态
    private int deviceType;//靶机，采集，显靶分别为0，1，2
    private int deviceIndex;//设备编号
    private int deviceId;//设备标识

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

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(int deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
}
