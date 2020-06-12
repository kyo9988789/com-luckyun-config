package com.luckyun.config.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.luckyun.config.param.YamlParam;
import com.luckyun.config.service.YamlHelperService;

@RequestMapping("yml")
@RestController
public class ConfigYmlController {
	
	@Autowired
	private YamlHelperService yamlHelperService;
	
	@PostMapping(value = "/saveBaseYml", produces = {"application/json;charset=UTF-8"})
	public void saveBaseYml(HttpServletRequest request) {
		String filename = request.getParameter("filename");
		if(StringUtils.isEmpty(filename)) {
			throw new RuntimeException("需要传递文件名称!");
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
			String result = stringBuilder.toString();
			JSONObject jsonObject = JSONObject.parseObject(result);
			yamlHelperService.generateBaseServerYml(jsonObject, filename);
		}catch (IOException e) {
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

	@PostMapping("/saveNewServerYml")
	public void save(@RequestBody YamlParam yamlParam) {
		String content = yamlParam.getContent();
		try {
			JSONObject jsonObject = JSONObject.parseObject(content);
			yamlHelperService.generateServerYml(jsonObject, yamlParam.getFilename());
		}catch (Exception e) {
			throw new RuntimeException("配置信息有误,无法部署");
		}
	}
	
	@GetMapping("getYmlInfo")
	public Map<String, Object> getYmlInfo(String filename) {
		Map<String,Object> content = yamlHelperService.readYml(filename);
		return content;
	}
}
