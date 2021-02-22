package com.rlj.service;

import com.rlj.pojo.Users;
import com.rlj.pojo.bo.UserBO;
import org.springframework.stereotype.Service;


public interface UserService {
    /**
     * 判断用户名是否存在
     */
    public boolean queryUsernameIsExist(String username);
    /**
     * 创建用户
     */
    public Users createUser(UserBO userBO);//userBO用来封装前端请求的用户名和密码
    /**
     * 用户登录(检索用户名和密码是否匹配)
     */
    public Users queryUserForLogin(String username,String password);
}
