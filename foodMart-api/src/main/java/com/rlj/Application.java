package com.rlj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import tk.mybatis.spring.annotation.MapperScan;
//开启定时任务
@EnableScheduling
//扫描mybatis通用mapper所在的包
@MapperScan(basePackages = "com.rlj.mapper")
//排除自动装配，避免SpringSession中第一次访问方法、cookie删除访问方法两种情况下时候需要验证登录
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描所有包以及相关组件（ID生成策略组件）包
@ComponentScan(basePackages = {"com.rlj","org.n3r.idworker"})
@EnableRedisHttpSession//开启使用Redis作为SpringSession
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
