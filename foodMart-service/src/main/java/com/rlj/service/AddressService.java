package com.rlj.service;

import com.rlj.pojo.Carousel;
import com.rlj.pojo.UserAddress;
import com.rlj.pojo.bo.AddressBO;

import java.util.List;

public interface AddressService {
    //1、查询用户的所有收货地址列表
    public List<UserAddress> queryAll(String userId);
    //2、用户新增地址
    public void addNewUserAddress(AddressBO addressBO);
    //3、用户修改地址
    public void updateUserAddress(AddressBO addressBO);
    //4、用户删除地址
    public void deleteUserAddress(String userId,String addressId);
    //5、修改默认地址
    public void updateUserDefaultAddress(String userId,String addressId);
    //6、根据用户ID和地址ID（查询全部地址只需要用户ID），查询出前端用户指定的地址信息
    public UserAddress queryUserAddress(String userId,String addressId);
}
