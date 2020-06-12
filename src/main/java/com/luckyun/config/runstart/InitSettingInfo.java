package com.luckyun.config.runstart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.luckyun.config.helper.ZuulDevDefaultHelper;
import com.luckyun.config.redis.RedisOperationDao;
import com.luckyun.config.service.ConfigSettingService;
import com.luckyun.config.service.PropertesHelperService;

@Component
public class InitSettingInfo {
	
	@Autowired
	private RedisOperationDao redisOperationDao;
	
	@Autowired
	private PropertesHelperService propertesHelperService;
	
	@Autowired
	private ConfigSettingService configSettingService;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private ZuulDevDefaultHelper zuulDevDefaultHelper;

	@Order(80)
	@EventListener
	public void onInitSettingInfo(ContextRefreshedEvent event) {
		List<String> keys = redisOperationDao.getPatternKeys("setting_info_*");
		List<String> basicKeySets = new ArrayList<String>();
		if(keys != null && keys.size() >= 1) {
			for(String key : keys) {
				String[] allKeys = key.split("setting_info_");
				String value = redisOperationDao.get(key);
				if(allKeys.length == 2) {
					String result = allKeys[1];
					String[] filenameAndStype= result.split("\\$_\\$");
					//移除不符合要求的key
					value = removeKeyNoDev(value);
					if(filenameAndStype.length == 2) {
						configSettingService.generateSetting(value, filenameAndStype[0], filenameAndStype[1] , 0);
					}
				}
			}
		}else {
			//环境变量镜像初始化
			Map<String, JSONObject> value = basicTypeEnv(null);
			writeBasicRedis(value,basicKeySets);
			Map<String, JSONObject> serviceValue = serviceTypeEnv(null);
			writeServiceRedis(serviceValue,null);
			//路由服务器写入
			Map<String, JSONObject> serviceZuulRoute = zuulDevDefaultHelper.serviceZuulRouteUrl();
			if(serviceZuulRoute != null) {
				writeServiceRedis(serviceZuulRoute,null);
			}
		}
	}
	
	private String removeKeyNoDev(String value) {
		JSONObject jsonObject = JSONObject.parseObject(value);
		String cenv = propertesHelperService.getCenv();
		if(StringUtils.isEmpty(cenv) || !"dev".equals(cenv)) {
			String[] devList = new String[] {"base","auth","oss","report"};
			for(String devKey : devList) {
				jsonObject.remove("zuul|routes|"+devKey+"|url");
			}
			jsonObject.remove("application|servernm|luckyun-base-url");
			jsonObject.remove("application|servernm|luckyun-auth-url");
			jsonObject.remove("application|servernm|luckyun-bpm-api-url");
		}
		return jsonObject.toJSONString();
	}
	
	private void writeBasicRedis(Map<String,JSONObject> mapObj,List<String> basicList) {
		for(Map.Entry<String, JSONObject> entry : mapObj.entrySet()) {
			if(!basicList.contains(entry.getKey())) {
				JSONObject jsonObject = entry.getValue();
				if(jsonObject.size() >= 1) {
					configSettingService.generateSetting(jsonObject.toJSONString(), entry.getKey(), "basic" , 1);
				}
			}
		}
	}
	
	private void writeServiceRedis(Map<String,JSONObject> mapObj,List<String> basicList) {
		for(Map.Entry<String, JSONObject> entry : mapObj.entrySet()) {
			if(basicList == null || 
					(basicList != null && !basicList.contains(entry.getKey()))) {
				JSONObject jsonObject = entry.getValue();
				if(jsonObject.size() >= 1) {
					configSettingService.generateSetting(jsonObject.toJSONString(), entry.getKey(), "service" , 1);
				}
			}
		}
	}
	
	private String basicTypeEnvRtnStr(String basicStr) {
		JSONObject basicObj = JSONObject.parseObject(basicStr);
		if(basicObj == null) {
			basicObj = new JSONObject();
		}
		Map<String, Object> propertiesObj = propertesHelperService.getProperties();
		for(Map.Entry<String, Object> entry : propertiesObj.entrySet()) {
			//没有在redis里面进行配置,则获取环境变量值
			String basicKey = entry.getKey().replaceAll("\\.", "|");
			String linuxKey = entry.getKey().toUpperCase().replaceAll("\\.", "_");
			if(basicObj.get(basicKey) == null && env.getProperty(linuxKey) != null) {
				String value = env.getProperty(linuxKey);
				JSONObject valueObj = new JSONObject();
				valueObj.put("value", value);
				basicObj.put(basicKey, valueObj);
			}
		}
		return basicObj.toJSONString();
	}
	/**
	 * 根据环境变量更新数据
	 */
	private Map<String, JSONObject> basicTypeEnv(String basicStr) {
		JSONObject basicObj = JSONObject.parseObject(basicTypeEnvRtnStr(basicStr));
		Map<String, JSONObject> typeMap = new HashMap<String, JSONObject>();
		typeMap.put("redis", new JSONObject());
		typeMap.put("mq", new JSONObject());
		typeMap.put("db", new JSONObject());
		Set<String> keySet = basicObj.keySet();
		for(String key : keySet) {
			if(key.startsWith("redis|")) {
				typeMap.get("redis").put(key, basicObj.get(key));
			}else if(key.startsWith("rabbitmq|")) {
				typeMap.get("mq").put(key, basicObj.get(key));
			}else if(key.startsWith("base|")) {
				typeMap.get("db").put(key, basicObj.get(key));
			}
		}
		return typeMap;
	}
	
	private Map<String,JSONObject> serviceTypeEnv(String serviceStr){
		JSONObject basicObj = JSONObject.parseObject(basicTypeEnvRtnStr(serviceStr));
		Map<String, JSONObject> typeMap = new HashMap<String, JSONObject>();
		typeMap.put("bpm-lucksoft.yml", new JSONObject());
		Set<String> keySet = basicObj.keySet();
		for(String key : keySet) {
			if(key.startsWith("bpm|")) {
				String bpmRealKey = bpmConvert(key);
				if(!StringUtils.isEmpty(bpmRealKey)) {
					typeMap.get("bpm-lucksoft.yml").put(bpmRealKey, basicObj.get(key));
				}
			}
		}
		return typeMap;
	}
	
	private String bpmConvert(String key) {
		switch(key) {
			case "bpm|driver":return "spring|datasource|driver-class-name";
			case "bpm|url":return "spring|datasource|url";
			case "bpm|username":return "spring|datasource|username";
			case "bpm|password" : return "spring|datasource|password";
		}
		return "";
	}
}
