package com.rlj.controller;

import com.rlj.enums.OrderStatusEnum;
import com.rlj.enums.PayMethod;
import com.rlj.pojo.OrderStatus;
import com.rlj.pojo.UserAddress;
import com.rlj.pojo.bo.AddressBO;
import com.rlj.pojo.bo.ShopcartBO;
import com.rlj.pojo.bo.SubmitOrderBO;
import com.rlj.pojo.vo.MerchantOrdersVO;
import com.rlj.pojo.vo.OrderVO;
import com.rlj.service.AddressService;
import com.rlj.service.OrderService;
import com.rlj.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "订单相关",tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController//该注解让返回的所有请求都是json对象
public class OrdersController extends BaseController{
    @Autowired
    private OrderService orderService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisOperator redisOperator;
    //1、创建订单
    //刷新购物车中的数据(主要是商品价格)
    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    //前端的那个请求（serverUrl+'/orders/create,'+{xxxxxx}）参数是json，所以@RequestBody
    public IMOOCJSONResult create(@RequestBody SubmitOrderBO submitOrderBO,
                                  HttpServletRequest request, HttpServletResponse response) {
        if (submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type
        && submitOrderBO.getPayMethod() != PayMethod.ALIPAY.type){
            return IMOOCJSONResult.errorMsg("支付方式不支持");
        }

        String shopCartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
        if (StringUtils.isBlank(shopCartJson)){
            return IMOOCJSONResult.errorMsg("购物车数据不正确");
        }
        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopCartJson,ShopcartBO.class);

        //1、创建订单
        OrderVO orderVO = orderService.createOrder(shopcartList,submitOrderBO);
        String orderId = orderVO.getOrderId();
        //2、创建订单以后，移除购物车中已结算的商品
        //整合Redis后，完善购物车中的已结算商品清除，并且同步到前端的Cookie
        //将已经结算的商品从我们的购物车Redis中清除，清除后的shopcartList就是最新的，更新到Redis中
        shopcartList.removeAll(orderVO.getToBeRemovedShopcartList());
        redisOperator.set(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(),JsonUtils.objectToJson(shopcartList));
        //使用上面最新的shopcartList更新Cookie
        CookieUtils.setCookie(request,response,FOODIE_SHOPCART,JsonUtils.objectToJson(shopcartList),true);
        //3、向支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("imoocUserId","imooc");
        httpHeaders.add("password","imooc");
        //要传入的参数(一个是封装了订单的的merchantOrdersVO，一个是支付中心的账号密码httpHeaders)
        HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(merchantOrdersVO,httpHeaders);
        //三个参数：支付中心对应的路由、要传给支付中心的路由、返回的类型，这样就通过restTemplate变为RestFul风格请求
        ResponseEntity<IMOOCJSONResult> responseEntity =
                restTemplate.postForEntity(paymentUrl, entity, IMOOCJSONResult.class);
        //拿到支付中心返回的结果
        IMOOCJSONResult paymentResult = responseEntity.getBody();
        if (paymentResult.getStatus() != 200){
            return IMOOCJSONResult.errorMsg("支付中心订单创建失败，请联系订单管理员");
        }
        return IMOOCJSONResult.ok(orderId);
    }
    //构建商户端支付成功的回调接口
    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId){
        //传入要修改状态的订单的ID，同时将订单状态设置为已付款待发货
        System.out.println("回调接口传入的ID是"+merchantOrderId);
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();//直接返回200状态码
    }
    //返回前端订单状态的接口
    @PostMapping("getPaidOrderInfo")
    public IMOOCJSONResult queryOrderStatusInfo(String orderId){
        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        //System.out.println("接收到请求，订单状态为"+orderStatus);
        return IMOOCJSONResult.ok(orderStatus);
    }
}
