package com.luckyun.config.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.luckyun.config.model.OtherSettingInfo;
import com.luckyun.config.model.RtnLogInfo;
import com.luckyun.config.model.SettingState;
import com.luckyun.config.service.ConfigSettingService;
import com.luckyun.config.service.PropertesHelperService;
import com.luckyun.config.service.RedisHelperService;
import com.luckyun.config.service.YamlHelperService;

@RestController
@RequestMapping("/setting")
public class ConfigSettingController {

	@Autowired
	private RedisHelperService redisHelperService;
	
	@Autowired
	private PropertesHelperService propertesHelperService;
	
	@Autowired
	private ConfigSettingService configSettingService;
	
	@Autowired
	private YamlHelperService yamlHelperService;
	
	
	@GetMapping("/getSettingState")
	public Map<String, SettingState> getSettingState(){
		return redisHelperService.getSettingState();
	}
	
	@PostMapping(value = "/writeProperties", produces = {"application/json;charset=UTF-8"} )
	public void writeProperties(HttpServletRequest request) {
		String filename = request.getParameter("filename");
		String stype = request.getParameter("stype");
		if(StringUtils.isEmpty(filename) || StringUtils.isEmpty(stype)) {
			throw new RuntimeException("参数异常");
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
			String paramsStr = stringBuilder.toString();
			redisHelperService.addUpdateInfo(new Date().getTime(), filename, stype, paramsStr);
			configSettingService.generateSetting(paramsStr, filename, stype,1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 导出配置数据
	 */
	@GetMapping("/getYaml")
	public void exportYml(HttpServletResponse res) {
		res.setHeader("content-type", "text/plain");
		res.setHeader("content-type", "application/x-msdownload;");
		res.setContentType("text/plain; charset=utf-8");
		res.setHeader("Content-Disposition", "attachment; filename=settingInfo.yaml");
		OutputStream os = null;
		try {
			os = res.getOutputStream();
			os.write(yamlHelperService.exportYaml().getBytes());
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("导出配置出错!");
		}finally {
			if(os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@PostMapping("/importYaml")
	public void importYml(@RequestParam(required=true) MultipartFile file) {
		try {
			yamlHelperService.importYaml(file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("导入文件异常!");
		}
	}
	
	@GetMapping("getOtherSettingInfo")
	public List<OtherSettingInfo> getInfo(){
		return redisHelperService.getOtherSettingInfo();
	}
	
	@PostMapping("addOtherSettingInfo")
	public void addOtherInfo(@RequestBody OtherSettingInfo otherSettingInfo) {
		JSONObject logObj = new JSONObject();
		if(!StringUtils.isEmpty(otherSettingInfo.getContent())) {
			logObj = JSONObject.parseObject(otherSettingInfo.getContent()); 
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("value", otherSettingInfo.getSname());
		jsonObject.put("label", "业务名称");
		logObj.put("other|name", jsonObject.toJSONString());
		jsonObject = new JSONObject();
		jsonObject.put("value", otherSettingInfo.getFilename());
		jsonObject.put("label", "业务别名");
		logObj.put("other|alias", jsonObject.toJSONString());
		redisHelperService.addUpdateInfo(new Date().getTime(), otherSettingInfo.getFilename()
				, "other", logObj.toJSONString());
		redisHelperService.addOtherSettingInfo(otherSettingInfo.getFilename()
				, otherSettingInfo);
		String content = otherSettingInfo.getContent();
		configSettingService.generateSetting(content, otherSettingInfo.getFilename(), "other",1);
	}
	
	@GetMapping("delOtherSettingInfo")
	public void delOtherInfo(String filename) {
		redisHelperService.recordOtherSettingInfo(new Date().getTime(), filename);
		redisHelperService.delOtherSettingInfo(filename);
	}
	
	@GetMapping("/getAllProperties")
	public Map<String, Object> getEnvFiles() {
		return propertesHelperService.getProperties();
	}
	
	@GetMapping("/getSettingInfo")
	public JSONObject getSettingInfo(@RequestParam("filename") String filename,@RequestParam("stype") String stype) {
		return JSONObject.parseObject(redisHelperService.getSettingInfo(filename+"$_$"+stype));
	}
	
	@GetMapping("/getUpdateLog")
	public List<RtnLogInfo> getUpdateLog(){
		return redisHelperService.getLogList();
	}
}
