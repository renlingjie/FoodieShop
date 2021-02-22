package com.rlj.controller;

import com.rlj.pojo.bo.ShopcartBO;
import com.rlj.utils.IMOOCJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "购物车接口controller",tags = {"购物车接口相关的api接口"})
@RequestMapping("shopcart")
@RestController//该注解让返回的所有请求都是json对象
public class ShopCartController {
    @ApiOperation(value = "添加商品到购物车",notes = "添加商品到购物车",httpMethod = "POST")
    @PostMapping("/add")//下面的又是"?XXX=xxx&YYY=yyy"，所以是@RequestParam
    public IMOOCJSONResult add(@RequestParam String userId, @RequestBody ShopcartBO shopcartBO,
                               //涉及到cookie，就需要HttpServletRequest/HttpServletResponse
                               HttpServletRequest request, HttpServletResponse response
    ){
        //简单做一个判断，后期完善（使用下面的那个TxDO，作为一个要做的标签）
        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg("");
        }
        //打印输出测试前端传来的数据
        System.out.println(shopcartBO);
        //TODO 前端用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存
        return IMOOCJSONResult.ok();
    }
    @ApiOperation(value = "从购物车中删除商品",notes = "从购物车中删除商品",httpMethod = "POST")
    @PostMapping("/del")//下面的又是"?XXX=xxx&YYY=yyy"，所以是@RequestParam
    public IMOOCJSONResult del(@RequestParam String userId, @RequestParam String itemSpecId,
                               //涉及到cookie，就需要HttpServletRequest/HttpServletResponse
                               HttpServletRequest request, HttpServletResponse response
    ){
        //简单做一个判断，后期完善（使用下面的那个TxDO，作为一个要做的标签）
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)){
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }
        //TODO 用户在页面删除购物车中的数据，如果此时用户已经登录，则在后端同步删除redis缓存
        return IMOOCJSONResult.ok();
    }
}
