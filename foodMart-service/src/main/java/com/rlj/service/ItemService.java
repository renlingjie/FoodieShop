package com.rlj.service;

import com.rlj.pojo.*;
import com.rlj.pojo.vo.CommentLevelCountsVO;
import com.rlj.pojo.vo.ItemCommentVO;
import com.rlj.pojo.vo.ShopcartVO;
import com.rlj.utils.PagedGridResult;

import java.util.List;

public interface ItemService {
    //1、根据(三级)商品ID查询商品基本信息
    public Items queryItemById(String itemId);
    //2、根据(三级)商品ID查询商品图片列表
    public List<ItemsImg> queryItemImgList(String itemId);
    //3、根据(三级)商品ID查询商品规格
    public List<ItemsSpec> queryItemSpecList(String itemId);
    //4、根据(三级)商品ID查询商品参数
    public ItemsParam queryItemParam(String itemId);
    //5、根据ID查询查询商品评价等级的数量
    public CommentLevelCountsVO queryCommentCounts(String itemId);
    //6、根据商品ID、评价等级，查询商品评价列表(需要分页的)
    public PagedGridResult queryPagedComments(String itemId, Integer level, Integer page, Integer pageSize);
    //7、搜索商品列表(需要分页的)
    public PagedGridResult searchItems(String keyWords, String sort, Integer page, Integer pageSize);
    //8、根据三级分类的商品ID搜索商品列表(需要分页的)
    public PagedGridResult searchItemsByThirdCat(Integer catId, String sort, Integer page, Integer pageSize);
    //9、根据规格ids查询最新的购物车中的商品数据(用于刷新渲染购物车中的商品数据)
    public List<ShopcartVO> queryItemsBySpecIds(String specIds);
    //10、根据规格id查询对应的商品规格的信息
    public ItemsSpec queryItemsSpecById(String specId);
    //11、根据商品id获取商品图片主图的url
    public String queryItemMainImgById(String itemId);
    //12、在用户提交订单之后，规格表中需要扣除库存
    public void decreaseItemSpecStock(String specId,int buyCounts);
}
