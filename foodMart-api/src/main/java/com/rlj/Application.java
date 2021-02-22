package com.rlj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;
//开启定时任务
@EnableScheduling
//扫描mybatis通用mapper所在的包
@MapperScan(basePackages = "com.rlj.mapper")
//扫描所有包以及相关组件（ID生成策略组件）包
@SpringBootApplication
@ComponentScan(basePackages = {"com.rlj","org.n3r.idworker"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
