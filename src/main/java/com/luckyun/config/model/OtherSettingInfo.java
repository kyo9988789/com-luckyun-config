package com.luckyun.config.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class OtherSettingInfo implements Serializable{

	/**
	 * TODO(请说明这个变量表示什么).
	 */
	private static final long serialVersionUID = -8633276294502445335L;
	
	private Integer istate;
	
	private String sname;
	
	private String filename;
	
	private String content;
}
