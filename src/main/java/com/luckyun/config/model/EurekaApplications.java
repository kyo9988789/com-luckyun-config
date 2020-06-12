package com.luckyun.config.model;

import java.util.List;

import lombok.Data;

@Data
public class EurekaApplications {

	private Applications applications;
	
	@Data
	public static class Applications {
		
		List<Application> application;
	}
	
	@Data
	public static class Application{
		
		private String name;
		
		private String sname;
		
		private List<Instance> instance;
		
	}
	
	@Data
	public static class Instance{
		
		/**
		 * 服务完整的ip地址包括端口
		 */
		private String instanceId;
		
		/**
		 * 域名或者ip地址
		 */
		private String hostName;
		
		/**
		 * 应用名称
		 */
		private String app;
		
		/**
		 * ip地址
		 */
		private String ipAddr;
		
		/**
		 * 状态,UP,DOWN
		 */
		private String status;
		
		/**
		 * 首页地址
		 */
		private String homePageUrl;
		
		/**
		 * 服务是否继承luckyun-core
		 */
		private Integer serverExtendCore;
		
		/**
		 * 可以进行的操作,1->启动状态,可以进行重启,停止,2->停止,可以进行重启,启动,
		 * 3->重启中,默认3分钟,状态无法变更则变成失败,-1->无法做任何操作
		 */
		private Integer operate;
	}
}
