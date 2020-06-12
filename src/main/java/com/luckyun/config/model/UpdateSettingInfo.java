package com.luckyun.config.model;

import lombok.Data;

@Data
public class UpdateSettingInfo {

	private Object oldValue;
	
	private Object newValue;
	
	private String updateKey;
	
	private Long timeStamp;
	
	private String systemInfoName;
	
	private String label;
	
	private String filename;
	
	private String stype;
}
