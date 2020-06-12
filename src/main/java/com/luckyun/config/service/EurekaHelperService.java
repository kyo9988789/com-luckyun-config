package com.luckyun.config.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.luckyun.config.CommonConfig;
import com.luckyun.config.model.EurekaApplications;
import com.luckyun.config.model.EurekaApplications.Application;
import com.luckyun.config.model.EurekaApplications.Instance;
import com.luckyun.config.model.InstanceIdObj;

import lombok.extern.slf4j.Slf4j;

/**
 * eureka相关功能
 * @author yangj080
 *
 */
@Service
@Slf4j
public class EurekaHelperService {
	
	private static final String UP = "UP";
	
	private static final String DOWNLOAD = "DOWNLOAD";
	
	private static final Map<String,String> serverMapping = new HashMap<>(); 
	
	static {
		serverMapping.put("base", "LUCKYUN-BASE");
		serverMapping.put("auth", "LUCKYUN-AUTH");
		serverMapping.put("bpm-lucksoft.yml", "LUCKYUN-BPM-API");
		serverMapping.put("zuul", "LUCKYUN-GETWAY");
		serverMapping.put("oss-pro", "LUCKYUN-OSS-PRO");
		serverMapping.put("report-lucksoft.yml", "LUCKYUN-REPORT");
		//serverMapping.put("siatask-lucksoft.yml", "");
		//serverMapping.put("", "");
	}
	
	@Autowired
	private CommonConfig commonConfig;

	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate restTemplate;
	
	public List<Application> getEurekeServerInstances(){
		List<Application> resultApplications = getApplications();
		if(resultApplications != null && resultApplications.size() >= 1) {
			for(Application application : resultApplications) {
				List<Instance> instances = application.getInstance();
				for(Instance instance : instances) {
					if(UP.equals(instance.getStatus())) {
						instance.setServerExtendCore(0);
						//启动状态,可以进行停止,重启
						instance.setOperate(1);
					}else if(DOWNLOAD.equals(instance.getStatus())) {
						instance.setServerExtendCore(0);
						instance.setOperate(-1);
					}else {
						instance.setServerExtendCore(0);
						instance.setOperate(-1);
					}
				}
			}
		}
		return resultApplications;
	}
	
	public InstanceIdObj getInstanceId(String filename) {
		InstanceIdObj instanceIdObj = new InstanceIdObj();
		List<Application> resultApplications = getApplications();
		for(Application application : resultApplications) {
			List<Instance> upInstances = getActiveUPInstance(application.getInstance());
			String value = serverMapping.get(filename);
			if(!StringUtils.isEmpty(value)) {
				if(application.getName().toUpperCase().equals(value.toUpperCase())) {
					instanceIdObj.setName(value.toUpperCase());
					instanceIdObj.setInstanceIds(upInstances.stream().map(Instance::getInstanceId).collect(Collectors.toList()));
					break;
				}
			}else {
				if(application.getName().toUpperCase().equals(filename.toUpperCase())) {
					instanceIdObj.setName(filename.toUpperCase());
					instanceIdObj.setInstanceIds(upInstances.stream().map(Instance::getInstanceId).collect(Collectors.toList()));
					break;
				}
			}
		}
		return instanceIdObj;
	}
	
	public List<Application> getApplicationList(){
		return getApplications();
	}
	
	public List<Instance> getActiveUPInstance(List<Instance> instances){
		List<Instance> UPInstance = new ArrayList<EurekaApplications.Instance>();
		for(Instance instance : instances) {
			if(UP.equals(instance.getStatus())) {
				UPInstance.add(instance);
			}
		}
		return UPInstance;
	}
	
	public List<Instance> getGateWayApplicationName(String ...getwayNames){
		String[] getMaybeName = getwayNames;
		if(getMaybeName.length <= 0) {
			getMaybeName = new String[] {"luckyun-getway","luckyun-gateway"};
		}
		List<Application> applications = getApplications();
		for(String name : getMaybeName) {
			List<Instance> instances = getInstanceByName(name,applications);
			if(instances != null && instances.size() >= 1) {
				return instances;
			}else {
				continue;
			}
		}
		return null;
	}
	
	private List<Instance> getInstanceByName(String name,List<Application> applications) {
		try {
			String lowerName = name.toLowerCase();
			for(Application application : applications) {
				if(lowerName.equals(application.getName().toLowerCase())) {
					return application.getInstance();
				}
			}
		}catch(Exception e) {
			log.error("获取实例列表出错;错误信息:" + e.getMessage());
		}
		return null;
	}
	
	private List<Application> getApplications(){
		String url = "http://" + commonConfig.getEurekaHost() + ":" + commonConfig.getEurekaPort();
		ResponseEntity<String> result = 
				restTemplate.getForEntity(url + "/eureka/apps", String.class);
		EurekaApplications applications = JSON.parseObject(result.getBody(), EurekaApplications.class); 
		List<Application> resultApplications = 
				applications.getApplications().getApplication();
		return resultApplications;
	}
}
