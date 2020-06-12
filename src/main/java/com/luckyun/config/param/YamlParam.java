package com.luckyun.config.param;

import java.io.Serializable;

import lombok.Data;

@Data
public class YamlParam implements Serializable{
	/**
	 * TODO(请说明这个变量表示什么).
	 */
	private static final long serialVersionUID = 77432243413521921L;
	
	private String content;
	
	/**
	 * 生成的文件名称
	 */
	private String filename;

}
