package com.luckyun.config.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.luckyun.config.service.PropertesHelperService;

import lombok.Data;

@Component
public class ZuulDevDefaultHelper {
	
	@Autowired
	private PropertesHelperService propertesHelperService;
	
	@Autowired
	private Environment env;

	public JSONObject getDefaultProperties() {
		JSONObject jsonObject = new JSONObject();
		String cenv = propertesHelperService.getCenv();
		if(!StringUtils.isEmpty(cenv) && "dev".equals(cenv)) {
			
			List<ZuulRouteEntity> routerList = new ArrayList<>();
			ZuulRouteEntity zuulRouteEntity = new ZuulRouteEntity("LUCKYUN_BASE", "base", "网关跳转的基础服务远程url");
			routerList.add(zuulRouteEntity);
			ZuulRouteEntity zuulRouteEntity1 = new ZuulRouteEntity("LUCKYUN_AUTH", "auth", "网关跳转的授权服务远程url");
			routerList.add(zuulRouteEntity1);
			ZuulRouteEntity zuulRouteEntity2 = new ZuulRouteEntity("LUCKYUN_OSS_PRO", "oss", "网关跳转的oss服务远程url");
			routerList.add(zuulRouteEntity2);
			ZuulRouteEntity zuulRouteEntity3 = new ZuulRouteEntity("LUCKYUN_REPORT", "report", "网关跳转的report服务远程url");
			routerList.add(zuulRouteEntity3);
			for(ZuulRouteEntity entry : routerList) {
				String value = env.getProperty(entry.getValue());
				entry.setValue(value);
				if(!StringUtils.isEmpty(value)) {
					jsonObject.put("zuul|routes|" + entry.getKey() + "|url", entry);
				}
			}
			routerList.clear();
		}
		return jsonObject;
	}
	/**
	 * 开发环境下的基础服务,授权服务等其它非必要服务的路由跳转配置,一般会跳转到公用服务器
	 */
	public Map<String,JSONObject> serviceZuulRouteUrl() {
		String cenv = propertesHelperService.getCenv();
		if(!StringUtils.isEmpty(cenv) && "dev".equals(cenv)) {
			JSONObject jsonObject = getDefaultProperties();
			Map<String,JSONObject> result = new HashMap<>(1);
			result.put("zuul", jsonObject);
			return result;
		}
		return null;
	}
	
	@Data
	private class ZuulRouteEntity{
		
		public ZuulRouteEntity(String value,String key,String label) {
			this.key = key;
			this.value = value;
			this.label = label;
		}
		private String key;
		
		private String value;
		
		private String label;
	}
}
