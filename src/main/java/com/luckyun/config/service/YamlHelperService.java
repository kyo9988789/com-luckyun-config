package com.luckyun.config.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSONObject;

/**
 * 生成yaml文件业务类
 * @author yangj080
 *
 */
@Service
public class YamlHelperService {

	private final static DumperOptions OPTIONS = new DumperOptions();
	
	@Autowired
	private RedisHelperService redisHelperService;
	
	@Autowired
	private ConfigSettingService configSettingService;
	 
    static {
        //将默认读取的方式设置为块状读取
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }
    
    public void generateServerYml(JSONObject jsonObject,String filename) {
    	if(!filename.endsWith(".yml")) {
    		filename = filename + "-luckyun.yml";
    	}
    	Map<String, Object> mapResult = generateServerYml();
    	Set<String> setList = jsonObject.keySet();
    	//一层一层查找节点是否存在
    	for(String keys : setList) {
    		//以|为分隔符
    		String[] nodeListByKey = keys.split("\\|");
    		updateYml(nodeListByKey,jsonObject.get(keys),mapResult);
    	}
    	generateYml(mapResult,filename);
    }
    
    public String exportYaml() {
    	Map<String, Object> mapObj = new LinkedHashMap<String, Object>();
		List<String> keyList = redisHelperService.getSettingInfoKeys();
		for(String key : keyList) {
			mapObj.put(key, redisHelperService.getSettingInfoAllKey(key));
		}
		Yaml yaml = new Yaml(OPTIONS);
		return yaml.dump(mapObj);
    }
    
    @SuppressWarnings("unchecked")
	public void importYaml(InputStream in) {
    	Yaml yaml = new Yaml(OPTIONS);
		try {
			//清理原来key
			redisHelperService.refushdb();
			Object content = yaml.load(in);
			if(content instanceof Map) {
				for(Map.Entry<String, Object> entry : ((Map<String, Object>)content).entrySet()) {
					Object value = entry.getValue();
					String key = entry.getKey();
					String[] allKeys = key.split("setting_info_");
					if(allKeys.length == 2) {
						String result = allKeys[1];
						String[] filenameAndStype= result.split("\\$_\\$");
						if(filenameAndStype.length == 2) {
							configSettingService.generateSetting(value.toString(), filenameAndStype[0], filenameAndStype[1],1);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("生成配置文件出错!");
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    public Object getYmlKeys(String filename,String keys) {
    	if(!filename.endsWith(".yml")) {
    		filename = filename + "-luckyun.yml";
    	}
    	Map<String, Object> mapResult = readYml(filename);
    	String[] keyArrs = keys.split("\\|");
    	Object value = getYmlFileValue(keyArrs,0,mapResult);
    	return value;
    }
    
    @SuppressWarnings("unchecked")
	private Object getYmlFileValue(String[] keyArrs,int i,Map<String, Object> mapResult) {
    	if(i < keyArrs.length - 1) {
    		Object object = mapResult.get(keyArrs[i]);
    		if(object != null && object instanceof Map) {
    			return getYmlFileValue(keyArrs, ++i,(Map<String, Object>) object);
    		}
    	}else if(i == keyArrs.length-1) {
			return mapResult.get(keyArrs[i]);
    	}
    	return null;
    }
    
    /**
     * 还原初始的文件进行添加新的数据
     * @param jsonObject 新增的配置数据
     * @param fileName 文件名称对象
     */
    public void generateBaseServerYml(JSONObject jsonObject,String fileName) {
    	if(!fileName.endsWith(".yml")) {
    		fileName = fileName + "-luckyun.yml";
    	}
    	String filePath = generateFilePath(fileName);
    	//文件替换,将文件换回原始模板文件
    	repalceFiles(filePath);
    	Map<String, Object> mapResult = readYml(fileName);
    	Set<String> setList = jsonObject.keySet();
    	//一层一层查找节点是否存在
    	for(String keys : setList) {
    		//以|为分隔符
    		String[] nodeListByKey = keys.split("\\|");
    		updateYml(nodeListByKey,jsonObject.get(keys),mapResult);
    	}
    	createYml(mapResult,filePath);
    }
    
    /**
     *在本来的文件上面继续添加新的内容
     * @param jsonObject 新增的内容
     * @param fileName 文件名称对象
     */
    public void generateBaseServerYmlAppend(JSONObject jsonObject,String fileName) {
    	if(!fileName.endsWith(".yml")) {
    		fileName = fileName + "-luckyun.yml";
    	}
    	String filePath = generateFilePath(fileName);
    	//文件替换,将文件换回原始模板文件
    	File compFile = new File(filePath);
    	if(!compFile.exists()) {
    		repalceFiles(filePath);
    	}
    	Map<String, Object> mapResult = readYml(fileName);
    	Set<String> setList = jsonObject.keySet();
    	//一层一层查找节点是否存在
    	for(String keys : setList) {
    		//以|为分隔符
    		String[] nodeListByKey = keys.split("\\|");
    		updateYml(nodeListByKey,jsonObject.get(keys),mapResult);
    	}
    	createYml(mapResult,filePath);
    }
    
    private void repalceFiles(String competeFilePath) {
    	String os  = System.getProperty("os.name").toLowerCase();
    	if(os.indexOf("win") >= 0) {
    		competeFilePath = competeFilePath.startsWith("/")? competeFilePath.substring(1):competeFilePath;
    	}
    	try {
			Files.copy(Paths.get(competeFilePath + ".template")
					,Paths.get(competeFilePath), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("模板文件不存在");
		}
    }
    
    private void updateYml(String[] keyNodes,Object value,Map<String, Object> ymlObj) {
    	updateYmlValue(keyNodes,0,value,ymlObj);
    }
    
    @SuppressWarnings("unchecked")
	private void updateYmlValue(String[] keyNodes,int i,Object value,Map<String, Object> ymlObj) {
    	if(i < keyNodes.length - 1) {
	    	Object object = ymlObj.get(keyNodes[i]);
	    	if(object != null && object instanceof Map) {
	    		updateYmlValue(keyNodes, ++i,value ,(Map<String, Object>)object);
	    	}else {
	    		String[] needGenerateNodes = new String[keyNodes.length - i];
	    		int ij = 0;
	    		for(int j = i;j<keyNodes.length;j++) {
	    			needGenerateNodes[ij] = keyNodes[j];
	    			ij++;
	    		}
	    		generateYmlObj(needGenerateNodes,0,value,ymlObj);
	    	}
    	}else if(i == keyNodes.length - 1){
    		ymlObj.put(keyNodes[i], value);
    	}
    }
    private Map<String,Object> generateYmlObj(String[] needGenerateNodes,int i,Object value,Map<String,Object> ymlSonObj){
    	if(i == needGenerateNodes.length - 1) {
    		ymlSonObj.put(needGenerateNodes[i], value);
    	}else {
    		Map<String, Object> map = new LinkedHashMap<String, Object>();
    		ymlSonObj.put(needGenerateNodes[i], map);
    		generateYmlObj(needGenerateNodes, ++i , value, map);
    	}
    	return ymlSonObj;
    }
    
    @SuppressWarnings("unchecked")
	public Map<String, Object> readYml(String filename) {
    	String filePath = generateFilePath(filename);
    	File file = new File(filePath);
    	FileInputStream out = null;
    	try {
    		// 读取文件内容 (输入流)
            out = new FileInputStream(file);
            Yaml yaml = new Yaml(OPTIONS);
            Object loaded = yaml.load(out);
            Map<String, Object> mapRelease = (Map<String, Object>)loaded;
            return mapRelease;
    	}catch (Exception e) {
    		throw new RuntimeException("读取文件出错");
    	}finally {
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
    }
	
    public void generateYml(JSONObject jsonObject,String filename) {
    	String filePath = generateFilePath(filename);
    	File file = new File(filePath);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		createYml(jsonObject,filePath);
    }
    
    public void generateYml(Map<String, Object> jsonObject,String filename) {
    	String filePath = generateFilePath(filename);
    	File file = new File(filePath);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		createYml(jsonObject,filePath);
    }
    
    private String generateFilePath(String filename) {
    	String classesPath = YamlHelperService.class.getClassLoader().getResource("").getPath();
    	String path = "properties/luckyun/";
		String allPath = classesPath + path;
		File file = new File(allPath);
		if(!file.exists()) {
			throw new RuntimeException("文件夹结构异常,请检查");
		}
		String filePath = allPath + filename;
		return filePath;
    }
    
	private void createYml(JSONObject jsonObject,String dest) {
		Yaml yaml = new Yaml(OPTIONS);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(dest,false);
			//将数据重新写回文件
	        yaml.dump(jsonObject, fileWriter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void createYml(Map<String, Object> jsonObject,String dest) {
		Yaml yaml = new Yaml(OPTIONS);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(dest,false);
			//将数据重新写回文件
	        yaml.dump(jsonObject, fileWriter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private Map<String, Object> generateServerYml(){
		String[] fatherNode = new String[] {"redis","key-redis","rabbitmq"};
		String[][] sonNodes = new String[][] {
			{"host","port","password","database"},
			{"host","port","password","database"},
			{"host","port","username","password","virtual-host"}
		};
		String[][] sonValuess = new String[][] {
			{"${redis.host}","${redis.port}","${redis.password}","1"},
			{"${redis.host}","${redis.port}","${redis.password}","2"},
			{"${rabbitmq.host}","${rabbitmq.port}","${rabbitmq.username}","${rabbitmq.password}","${rabbitmq.vhost}"}
		};
		Map<String, Object> father = new LinkedHashMap<>();
		for(int i = 0;i<fatherNode.length;i++) {
			Map<String, Object> map = new LinkedHashMap<>();
			for(int j = 0;j<sonNodes[i].length;j++) {
				map.put(sonNodes[i][j], sonValuess[i][j]);
			}
			father.put(fatherNode[i], map);
		}
		Map<String, Object> rootNode = new LinkedHashMap<String, Object>();
		rootNode.put("spring", father);
		return rootNode;
	}
	
}
