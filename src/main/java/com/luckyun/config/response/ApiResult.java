package com.luckyun.config.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global Restful Return （统一返回结构）
 * 
 * @author omai
 *
 */
@Data
public class ApiResult {

	public ApiResult(int code, String message, Object data) {
		super();
		this.code = code;
		this.message = message;
		this.data = data;	
		this.list = new ArrayList<Object>();
		this.detail = new HashMap<Long,Object>();
	}
	
	public ApiResult() {
		this(1,null,null);
	}

	/**
	 * 1正常 0 异常
	 */
	private int code;
	/**
	 * 对错误的具体解释
	 */
	private String message;
	/**
	 * 返回的结果包装在value中，value可以是单个对象
	 */
	private Object data;
	
	private List<Object> list;
	
	private Map<Long,Object> detail;
	
	private Object mainInfo;
	
	private List<String> operates;

	
	/**
	 * 一般信息处理
	 * @param code
	 * @param message
	 * @param data
	 * @return
	 */
	public static ApiResult valueOf(int code, String message, Object data) {
		return new ApiResult(code, message, data);
	}

}