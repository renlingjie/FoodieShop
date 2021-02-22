package com.rlj.service.center;

import com.rlj.pojo.Orders;
import com.rlj.pojo.Users;
import com.rlj.pojo.bo.center.CenterUserBO;
import com.rlj.pojo.vo.OrderStatusCountsVO;
import com.rlj.utils.PagedGridResult;


public interface MyOrdersService {
    //1、查询我的订单列表
    public PagedGridResult queryMyOrders(String userId,Integer orderStatus,
                                         Integer page,Integer pageSize);
    //2、更改订单状态为商家发货
    public void updateDeliverOrderStatus(String orderId);
    //3、更改订单状态为确认收货
    public boolean updateReceiveOrderStatus(String orderId);
    //4、检查订单ID和用户ID是否关联(如果根据这两个参数查出来了说明有关联)，防止恶意攻击
    public Orders queryMyOrder(String userId, String orderId);
    //5、删除订单(逻辑删除)
    public boolean deleteOrder(String userId, String orderId);
    //6、获取订单状态的计数
    public OrderStatusCountsVO getOrderStatusCounts(String userId);
    //7、获取订单状态为20/30/40的动向(要分页)
    public PagedGridResult getOrdersTrend(String userId,Integer page,Integer pageSize);
}
