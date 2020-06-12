package com.luckyun.config.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.luckyun.config.CommonConfig;
import com.luckyun.config.model.EurekaApplications;
import com.luckyun.config.model.EurekaApplications.Application;
import com.luckyun.config.model.EurekaApplications.Instance;
import com.luckyun.config.model.InstanceIdObj;
import com.luckyun.config.service.EurekaHelperService;
import com.luckyun.config.service.YamlHelperService;

@RequestMapping("/configinfo")
@RestController
public class ConfigIndexController {

	@Autowired
	private CommonConfig commonConfig;
	
	@Autowired
	private YamlHelperService yamlHelperService;
	
	private static final Map<String,String> sername = new HashMap<String, String>();
	
	static {
		sername.put("luckyun-config", "config配置服务");
		sername.put("luckyun-base", "base基础服务");
		sername.put("luckyun-auth", "auth授权服务");
		sername.put("luckyun-oss-pro", "oss附件管理服务");
		sername.put("sia-task-config", "sia定时服务");
		sername.put("luckyun-report", "report导入导出服务");
		sername.put("luckyun-bpm-app", "bpm-app建模服务");
		sername.put("luckyun-bpm-api", "bpm-api工作流api服务");
		sername.put("luckyun-getway", "gateway网关服务");
	}
	
	@Autowired
	private EurekaHelperService eurekaHelperService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/index")
	public EurekaApplications test() {
		String url = "http://" + commonConfig.getEurekaHost() + ":" + commonConfig.getEurekaPort();
		ResponseEntity<String> result = 
				restTemplate.getForEntity(url + "/eureka/apps", String.class);
		EurekaApplications applications = JSON.parseObject(result.getBody(), EurekaApplications.class); 
		return applications;
	}
	
	/**
	 * 获取所有的应用列表
	 * @return 应用列表
	 */
	@GetMapping("/getApplications")
	public List<Application> getApplications(){
		List<Application> applications = eurekaHelperService.getEurekeServerInstances();
		for(Application application : applications) {
			String sname = sername.get(application.getName().toLowerCase());
			if(!StringUtils.isEmpty(sname)) {
				application.setSname(sname);
			}else {
				try {
					Object value = yamlHelperService.getYmlKeys(application.getName().toLowerCase()
							, "systeminfo|system-name");
					application.setSname(value.toString());
				}catch(Exception e) {
					application.setSname("[配置文件未知]服务!异常");
				}
			}
		}
		return applications.stream().filter(e -> !e.getName().toLowerCase().equals("luckyun-config")).collect(Collectors.toList());
	}
	
	@GetMapping("/restartApplication")
	public void restartApplication(@RequestParam(value="sname",defaultValue = "") String sname,
			@RequestParam(value="gatewayNams",defaultValue = "") String gateWayNames) {
		String[] gatewayNameArr = gateWayNames.split(",");
		List<Instance> instances = eurekaHelperService.getGateWayApplicationName(gatewayNameArr);
		List<Instance> activeInstance = eurekaHelperService.getActiveUPInstance(instances);
		if(activeInstance != null && activeInstance.size() >= 1) {
			if(StringUtils.isEmpty(sname)) {
				for(Instance instance : activeInstance) {
					restartServer(instance.getInstanceId(),"");
				}
			}else {
				for(Instance instance : activeInstance) {
					if(sname.toLowerCase().equals(instance.getApp().toLowerCase())) {
						restartServer(instance.getInstanceId(),"");
					}
				}
			}
		}else {
			throw new RuntimeException("无法找到活动的服务");
		}
		
	}
	
	@GetMapping("/restartApplicationByUrl")
	public void restartApplicationByHostAndPort(@RequestParam("instanceId") String instanceId,
			@RequestParam("instanceName") String instanceName) {
		restartServer(instanceId,instanceName);
	}
	
	
	@GetMapping("/restartSingleApplication")
	public void restartSingleApplication(@RequestParam("filename") String filename) {
		if("redis".equals(filename) || "mq".equals(filename) 
				|| "basicInfo".equals(filename) || "db".equals(filename)) {
			List<Application> applications = eurekaHelperService.getApplicationList();
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(Application application : applications) {
						if(!application.getName().toUpperCase().equals("LUCKYUN-CONFIG") 
								&& application.getName().toUpperCase().indexOf("EUREKA") < 0) {
							for(Instance instance : application.getInstance()) {
								restartServer(instance.getInstanceId(),application.getName().toUpperCase());
							}
							try {
								Thread.sleep(30000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
			thread.start();
		}else {
			InstanceIdObj instanceIdObj = eurekaHelperService.getInstanceId(filename);
			if(instanceIdObj.getInstanceIds() != null && instanceIdObj.getInstanceIds().size() >= 1) {
				for(String instanceId : instanceIdObj.getInstanceIds()) {
					restartServer(instanceId,instanceIdObj.getName());
				}
			}else {
				throw new RuntimeException("重启实例不存在");
			}
		}
	}
	
	private void restartServer(String instanceId,String instanceName) {
		String url = "http://"+ instanceId +"/restart/index";
		if(!instanceName.toUpperCase().equals("LUCKYUN-CONFIG")) {
			if(instanceName.toUpperCase().equals("LUCKYUN-BPM-APP")) {
				url = "http://"+ instanceId +"/activiti-app/app/restart/index";
			}
			if(instanceName.toLowerCase().equals("LUCKYUN-OSS-PRO")) {
				url = "http://"+ instanceId +"/restart/index?tomcatPath=/data/apache-tomcat-9.0.26/bin";
			}
			ResponseEntity<String> result = 
					restTemplate.getForEntity(url, String.class);
			System.out.println(result.getBody());
		}
	}
}
