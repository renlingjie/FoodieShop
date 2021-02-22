package com.rlj.service.center;

import com.rlj.pojo.Users;
import com.rlj.pojo.bo.UserBO;
import com.rlj.pojo.bo.center.CenterUserBO;


public interface CenterUserService {
    //1、根据用户ID查询用户信息
    public Users queryUserInfo(String userId);
    //2、修改用户信息
    public Users updateUserInfo(String userId, CenterUserBO centerUserBO);
    //3、用户头像更新
    public Users updateUserFace(String userId, String faceUrl);
}
