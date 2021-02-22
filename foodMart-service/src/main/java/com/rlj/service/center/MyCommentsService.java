package com.rlj.service.center;

import com.rlj.pojo.OrderItems;
import com.rlj.pojo.Users;
import com.rlj.pojo.bo.center.CenterUserBO;
import com.rlj.pojo.bo.center.OrderItemsCommentBO;
import com.rlj.utils.PagedGridResult;

import java.util.List;

public interface MyCommentsService {

    //根据订单id查询关联的商品
    public List<OrderItems> queryPendingComment(String orderId);

    //保存用户的评论
    public void saveComments(String orderId, String userId, List<OrderItemsCommentBO> commentList);


    //我的评价查询(分页)
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize);
}
