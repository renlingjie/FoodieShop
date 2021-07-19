package com.rlj.controller;

import com.rlj.service.ItemsEsService;
import com.rlj.utils.IMOOCJSONResult;
import com.rlj.utils.PagedGridResult;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//该注解让返回的所有请求都是json对象
@RestController
@RequestMapping("items")
public class ItemsController {

    @Autowired
    private ItemsEsService itemsEsService;

    @GetMapping("/search")
    public IMOOCJSONResult search(String keywords, String sort,
                                  Integer page, Integer pageSize) {
        if (StringUtils.isBlank(keywords)) {
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null) {
            page = 0;//ES的分页默认是从0开始的
        }else {
            page--;//ES的分页默认是从0开始的，我们查询第一页发送的请求就是1，其实是要为0，所以减1
        }
        if (pageSize == null) {
            pageSize = 10;//在这里设置死每页查询10条记录
        }
        PagedGridResult gird = itemsEsService.searchItems(keywords, sort, page, pageSize);
        return IMOOCJSONResult.ok(gird);
    }

}
