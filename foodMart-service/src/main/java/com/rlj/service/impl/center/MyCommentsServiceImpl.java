package com.rlj.service.impl.center;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rlj.enums.YesOrNo;
import com.rlj.mapper.*;
import com.rlj.pojo.OrderItems;
import com.rlj.pojo.OrderStatus;
import com.rlj.pojo.Orders;
import com.rlj.pojo.Users;
import com.rlj.pojo.bo.center.CenterUserBO;
import com.rlj.pojo.bo.center.OrderItemsCommentBO;
import com.rlj.pojo.vo.MyCommentVO;
import com.rlj.service.center.CenterUserService;
import com.rlj.service.center.MyCommentsService;
import com.rlj.utils.PagedGridResult;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MyCommentsServiceImpl implements MyCommentsService {

    @Autowired
    public OrderItemsMapper orderItemsMapper;

    @Autowired
    public OrdersMapper ordersMapper;

    @Autowired
    public OrderStatusMapper orderStatusMapper;

    @Autowired
    public ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComment(String orderId) {
        OrderItems query = new OrderItems();
        query.setOrderId(orderId);
        return orderItemsMapper.select(query);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComments(String orderId, String userId,
                             List<OrderItemsCommentBO> commentList) {

        //1. 保存评价:items_comments
        //1.1、将一个订单对应的多个商品的多个评论的list集合遍历，给每一个评论对象都通过Sid设置一个commentId
        for (OrderItemsCommentBO oic : commentList) {
            oic.setCommentId(sid.nextShort());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("commentList", commentList);
        itemsCommentsMapperCustom.saveComments(map);
        // 2. 修改订单表(orders)改已评价
        Orders order = new Orders();
        order.setId(orderId);
        order.setIsComment(YesOrNo.YES.type);
        ordersMapper.updateByPrimaryKeySelective(order);
        // 3. 修改订单状态表的留言时间:order_status
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }
    //分页方法，传入的可能是各种list，所以不写死--->List<?>
    private PagedGridResult setterPagedGird(List<?> list,Integer page){
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gird = new PagedGridResult();
        gird.setPage(page);//当前页数(请求的第几页作为参数传进来了，这里也要返回回去)
        gird.setRows(list);//总页数
        gird.setTotal(pageList.getPages());//总记录数
        gird.setRecords(pageList.getTotal());//每行显示的内容
        return gird;
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        PageHelper.startPage(page, pageSize);
        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);
        return setterPagedGird(list, page);
    }


}
