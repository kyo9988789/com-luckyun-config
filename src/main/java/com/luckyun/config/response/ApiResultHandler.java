package com.luckyun.config.response;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Global Restful Return AOP （全局返回AOP处理）
 * @author omai
 *
 */
@ControllerAdvice(basePackages = {"com.luckyun.config.controller"})
public class ApiResultHandler implements ResponseBodyAdvice<Object> {

	private final ObjectMapper objectMapper;
	
	@Autowired
	public ApiResultHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * 对所有RestController的接口方法进行拦截
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		Object out;
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
		//request或者response header有rpc均可
		Object rpc = request.getHeaders().get("rpc");
		if(rpc == null) {
			rpc = response.getHeaders().get("rpc");
		}
		
		if (rpc!=null || body instanceof ApiResult) { // 如果是rpc请求，或者返回类型是ApiResult则不作处理
			return body;
		} else if (body instanceof String) {
			try {
				return objectMapper.writeValueAsString(ApiResult.valueOf(1, null, body));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} 
		else if (body instanceof List) {
			out = new ApiResult(1, "success", body);
		} else {
			out = new ApiResult(1, "success", body);
		}
		return out;
	}
}