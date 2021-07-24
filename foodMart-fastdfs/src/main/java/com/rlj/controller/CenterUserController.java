package com.rlj.controller;

import com.rlj.pojo.Users;
import com.rlj.pojo.bo.center.CenterUserBO;
import com.rlj.pojo.vo.UsersVO;
import com.rlj.resource.FileResource;
import com.rlj.service.FdfsService;
import com.rlj.service.center.CenterUserService;
import com.rlj.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("fdfs")
@RestController//该注解让返回的所有请求都是json对象
public class CenterUserController {
    @Autowired
    private FdfsService fdfsService;

    @Autowired
    private FileResource fileResource;

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private RedisOperator redisOperator;

    public static final String REDIS_USR_TOKEN = "redis_usr_token";

    @GetMapping("/hello")
    public Object hello(){
        return "hello world~";
    }

    @PostMapping("uploadFace")//前端center中查询用户信息的路由就是center/userInfo
    public IMOOCJSONResult uploadFace(@RequestParam String userId, MultipartFile file,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = "";//接收service返回的文件存储路径，经过一些操作得到最终存储路径，并存储在数据库中
        //开始文件上传（之前的自定义文件路径、文件路径递归生成文件夹、文件名称按照日期重塑、得到最终文件存储路径、
        //文件通过FileOutputStream写入磁盘  什么的完全都不需要了）
        if (file != null){
            String fileName = file.getOriginalFilename();
            if (StringUtils.isNotBlank(fileName)){
                //得到文件后缀
                String fileNameArr[] = fileName.split("\\.");
                String suffix = fileNameArr[fileNameArr.length - 1];
                //判断后缀名是否为指定的图片格式
                if (!suffix.equalsIgnoreCase("png")&&
                        !suffix.equalsIgnoreCase("jpg")&&
                        !suffix.equalsIgnoreCase("jpeg")){
                    return IMOOCJSONResult.errorMsg("图片格式不正确");
                }
                path = fdfsService.upload(file,suffix);
                System.out.println(path);//如果能打印出存储路径说明文件可以通过fdfs做一个成功的上传
            }
        }else {
            return IMOOCJSONResult.errorMsg("文件不能为空");
        }
        if (StringUtils.isNotBlank(path)){
            //为了不写死，这个地址写到了file.properties中"file.host=http://192.168.218.6:8888"，同时我们需要一个类去与
            //properties建立映射，每一个属性对应properties中的一个键值对的，然后用这个映射类去get这个属性就能拿到properties对应的值
            String finalUserFaceUrl = fileResource.getHost()+path;
            //更新用户头像到数据库
            Users userResult = centerUserService.updateUserFace(userId,finalUserFaceUrl);
            //之前我们写了一个分布式会话拦截器，用于在执行controller请求之前，验证是否是当前用户发起的请求。如何实现？就是用户在注册、登录的
            //时候，会生成一个令牌放到cookie中，这个令牌同时也保存在redis中（key通过userId以一定的规则生成），验证的话就拿到前端的cookie
            //和userId，通过userId拿到redis中对应的令牌和前端cookie中的令牌比较，一样就说明是同一个用户在同一台电脑登录（因为cookie是保存在
            //浏览器本地，换一台电脑cookie就没有了，就需要重新登录，那么生成的令牌也会变化）。而这里，我们上传完头像后路径更新，同时也要将
            //cookie更新，之后和注册、登录一样，更新cookie、redis中该用户对应的令牌(每一次登录、注册或者一些操作，只要涉及到更改user的cookie)
            //就应该全部更新一下，包括redis中的令牌
            String uniqueToken = UUID.randomUUID().toString().trim();
            redisOperator.set(REDIS_USR_TOKEN+":"+userResult.getId(),uniqueToken);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(userResult,usersVO);
            usersVO.setUserUniqueToken(uniqueToken);
            CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);
        }else {
            return IMOOCJSONResult.errorMsg("上传头像失败");
        }
        return IMOOCJSONResult.ok();
    }

}
