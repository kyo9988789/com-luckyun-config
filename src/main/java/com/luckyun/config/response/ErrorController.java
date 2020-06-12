package com.luckyun.config.response;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;

/**
 * Global Error Return AOP （全局错误处理）
 * @author omai
 *
 */
@Controller
public class ErrorController extends AbstractErrorController{
	
    public ErrorController(ErrorAttributes errorAttributes) {
        super(new DefaultErrorAttributes());
    }
    
    @Override
    public String getErrorPath() {
        return null;
    }

    private static final String ERROR_PATH = "/error";
    private static final int OK = 200;
    
    @RequestMapping(ERROR_PATH)
    public ModelAndView ExceptionHandler(HttpServletRequest request,HttpServletResponse response){
        
        // 返回成功状态
        response.setStatus(OK);
        //国际化参数en,cn
        String locale = request.getHeader("locale");
        
        Map<String, Object> model = Collections.unmodifiableMap(getErrorAttributes(request, false));
        int status =  (Integer)model.get("status");
        String message = (String)model.get("message");
        
        re(response,status,message,locale);
     
        return null;
    }
    
    protected void re(HttpServletResponse response
    		,int status, String message ,String locale) {
    	 Map<String,Object> error = new HashMap<>();
         error.put("code", status);
         error.put("message",i18nConvertMsg(message,locale));
         writeJson(response,error);
    }
    
    private String i18nConvertMsg(String message,String locale) {
    	return message;
    }
    
    protected void writeJson(HttpServletResponse response,Map<String,Object> error){  
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(error));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}