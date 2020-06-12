package com.luckyun.config.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.luckyun.config.CommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认配置
 * @author yangj080
 *
 */
@Service
@Slf4j
public class PropertesHelperService {
	
	@Autowired
	private CommonConfig commonConfig;
	
	private final String classesPath = PropertesHelperService.class.getClassLoader().getResource("").getPath()
			.endsWith("/") ? PropertesHelperService.class.getClassLoader().getResource("").getPath() : PropertesHelperService.class.getClassLoader().getResource("").getPath() + "/";

	public void resetCommonProperties(JSONObject settingInfo) {
		File file = getPropertiesFile();
		FileInputStream out = null;
		InputStreamReader isr = null;
		Writer outWirte = null;
		try {
            // 读取文件内容 (输入流)
			out = new FileInputStream(file);
            isr = new InputStreamReader(out);
            int ch = 0;
            StringBuilder stringBuilder = new StringBuilder();
            while ((ch = isr.read()) != -1) {
                stringBuilder.append((char) ch);
            }
            Set<String> keySets = settingInfo.keySet();
            for(String setKey : keySets) {
            	Object value = settingInfo.get(setKey);
            	String replaceValue = setKey.replaceAll("\\|", ".");
            	int start = stringBuilder.indexOf(replaceValue + "=");
            	//需要替换文件开始的位置, +1 代表到=号位置
            	int realStart = start + replaceValue.length() + 1;
            	int realEnd = realStart;
            	while(realEnd < stringBuilder.length()) {
            		String csb = stringBuilder.substring(realEnd, realEnd+1);
            		//计算换行符的位置
            		if(csb.equals("\n") || csb.equals("\t")) {
            			break;
            		}
            		realEnd ++;
            	}
            	if(start >= 0) {
            		stringBuilder.replace(realStart, realEnd, value.toString());
            	}
            }
            outWirte = new FileWriter(file,false);
            outWirte.write(stringBuilder.toString());
        } catch (Exception e) {
        	log.error("设置配置出错;错误信息:" + e.getMessage());
        } finally {
        	if(out != null) {
        		try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	if(isr != null) {
        		try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	if(outWirte != null) {
        		try {
					outWirte.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		
	}
	
	public Map<String,Object> getProperties(){
		FileInputStream fis = null;
		
		File file = getPropertiesFile();
		try {
			fis = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fis);
			Enumeration<Object> keys = properties.keys();
			Map<String,Object> mapResult = new LinkedHashMap<String, Object>();
			while(keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				mapResult.put(key, properties.get(key));
			}
			return mapResult;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new HashMap<>();
	}
	
	private File getPropertiesFile() {
		String cenv = getCenv();
		if(StringUtils.isEmpty(cenv)) {
			throw new RuntimeException("环境变量不存在");
		} 
		String commonFileName = classesPath + "properties/common-" + cenv + ".properties";
		String devFileName = classesPath + "properties/common-template.properties";
		File devFile = new File(devFileName);
		File file = new File(commonFileName);
		if(!file.exists()) {
			if(!devFile.exists()) {
				throw new RuntimeException("缺少必要的common-template.properties模板文件");
			}
			devFile.renameTo(file);
		}
		return file;
	}
	
	public String getCenv(){
		String[] stringEnv = new String[] {"dev","test","prod-test","prod","custom"};
		String cenv = commonConfig.getCenv();
		String[] cenvArr = cenv.split(",");
		for(String se : cenvArr) {
			if(isContainer(stringEnv, se)) {
				return se;
			}
		}
		return "";
	}
	
	private boolean isContainer(String[] arr,String name) {
		for(String el : arr) {
			if(el.equals(name)) {
				return true;
			}
		}
		return false;
	}
}
