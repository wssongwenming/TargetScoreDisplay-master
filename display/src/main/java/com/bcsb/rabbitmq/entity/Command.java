package com.bcsb.rabbitmq.entity;

import java.io.Serializable;

public class Command{
   int code;
   String description;
   int index;
   UserDetail data;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public UserDetail getData() {
		return data;
	}

	public void setData(UserDetail data) {
		this.data = data;
	}
}
