package com.rlj.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.rlj.es.pojo.Items;
import com.rlj.es.pojo.Stu;
import com.rlj.service.ItemsEsService;
import com.rlj.utils.PagedGridResult;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Renlingjie
 * @name
 * @date 2021-07-13
 */
@Service
public class ItemsEsServiceImpl implements ItemsEsService {

    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    @Override
    public PagedGridResult searchItems(String keywords, String sort, Integer pageIndex, Integer pageSize) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                //定义查询内容：第一个参数表示要查询的内容，后面的参数规定从哪些"字段"(一个或多个，这里仅指定一个itemName)中间进行查询
                .withQuery(QueryBuilders.multiMatchQuery(keywords,"itemName"))
                //指定查询结果中上面的查询内容如何高亮，每个"字段"都需要设置各自的样式（拼接html标签，自定义标签样式）
                .withHighlightFields(//这里指定如果命中记录的name字段中如果有"ES"的需要填充的样式
                        new HighlightBuilder.Field("itemName").preTags("<font color='red'>").postTags("</font>"))
                //指定按照哪个"字段"进行升/降排序
                //.withSort(SortBuilders.fieldSort("stuId").order(SortOrder.DESC))
                //指定分页(从哪一页开始，每页多少记录)
                .withPageable(PageRequest.of(pageIndex, pageSize))
                .build();
        // 根据查询对象得到查询结果
        SearchHits<Items> search = esTemplate.search(searchQuery, Items.class);
        // 其里面的searchHits就表示命中的查询数据，它也是一个数组的形式，里面每一条记录存储一条命中结果的相关信息
        List<SearchHit<Items>> searchHits = search.getSearchHits();
        // 设置一个需要返回的实体类集合，因为在searchHits中的每一条记录，有两大部分，一部分是普通的查询结果，一种是拼接了前后缀的高亮查询结果，
        // 后者存在于该记录的的highlightFields属性中，所以我们将上述searchHits遍历，拿到这个属性，然后存储到我们的实体类集合中
        List<Items> itemsList = new ArrayList<>();
        // 遍历返回的内容进行处理
        for(SearchHit<Items> searchHit : searchHits){
            // 高亮的内容
            Map<String, List<String>> highLightFields = searchHit.getHighlightFields();
            // 将存在高亮的内容（字段）填充到content中，每个content中其实就是一个Items对象。比如说它原来查询的itenName是"小蛋糕好吃"，
            // keywords是"蛋糕"，现在在就替换为highLightFields的"小<font color='red'>蛋糕</font>好吃"
            searchHit.getContent().setItemName(highLightFields.get("itemName") == null ? searchHit.getContent().getItemName() : highLightFields.get("itemName").get(0));
            itemsList.add(searchHit.getContent());
        }

        //新建一个查询数量的方法，用于创建分页所需的总记录数
        NativeSearchQuery searchQueryOfCount = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keywords,"itemName")).build();
        int totalCount = (int)esTemplate.count(searchQueryOfCount, Items.class);


        //pageHapler其实又很多分页方法，我们可以使用它的public Page(int[] rowBounds, boolean count)方法自定义分页
        //我们上面通过pageIndex/pageSize已经查到指定页码的指定数量的记录，因此我们可以根据这个确定rowBounds参数
        int startWith = pageIndex*pageSize;    //第几条记录开始
        int endWith = startWith + itemsList.size();    //第几条记录结束(因为itemsList.size()<=pageSize)，所以用前者
        int[] rowBounds = {startWith,endWith};
        //创建分页对象，第二个参数不知道干嘛的
        Page page = new Page(rowBounds,true);
        PagedGridResult gird = new PagedGridResult();
        //当前页数，(请求的第几页作为参数传进来了，这里也要返回回去)。之前为了迎合ES减1，而现在我们又回到正常的水准了，所以加1复原
        gird.setPage(pageIndex+1);//当前页数
        gird.setRows(itemsList);//每行显示的内容
        int totalPage;
        if (totalCount%pageSize==0){
            totalPage = totalCount/pageSize;
        }else {
            totalPage = totalCount/pageSize + 1;
        }
        gird.setTotal(totalPage);//总页数
        gird.setRecords(totalCount);//总记录数
        return gird;
    }

}
