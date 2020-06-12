package com.luckyun.config.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.luckyun.config.helper.ZuulDevDefaultHelper;
import com.luckyun.config.model.OtherSettingInfo;

@Service
public class ConfigSettingService {
	
	@Autowired
	private YamlHelperService yamlHelperService;
	
	@Autowired
	private RedisHelperService redisHelperService;
	
	@Autowired
	private PropertesHelperService propertesHelperService;

	@Autowired
	private Environment env;
	
	@Autowired
	private ZuulDevDefaultHelper zuulDevDefaultHelper; 
	
	/**
	 * 生成配置文件
	 * @param paramsStr 配置文件内容
	 * @param filename 文件名称
	 * @param stype 文件所属类型
	 * @param isource 来源.请求的来源为1,非请求来源为0
	 */
	public void generateSetting(String paramsStr,String filename,String stype,Integer isource) {
		JSONObject paramsCompete = JSONObject.parseObject(paramsStr);
		//开发环境下写入配置,其它环境移除key
		devWriteFeignPro(paramsCompete,filename);
		//优化提交的params
		optimiJsonObject(paramsCompete);
		JSONObject params = convertObject(paramsCompete);
		//基础模块
		if(stype.equals("basic")) {
			propertesHelperService.resetCommonProperties(params);
		//基础业务模块
		}else if(stype.equals("service")){
			operateTypeService(params,filename);
			//feign地址写入
			yamlHelperService.generateBaseServerYml(params, filename);
		//业务服务
		}else if(stype.equals("other")) {
			operateOther(params,filename);
			//写入feign下面对应的auth,base的值
			yamlHelperService.generateServerYml(params, filename);
			//写入新的配置业务配置文件
			if(!StringUtils.isEmpty(params.getString("systeminfo|system-name")) && isource == 1) {
				OtherSettingInfo otherSettingInfo = new OtherSettingInfo();
				otherSettingInfo.setFilename(filename);
				otherSettingInfo.setSname(params.getString("systeminfo|system-name"));
				redisHelperService.addOtherSettingInfo(filename, otherSettingInfo);
			}
		}
		if(!StringUtils.isEmpty(filename) && isource == 1) {
			//存入缓存,表示当前配置启用
			redisHelperService.addSettingState(filename);
			redisHelperService.addSettingInfo(filename + "$_$" + stype , paramsCompete.toJSONString());
		}
	}
	
	private void optimiJsonObject(JSONObject params) {
		if(params != null && params.get("updateKeys") != null) {
			params.remove("updateKeys");
		}
	}
	
	private void devWriteFeignPro(JSONObject params,String filename) {
		String cenv = propertesHelperService.getCenv();
		if(!StringUtils.isEmpty(cenv) && "dev".equals(cenv)) {
			JSONObject paramDev = null;
			if(!StringUtils.isEmpty(env.getProperty("LUCKYUN_BASE"))) {
				paramDev = new JSONObject();
				paramDev.put("value", env.getProperty("LUCKYUN_BASE"));
				paramDev.put("label", "feign的基础服务远程url");
				if(params.getString("application|servernm|luckyun-base-url") == null) {
					params.put("application|servernm|luckyun-base-url", paramDev);
				}
			}
			if(!StringUtils.isEmpty(env.getProperty("LUCKYUN_AUTH"))) {
				paramDev = new JSONObject();
				paramDev.put("value", env.getProperty("LUCKYUN_AUTH"));
				paramDev.put("label", "feign的授权服务远程url");
				if(params.getString("application|servernm|luckyun-auth-url") == null) {
					params.put("application|servernm|luckyun-auth-url", paramDev);
				}
			}
			if(!StringUtils.isEmpty(env.getProperty("LUCKYUN_BPM_API"))) {
				paramDev = new JSONObject();
				paramDev.put("value", env.getProperty("LUCKYUN_BPM_API"));
				paramDev.put("label", "feign的bpm-api服务远程url");
				if(params.get("application|servernm|luckyun-bpm-api-url") == null) {
					params.put("application|servernm|luckyun-bpm-api-url", paramDev);
				}
			}
			if("zuul".equals(filename)) {
				JSONObject jsonObject = zuulDevDefaultHelper.getDefaultProperties();
				Set<String> stringSet = jsonObject.keySet();
				for(String key : stringSet) {
					if(params.get(key) == null) {
						params.put(key, jsonObject.get(key));
					}
				}
			}
		}else {
			//非开发环境
			String[] devList = new String[] {"base","auth","oss","report"};
			for(String devKey : devList) {
				params.remove("zuul|routes|"+devKey+"|url");
			}
			params.remove("application|servernm|luckyun-base-url");
			params.remove("application|servernm|luckyun-auth-url");
			params.remove("application|servernm|luckyun-bpm-api-url");
		}
	}
	
	private JSONObject convertObject(JSONObject paramsCompete) {
		JSONObject param = new JSONObject();
		Set<String> keyList = paramsCompete.keySet();
		for(String key : keyList) {
			try {
				JSONObject jsonObject = paramsCompete.getJSONObject(key);
				if(jsonObject.get("value") != null) {
					param.put(key, stringConvertArr(jsonObject.get("value")));
				}
			}catch(Exception e) {
				param.put(key, paramsCompete.get(key));
			}
		}
		return param;
	}
	
	private Object stringConvertArr(Object value) {
		if(value instanceof String 
				&& value.toString().startsWith("[") && value.toString().endsWith("]")) {
			String valueStr = value.toString().substring(1, value.toString().length() - 1);
			return valueStr.split(",");
		}
		return value;
	}
	
	private void operateOther(JSONObject params,String filename) {
		JSONObject zuulParams = new JSONObject();
		zuulParams.put("zuul|routes|"+filename+"|service-id", filename);
		//限流
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		Map<String, Object> ratelimitService = new HashMap<String, Object>();
		ratelimitService.put("limit", "${zuul.stream.limit}");
		ratelimitService.put("refresh-interval", "${zuul.refresh.interval}");
		ratelimitService.put("type", new String[] {"url"});
		mapList.add(ratelimitService);
		zuulParams.put("zuul|ratelimit|policy-list|"+filename, mapList);
		//写入网关配置文件中
		//generateSetting(zuulParams.toJSONString(),"zuul","service",0);
		yamlHelperService.generateBaseServerYmlAppend(zuulParams, "zuul");
	}
	
	private void operateTypeService(JSONObject params,String filename) {
		if(!StringUtils.isEmpty(params.getString("spring|servlet|multipart|max-file-size"))) {
			params.put("spring|servlet|multipart|max-request-size", params.getString("spring|servlet|multipart|max-file-size"));
		}
		if("zuul".equals(filename)) {
			if(!StringUtils.isEmpty(params.getString("common|no-interceptor-url"))) {
				String[] noInterceptor = params.getString("common|no-interceptor-url").split(",");
				params.put("common|no-interceptor-url", Arrays.asList(noInterceptor));
			}
			if(!StringUtils.isEmpty(params.getString("common|container"))){
				String[] container = params.getString("common|container").split(",");
				params.put("common|container", Arrays.asList(container));
			}
			if(!StringUtils.isEmpty(params.getString("common|startwith"))){
				String[] startwith = params.getString("common|startwith").split(",");
				params.put("common|startwith", Arrays.asList(startwith));
			}
			if(!StringUtils.isEmpty(params.getString("common|endwith"))){
				String[] endwith = params.getString("common|endwith").split(",");
				params.put("common|endwith", Arrays.asList(endwith));
			}
			String cenv = propertesHelperService.getCenv();
			String[] routeList = new String[] {"zuul|routes|base|url","zuul|routes|auth|url"
					,"zuul|routes|oss|url","zuul|routes|report|url"};
			//测试环境下可以添加对应的内容,其它环境全部移除
			if(StringUtils.isEmpty(cenv) || !"dev".equals(cenv)) {
				for(String jkey : routeList) {
					params.remove(jkey);
				}
			}else if(!StringUtils.isEmpty(cenv) && "dev".equals(cenv)) {
				
				//测试环境下为feign提供对应的动态地址
				if(!StringUtils.isEmpty(params.getString("zuul|routes|base|url"))) {
					params.put("application|servernm|luckyun-base-url", params.getString("zuul|routes|base|url"));
				}
				if(!StringUtils.isEmpty(params.getString("zuul|routes|auth|url"))) {
					params.put("application|servernm|luckyun-auth-url", params.getString("zuul|routes|auth|url"));
				}
			}
		}else if("auth".equals(filename)){
			
			if(!StringUtils.isEmpty(params.getString("common|no-interceptor|container"))){
				String container = params.getString("common|no-interceptor|container");
				if(container.indexOf("/bpm") < 0) {
					container += ",/bpm";
					params.put("common|no-interceptor|container", container);
				}
			}else {
				params.put("common|no-interceptor|container", "/bpm");
			}
			if(!StringUtils.isEmpty(params.getString("common|no-interceptor|startwith"))) {
				String startwith = params.getString("common|no-interceptor|startwith");
				if(startwith.indexOf("noAuth") < 0) {
					startwith += ",noAuth";
					params.put("common|no-interceptor|startwith", startwith);
				}
			}else {
				params.put("common|no-interceptor|startwith", "noAuth");
			}
		}else if("base".equals(filename)){
			
			if(!StringUtils.isEmpty(params.getString("systeminfo|show-recycle"))
					&& params.getString("systeminfo|show-recycle").equals("true")) {
				params.put("systeminfo|show-recycle", 1);
			}else {
				params.put("systeminfo|show-recycle", 0);
			}
		}else if("bpm-lucksoft.yml".equals(filename)) {
			String bpmDriver = params.getString("spring|datasource|driver-class-name");
			String bpmUrl = params.getString("spring|datasource|url");
			String bpmUsername = params.getString("spring|datasource|username");
			String bpmPassword = params.getString("spring|datasource|password");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("bpm|driver", bpmDriver);
			jsonObject.put("bpm|url", bpmUrl);
			jsonObject.put("bpm|username", bpmUsername);
			jsonObject.put("bpm|password", bpmPassword);
			//写入配置文件中
			generateSetting(jsonObject.toJSONString(),"","basic",0);
		}
	}
	
	protected void copyProperties(JSONObject params ,JSONObject bak) {
		Set<String> setKeys = params.keySet();
		for(String key : setKeys) {
			bak.put(key, params.get(key));
		}
	}
}
