package com.luckyun.config.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.luckyun.config.model.OtherSettingInfo;
import com.luckyun.config.model.RtnLogInfo;
import com.luckyun.config.model.SettingState;
import com.luckyun.config.model.UpdateSettingInfo;
import com.luckyun.config.redis.RedisOperationDao;

@Service
public class RedisHelperService {

	@Autowired
	private RedisOperationDao redisOperationDao;
	
	/**
	 * 设置服务的状态
	 * @param key 需要设置的服务field
	 */
	public void addSettingState(String field) {
		String compKey = "luckyun_setting_state";
		SettingState settingState = new SettingState();
		settingState.setIstate(1);
		redisOperationDao.hSet(compKey, field, JSONObject.toJSONString(settingState));
	}
	
	public Map<String, SettingState> getSettingState() {
		String compKey = "luckyun_setting_state";
		Map<String, SettingState> mapStateList = 
				redisOperationDao.hMgetAll(compKey, SettingState.class);
		return mapStateList;
	}
	
	public void addSettingInfo(String key,String value) {
		redisOperationDao.set("setting_info_" + key, value);
	}
	
	public String getSettingInfo(String key) {
		return redisOperationDao.get("setting_info_" + key);
	}
	
	public String getSettingInfoAllKey(String key) {
		return redisOperationDao.get(key);
	}
	
	public void addOtherSettingInfo(String filenameField,OtherSettingInfo content) {
		String compKey = "luckyun_other_setting_info";
		content.setIstate(1);
		redisOperationDao.hSet(compKey, filenameField, JSONObject.toJSONString(content));
	}
	
	public List<OtherSettingInfo> getOtherSettingInfo() {
		String compKey = "luckyun_other_setting_info";
		Map<String, OtherSettingInfo> mapStateList = 
				redisOperationDao.hMgetAll(compKey, OtherSettingInfo.class);
		List<OtherSettingInfo> infos = new ArrayList<OtherSettingInfo>();
		for(Map.Entry<String, OtherSettingInfo> entry : mapStateList.entrySet()) {
			//获取未被删除的对象
			if(entry.getValue().getIstate() == 1) {
				infos.add(entry.getValue());
			}
		}
		return infos;
	}
	
	public void delOtherSettingInfo(String filenameField) {
		String compKey = "luckyun_other_setting_info";
		Map<String, OtherSettingInfo> mapStateList = 
				redisOperationDao.hMgetAll(compKey, OtherSettingInfo.class);
		mapStateList.remove(filenameField);
		redisOperationDao.del(compKey);
		redisOperationDao.del("setting_info_"+filenameField + "$_$other");
		for(Map.Entry<String, OtherSettingInfo> entry : mapStateList.entrySet()) {
			redisOperationDao.hSet(compKey, entry.getKey(), JSONObject.toJSONString(entry.getValue()));
		}
	}
	//删除时记录的日志
	public void recordOtherSettingInfo(Long timestmp,String filenameField) {
		String keys = "setting_update_log$_$" + timestmp;
		String otherStr = redisOperationDao.get("setting_info_"+filenameField + "$_$other");
		JSONObject otherObj = JSONObject.parseObject(otherStr);
		Set<String> otherKeys = otherObj.keySet();
		List<UpdateSettingInfo> updateSettingInfos = new ArrayList<>();
		String sysInfoName = "";
		if(!StringUtils.isEmpty(otherObj.getString("systeminfo|system-name"))) {
			Object sysInfoValueObj = getValue(otherObj.getString("systeminfo|system-name"));
			if(sysInfoValueObj instanceof JSONObject) {
				sysInfoName = ((JSONObject) sysInfoValueObj).getString("value");
			}else {
				sysInfoName = sysInfoValueObj.toString();
			}
		}
		for(String okey : otherKeys) {
			if(!"systeminfo|system-name".equals(okey)) {
				Object oldObj = getValue(otherObj.getString(okey));
				updateSettingInfos.add(generateLogObj(null,oldObj,okey,timestmp,filenameField,"other",sysInfoName));
			}
		}
		if(updateSettingInfos != null && updateSettingInfos.size() >= 1) {
			redisOperationDao.setList(keys, updateSettingInfos);
		}
	}
	
	public List<String> getSettingInfoKeys(){
		return redisOperationDao.getPatternKeys("setting_info_*"); 
	}
	
	/**
	 * 添加日志到redis
	 * @param timestmp 时间戳
	 * @param filename 文件名称
	 * @param stype 分类
	 * @param newObjStr 新的对象
	 */
	public void addUpdateInfo(Long timestmp,String filename
			,String stype,String newObjStr) {
		JSONObject newObj = JSONObject.parseObject(newObjStr);
		String keys = "setting_update_log$_$" + timestmp;
		String oldStr = getSettingInfo(filename + "$_$" + stype );
		JSONObject oldObj = JSONObject.parseObject(oldStr);
		String updateKeys = newObj.get("updateKeys") != null ? newObj.getString("updateKeys") : "";
		//拿到修改的key
		String[] updKeys = updateKeys.split(",");
		List<UpdateSettingInfo> updateSettingInfos = new ArrayList<>();
		List<String> delKeyList = getDeleteKeys(oldObj,newObj);
		//整合所有的被操作的key
		for(String updKey : updKeys) {
			delKeyList.add(updKey);
		}
		String sysInfoName = "";
		if(!StringUtils.isEmpty(newObj.getString("systeminfo|system-name"))) {
			Object sysInfoValueObj = getValue(newObj.getString("systeminfo|system-name"));
			if(sysInfoValueObj instanceof JSONObject) {
				sysInfoName = ((JSONObject) sysInfoValueObj).getString("value");
			}else {
				sysInfoName = sysInfoValueObj.toString();
			}
		}
		for(String updKey : delKeyList) {
			if(!StringUtils.isEmpty(updKey)) {
				String oldValue = oldObj != null ? oldObj.get(updKey) != null ? oldObj.getString(updKey) : "" : "";
				Object oldValueObj = getValue(oldValue);
				String newValue = newObj != null ? newObj.get(updKey) != null ? newObj.getString(updKey) : "" : "";
				Object newValueObj = getValue(newValue);
				UpdateSettingInfo settingInfo = generateLogObj
						(newValueObj,oldValueObj,updKey,timestmp,filename,stype,sysInfoName);
				updateSettingInfos.add(settingInfo);
			}
		}
		if(updateSettingInfos != null && updateSettingInfos.size() >= 1) {
			redisOperationDao.setList(keys, updateSettingInfos);
		}
	}
	//生成日志记录的对象
	private UpdateSettingInfo generateLogObj(Object newValueObj,Object oldValueObj
			,String updKey,Long timestmp,String filename,String stype,String sysInfoName) {
		UpdateSettingInfo settingInfo = new UpdateSettingInfo();
		//删除
		if(newValueObj == null) {
			settingInfo.setNewValue(null);
		}else {
			if(newValueObj instanceof JSONObject) {
				JSONObject newJson = (JSONObject)newValueObj;
				settingInfo.setNewValue(newJson.getString("value"));
				if(!StringUtils.isEmpty(newJson.getString("label"))) {
					settingInfo.setLabel(newJson.getString("label"));
				}
			}else {
				settingInfo.setNewValue(newValueObj);
			}
		}
		if(oldValueObj != null) {
			if(oldValueObj instanceof JSONObject) {
				JSONObject oldJson = (JSONObject)oldValueObj;
				settingInfo.setOldValue(oldJson.getString("value"));
				if(!StringUtils.isEmpty(oldJson.getString("label"))) {
					settingInfo.setLabel(oldJson.getString("label"));
				}
			}else {
				settingInfo.setOldValue(oldValueObj);
			}
		}
		settingInfo.setUpdateKey(updKey);
		settingInfo.setTimeStamp(timestmp);
		settingInfo.setFilename(filename);
		settingInfo.setStype(stype);
		settingInfo.setSystemInfoName(sysInfoName);
		return settingInfo;
	}
	
	private List<String> getDeleteKeys(JSONObject oldObj,JSONObject newObj){
		if(oldObj != null) {
			Set<String> oldSet = oldObj.keySet();
			Set<String> newSet = newObj.keySet();
			List<String> delKeys = new ArrayList<>();
			for(String oldKey : oldSet) {
				if(!newSet.contains(oldKey)) {
					delKeys.add(oldKey);
				}
			}
			return delKeys;
		}
		return new ArrayList<>();
	}
	
	public List<RtnLogInfo> getLogList(){
		Set<String> keyList = redisOperationDao.getKeys("setting_update_log*");
		List<RtnLogInfo> logInfos = new ArrayList<>();
		for(String key : keyList) {
			RtnLogInfo info = new RtnLogInfo();
			List<UpdateSettingInfo> settingInfos = redisOperationDao.getList(key, UpdateSettingInfo.class);
			info.setSettingInfos(settingInfos);
			String[] keyArr = key.split("\\$_\\$");
			if(keyArr.length == 2) {
				info.setTimestemp(Long.valueOf(keyArr[1]));
			}
			if(settingInfos != null && settingInfos.size() >= 1) {
				UpdateSettingInfo updateSettingInfo = settingInfos.get(0);
				info.setFilename(updateSettingInfo.getFilename());
				info.setStype(updateSettingInfo.getStype());
				info.setSysInfoName(updateSettingInfo.getSystemInfoName());
			}
			logInfos.add(info);
		}
		return logInfos.stream().sorted((a,b) -> b.getTimestemp().compareTo(a.getTimestemp())).collect(Collectors.toList());
	}
	
	private Object getValue (String value) {
		try {
			JSONObject jsonObject = JSONObject.parseObject(value);
			return jsonObject;
		}catch (Exception e) {
			return value;
		}
	}
	
	public void refushdb() {
		List<String> settingList = redisOperationDao.getPatternKeys("setting_*");
		List<String> luckList = redisOperationDao.getPatternKeys("luckyun_*");
		for(String key : settingList) {
			redisOperationDao.del(key);
		}
		for(String key : luckList) {
			redisOperationDao.del(key);
		}
	}
}
