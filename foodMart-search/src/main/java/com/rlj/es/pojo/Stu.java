package com.rlj.es.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

@Document(indexName = "stu")//ES7就没有Type这个属性了
public class Stu {
    @Id  //org.springframework.data.annotation
    private Long stuId;
    //org.springframework.data.elasticsearch.annotations
    @Field(store = true)  //设置为true表示要进行存储
    private String name;
    @Field(store = true)  //org.springframework.data.elasticsearch.annotations
    private Integer age;

    public Long getStuId() {
        return stuId;
    }

    public void setStuId(Long stuId) {
        this.stuId = stuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Stu{" +
                "stuId=" + stuId +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
