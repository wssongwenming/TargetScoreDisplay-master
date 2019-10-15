package com.bcsb.rabbitmq.entity;

/**
 * author:songwenming
 * Date:2019/10/13
 * Description:显示数据包括了打靶环数，
 *  float x = 10F, y = 20F;
    float r = (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    float theta = (float)Math.atan(y/x);
 */
public class MarkData {
    int id;
    float x;
    float y;
    float r;
    float theta;
    double ringNumber;
    String offsetDirection;
    String shootingTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public double getRingNumber() {
        return ringNumber;
    }

    public void setRingNumber(double ringNumber) {
        this.ringNumber = ringNumber;
    }

    public String getOffsetDirection() {
        return offsetDirection;
    }

    public void setOffsetDirection(String offsetDirection) {
        this.offsetDirection = offsetDirection;
    }

    public String getShootingTime() {
        return shootingTime;
    }

    public void setShootingTime(String shootingTime) {
        this.shootingTime = shootingTime;
    }
}
