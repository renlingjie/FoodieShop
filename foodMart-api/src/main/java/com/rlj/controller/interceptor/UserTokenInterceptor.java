package com.rlj.controller.interceptor;

import com.rlj.utils.IMOOCJSONResult;
import com.rlj.utils.JsonUtils;
import com.rlj.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class UserTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisOperator redisOperator;
    public static final String REDIS_USR_TOKEN = "redis_usr_token";
    //说明：这里返回的布尔类型是这样的含义：false代表请求被拦截，被驳回，验证出现问题；true代表请求在进行过验证校验之后是没问题的，是可以放行的
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)){
            String uniqueToken = redisOperator.get(REDIS_USR_TOKEN+":"+userId);
            if (StringUtils.isNotBlank(uniqueToken)){
                if (!uniqueToken.equals(userToken)){
                    returnErrorResponse(response,IMOOCJSONResult.errorMsg("账号不一致，可能是在异地登录"));
                    return false;
                }
            }else {
                returnErrorResponse(response,IMOOCJSONResult.errorMsg("请登录"));
                return false;
            }
        }else {
            returnErrorResponse(response,IMOOCJSONResult.errorMsg("请登录"));
            return false;//可以抛异常，这里我就抛空白页就表示被拦截了
        }
        return true;
    }
    //因为上面方法已经明确要返回boolean类型来标识是否拦截，但是如何将错误消息也回显到前端页面呢？
    //我们可以直接将错误消息变成输出流放到我们的响应中，在响应中执行write操作
    public void  returnErrorResponse(HttpServletResponse response, IMOOCJSONResult result){
        OutputStream out = null;
        try{
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");//是要将我们错误信息转换成json
            out = response.getOutputStream();
            //用byte的write，同时这里传入错误信息的Result对象，将之转换成为json后转换为流输出
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();//用完流清空
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (out != null){
                    out.close();//最后关闭流
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
