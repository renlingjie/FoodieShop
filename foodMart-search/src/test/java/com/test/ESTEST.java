package com.test;

import com.rlj.Application;
import com.rlj.es.pojo.Stu;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Elasticsearch 7.6.2 版本 @Document 注解 shards配置不生效
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ESTEST {
    @Autowired
    private ElasticsearchRestTemplate esTemplate;
    //------------------对索引结构的操作，不推荐通过Java代码来实现--------
    //1、创建文档
    @Test
    public void createIndexStu(){
        Stu stu = new Stu();
        stu.setStuId(1002l);
        stu.setName("real man");
        stu.setAge(16);
        IndexCoordinates indexCoordinates = esTemplate.getIndexCoordinatesFor(stu.getClass());
        IndexQuery indexQuery = new IndexQueryBuilder().withObject(stu).build();

        esTemplate.index(indexQuery,indexCoordinates);
    }
    //2、删除索引
    @Test
    public void deleteIndexStu(){
        //deleteIndex(Class<?> clazz)  传入要删除的类即可删除它indexName对应的索引
        esTemplate.deleteIndex(Stu.class);
    }

    //--------------------下面就是对文档（一个文档就是一条数据，一个type就是数据中的一个属性）数据的操作--------
    //1、更新文档某记录
    @Test
    public void updateDoc(){
        //创建一个文档对象，向里面放入要更新的数据（"字段名:值"的形式）
        Document document = Document.create();
        document.put("name", "update ES");
        UpdateQuery build = UpdateQuery.builder("1001").//指定要更新的文档的ID
                withDocument(document) .//将上面加入更新内容的文档放进去
                withScriptedUpsert(true) .build();
        //里面放入索引名称，我们要更新的是Stu类对应的索引，对应indexName = "stu"
        esTemplate.update(build, IndexCoordinates.of("stu"));
    }

    //2、查询文档某记录
    @Test
    public void getStuDoc(){
        GetQuery query = new GetQuery("1002");
        Stu stu = esTemplate.queryForObject(query, Stu.class);//第一个参数就是查询条件，第二个参数就是索引的实体映射（位置）
        System.out.println(stu);
    }

    //3、删除文档某记录
    @Test
    public void deleteStuDoc(){
        //传入要删除文档记录的ID以及对应索引的实体映射
        //delete(String id, Class<?> entityType)
        esTemplate.delete("1002",Stu.class);
    }

    //------------------------进阶（分页、高亮）-------------------------------
    //1、分页查询多条记录（文档）
    @Test
    public void searchStuDoc(){
        //Query（参数1）的查询条件集中的分页条件.org.springframework.data.domain.Pageable;
        Pageable pageable = PageRequest.of(0,10);//查第0页，每页显示10条数据
        //参数1是一个Query，里面传入一个或多个查询条件
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                //查询条件1，指定查询需求，可以有多个(查询name"字段"的属性有ES的数据)
                .withQuery(QueryBuilders.matchQuery("name","ES"))
                .withPageable(pageable)//查询条件2，指定查询的分页需求（第几页？每页几条数据？）
                .build();
        //参数3
        IndexCoordinates indexCoordinates = esTemplate.getIndexCoordinatesFor(Stu.class);
        //AggregatedPage里面就是包含了分页结果的信息(类似于我们的pageable，可以拿到分页内容、总记录数、第几页等)
        AggregatedPage<Stu> stus = esTemplate.queryForPage(query, Stu.class, indexCoordinates);
        stus.getContent().forEach(a -> System.out.println(a));//能把a（也就是stu类对象）打印出来的条件是要写toString方法
    }
    //2、分页、高亮、排序
    @Test
    public void highlightStuDoc(){
        // 定义一个综合上述条件的查询对象
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                //定义查询内容：第一个参数表示要查询的内容，后面的参数规定从哪些"字段"(一个或多个，这里仅指定一个name)中间进行查询
                .withQuery(QueryBuilders.multiMatchQuery("ES", "name"))
                //指定查询结果中上面的查询内容如何高亮，每个"字段"都需要设置各自的样式（拼接html标签，自定义标签样式）
                .withHighlightFields(//这里指定如果命中记录的name字段中如果有"ES"的需要填充的样式
                        new HighlightBuilder.Field("name").preTags("<font color='red'>").postTags("</font>"))
                //指定按照哪个"字段"进行升/降排序
                .withSort(SortBuilders.fieldSort("stuId").order(SortOrder.DESC))
                //指定分页(从哪一页开始，每页多少记录)
                .withPageable(PageRequest.of(0, 10))
                .build();
        // 根据查询对象得到查询结果
        SearchHits<Stu> search = esTemplate.search(searchQuery, Stu.class);
        // 其里面的searchHits就表示命中的查询数据，它也是一个数组的形式，里面每一条记录存储一条命中结果的相关信息
        List<SearchHit<Stu>> searchHits = search.getSearchHits();
        // 设置一个需要返回的实体类集合，因为在searchHits中的每一条记录，有两大部分，一部分是普通的查询结果，一种是拼接了前后缀的高亮查询结果，
        // 后者存在于该记录的的highlightFields属性中，所以我们将上述searchHits遍历，拿到这个属性，然后存储到我们的实体类集合中
        List<Stu> stuList = new ArrayList<>();
        // 遍历返回的内容进行处理
        for(SearchHit<Stu> searchHit : searchHits){
             // 高亮的内容
             Map<String, List<String>> highLightFields = searchHit.getHighlightFields();
             // 将存在高亮的内容（字段）填充到content中，每个content中其实就是一个Stu对象。比如说它原来
             // 查询的name是"ES IS GOOD"，现在就替换为highLightFields的<font color='red'>ES</font> IS GOOD
             searchHit.getContent().setName(highLightFields.get("name") == null ? searchHit.getContent().getName() : highLightFields.get("name").get(0));
             stuList.add(searchHit.getContent());
         }
        stuList.forEach(a -> System.out.println(a));
    }
}
