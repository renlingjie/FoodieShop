package com.rlj.controller;

import com.rlj.enums.YesOrNo;
import com.rlj.pojo.Carousel;
import com.rlj.pojo.Category;
import com.rlj.pojo.vo.CategoryVO;
import com.rlj.pojo.vo.NewItemsVO;
import com.rlj.service.CarouselService;
import com.rlj.service.CategoryService;
import com.rlj.utils.IMOOCJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "首页",tags = {"首页展示的相关接口"})
@RequestMapping("index")
@RestController
public class IndexController {
    @Autowired
    private CarouselService carouselService;
    @Autowired
    private CategoryService categoryService;
    @ApiOperation(value = "获取首页轮播图列表",notes = "获取首页轮播图列表",httpMethod = "GET")
    @GetMapping("/carousel")
    public IMOOCJSONResult carousel(){
        //传入的参数是isShow,这里我们使用枚举类(否则你来一个1/0就把它写死了)
        List<Carousel> list = carouselService.queryAll(YesOrNo.YES.type);
        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }

    @ApiOperation(value = "获取商品分类（一级分类）",notes = "获取商品分类（一级分类）",httpMethod = "GET")
    @GetMapping("/cats")//前端的那个请求（serverUrl+'/index/cats',{}）
    public IMOOCJSONResult cats(){
        List<Category> list = categoryService.queryAllRootLevelCat();
        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }

    @ApiOperation(value = "获取商品子分类",notes = "获取商品子分类",httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")//前端的那个请求（serverUrl+'/index/subCat',{}）
    public IMOOCJSONResult subCat(@PathVariable Integer rootCatId){
        if (rootCatId == null){
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        List<CategoryVO> list = categoryService.getSubCatList(rootCatId);
        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }

    @ApiOperation(value = "查询每个一级分类下的最新6条商品记录",notes = "查询每个一级分类下的最新6条商品记录",httpMethod = "GET")
    @GetMapping("/sixNewItems/{rootCatId}")//前端的那个请求（serverUrl+'/index/sixNewItems',{}）
    public IMOOCJSONResult sixNewItems(@PathVariable Integer rootCatId){
        if (rootCatId == null){
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
        return IMOOCJSONResult.ok(list);//IMOOCJSONResult可以通过ok返回内容，所以传入结果list
    }
}
