package com.bcsb.rabbitmq.entity;

public class Command2 {
;

	private String name;

	private int index;

	private String contentJson;



	public Command2(String name, int index) {
		super();
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getContentJson() {
		return contentJson;
	}

	public void setContentJson(String contentJson) {
		this.contentJson = contentJson;
	}
	
	

	
    
}
