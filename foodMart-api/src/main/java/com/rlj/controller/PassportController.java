package com.rlj.controller;

import com.rlj.pojo.Users;
import com.rlj.pojo.bo.UserBO;
import com.rlj.service.UserService;
import com.rlj.utils.CookieUtils;
import com.rlj.utils.IMOOCJSONResult;
import com.rlj.utils.JsonUtils;
import com.rlj.utils.MD5Utils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("passport")
public class PassportController {
    @Autowired
    private UserService userService;
    //1、判断用户名是否存在
    @GetMapping("/usernameIsExist")
    //返回的是一个枚举类，用来枚举不同状态下不同返回值的内涵
    public IMOOCJSONResult usernameIsExit(@RequestParam String username) {//@RequestParam表示这是一种请求类型的参数而不是路径参数
        //1、判断用户名不能为空
        if (StringUtils.isBlank(username)) {
            //如果请求传过来的用户名为空，就返回状态码500对应的枚举类状态
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }
        //2、查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");//如果用户名已经存在，就返回状态码500对应的枚举类状态
        }
        return IMOOCJSONResult.ok();//说明用户名不为空且数据库中没有该用户名，返回状态码200对应的枚举类状态
    }
    //2、创建用户
    @PostMapping("/regist")
    //返回的是一个枚举类，用来枚举不同状态下不同返回值的内涵   //cookie：也来一遍HttpServletRequest、HttpServletResponse
    public IMOOCJSONResult regist(@RequestBody UserBO userBO ,HttpServletRequest request,
                                  HttpServletResponse response) {//@RequestBody表示接收post请求中的请求体
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();
        //上面是在前端进行的校验（ajax发过来的异步请求的校验），在我们保存数据库之前，也要再进行一次校验
        //2.1、判断用户名和密码是否为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)
                || StringUtils.isBlank(confirmPassword)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        //2.2、查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        //2.3、密码长度不能少于6位
        if (password.length() < 6){
            return IMOOCJSONResult.errorMsg("密码长度不能小于6");
        }
        //2.4、判断两次密码是否一致
        if (!password.equals(confirmPassword)){
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }
        //2.5、实现注册
        Users userResult = userService.createUser(userBO);
        //复制过来
        userResult = setNullProperty(userResult);
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(userResult),true);
        //TODO 生成用户token，存入redis会话
        //TODO 从redis中同步购物车数据
        return IMOOCJSONResult.ok();
    }
    //3、用户登录
    @PostMapping("/login")
    //返回的是一个枚举类，用来枚举不同状态下不同返回值的内涵
    //CookieUtils方法要用到HttpServletRequest、HttpServletResponse，所以一并传入
    public IMOOCJSONResult login(@RequestBody UserBO userBO, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception{//@RequestBody表示接收post请求中的请求体
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        //3.1、判断用户名和密码是否为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        //3.2、实现登录(需要将将结果加密)
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));
        if (userResult == null){//说明根据用户名密码在数据库查询不到这样的一个Users，返回的肯定是一个空集
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }
        userResult = setNullProperty(userResult);//将准备放到cookie中的查询到的User对象的私密属性清空
        //使用老师给我们的CookieUtils将上面的对象放到cookie中，同时设置该cookie的名字，
        //并将其转换成为json字符(也是老师给我们的工具类)），并进行加密
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(userResult),true);
        //TODO 生成用户token，存入redis会话
        //TODO 从redis中同步购物车数据
        return IMOOCJSONResult.ok();
    }
    //4、定义一个清空查询到的Users对象的私密属性的方法
    private Users setNullProperty(Users userResult){
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }
    //5、用户登出
    @PostMapping("/logout")
    //来个swagger2
    @ApiOperation(value = "用户退出登录",notes = "用户退出登录",httpMethod = "POST")
    public IMOOCJSONResult logout(@RequestParam String userId, HttpServletRequest request,
                                  HttpServletResponse response){
        //清除用户相关的cookie
        CookieUtils.deleteCookie(request,response,"user");
        return IMOOCJSONResult.ok();
    }
}
