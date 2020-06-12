package com.luckyun.config.model;

import java.util.List;

import lombok.Data;

@Data
public class RtnLogInfo {

	private Long timestemp;
	
	private List<UpdateSettingInfo> settingInfos;
	
	private String sysInfoName;
	
	private String stype;
	
	private String filename;
}
