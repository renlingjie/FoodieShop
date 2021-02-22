package com.rlj.controller.center;

import com.rlj.controller.BaseController;
import com.rlj.pojo.Orders;
import com.rlj.pojo.Users;
import com.rlj.pojo.bo.center.CenterUserBO;
import com.rlj.pojo.vo.OrderStatusCountsVO;
import com.rlj.resource.FileUpload;
import com.rlj.service.OrderService;
import com.rlj.service.center.CenterUserService;
import com.rlj.service.center.MyOrdersService;
import com.rlj.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@Api(value = "用户中心我的订单",tags = {"用户中心我的订单相关接口"})
@RequestMapping("myorders")
@RestController//该注解让返回的所有请求都是json对象
public class MyOrdersController extends BaseController {
    @Autowired
    private MyOrdersService myOrdersService;
    @Autowired
    private FileUpload fileUpload;
    @ApiOperation(value = "查看订单列表(分页)", notes = "查看订单列表(分页)", httpMethod = "POST")
    @PostMapping("/query")
    //前端的那个请求（serverUrl+'/items/comments?itemId='+itemId+"&level="+level+"&page="+page+"&pageSize="+pageSize,{}）
    //这里的参数都是请求参数，而不是像之前是路径参数，所以这里@PathVariable--->@RequestParam
    public IMOOCJSONResult query(@RequestParam String userId,@RequestParam Integer orderStatus,
                                    @RequestParam Integer page,@RequestParam Integer pageSize) {
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        System.out.println("订单状态"+orderStatus);
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMENT_PAGE_SIZE;//直接用评论的10
        }
        PagedGridResult gird = myOrdersService.queryMyOrders(userId, orderStatus, page, pageSize);
        return IMOOCJSONResult.ok(gird);
    }
    // 商家发货没有后端，所以这个接口仅仅只是用于模拟
    @ApiOperation(value="商家发货", notes="商家发货", httpMethod = "POST")
    @PostMapping("/deliver")
    public IMOOCJSONResult deliver(
            @RequestParam String orderId,
            @RequestParam String userId) throws Exception {

        if (StringUtils.isBlank(orderId)) {
            return IMOOCJSONResult.errorMsg("订单ID不能为空");
        }
        IMOOCJSONResult checkResult = checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()) {
            return checkResult;
        }
        myOrdersService.updateDeliverOrderStatus(orderId);
        return IMOOCJSONResult.ok();
    }
    @ApiOperation(value="用户确认收货", notes="用户确认收货", httpMethod = "POST")
    @PostMapping("/confirmReceive")
    public IMOOCJSONResult confirmReceive(
            @RequestParam String orderId,
            @RequestParam String userId) throws Exception {

        IMOOCJSONResult checkResult = checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()) {
            return checkResult;
        }

        boolean res = myOrdersService.updateReceiveOrderStatus(orderId);
        if (!res) {
            return IMOOCJSONResult.errorMsg("订单确认收货失败！");
        }

        return IMOOCJSONResult.ok();
    }
    @ApiOperation(value="用户删除订单", notes="用户删除订单", httpMethod = "POST")
    @PostMapping("/delete")
    public IMOOCJSONResult delete(
            @RequestParam String orderId,
            @RequestParam String userId) throws Exception {
        IMOOCJSONResult checkResult = checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()) {
            return checkResult;
        }

        boolean res = myOrdersService.deleteOrder(userId, orderId);
        if (!res) {
            return IMOOCJSONResult.errorMsg("订单删除失败！");
        }
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value="获得订单状态数概况", notes="获得订单状态数概况", httpMethod = "POST")
    @PostMapping("/statusCounts")
    public IMOOCJSONResult statusCounts(@RequestParam String userId) {
        if (StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }
        OrderStatusCountsVO result = myOrdersService.getOrderStatusCounts(userId);
        return IMOOCJSONResult.ok(result);
    }

    @ApiOperation(value="查询订单动向", notes="查询订单动向", httpMethod = "POST")
    @PostMapping("/trend")
    public IMOOCJSONResult trend(@RequestParam String userId,@RequestParam Integer page,
                                 @RequestParam Integer pageSize) {
        if (StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMENT_PAGE_SIZE;//直接用评论的10
        }
        PagedGridResult gird = myOrdersService.getOrdersTrend(userId,page,pageSize);
        return IMOOCJSONResult.ok(gird);
    }

    //用于验证用户和订单是否有关联关系，避免用户非法调用
    private IMOOCJSONResult checkUserOrder(String userId, String orderId) {
        Orders order = myOrdersService.queryMyOrder(userId, orderId);
        if (order == null) {
            return IMOOCJSONResult.errorMsg("订单不存在！");
        }
        return IMOOCJSONResult.ok();
    }

}
