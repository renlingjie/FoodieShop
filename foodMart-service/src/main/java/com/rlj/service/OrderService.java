package com.rlj.service;

import com.rlj.pojo.Carousel;
import com.rlj.pojo.OrderStatus;
import com.rlj.pojo.bo.SubmitOrderBO;
import com.rlj.pojo.vo.OrderVO;
import tk.mybatis.mapper.annotation.Order;

import java.util.List;

public interface OrderService {
    //1、创建一个订单，并将前端需要的生成的订单ID返回，所以返回值是String
    public OrderVO createOrder(SubmitOrderBO submitOrderBO);
    //2、接收到支付中心付款成功的消息后，需要将订单状态改为已付款
    public void updateOrderStatus(String orderId,Integer orderStatus);
    //3、查询订单状态
    public OrderStatus queryOrderStatusInfo(String orderId);
    //4、关闭超时未支付订单
    public void closeOrder();
}
