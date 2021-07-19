package com.rlj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;


//之前引入了SpringSession的依赖，所以启动项目后首次发送请求会跳转到Session的登录页面，我们现排除SpringSession
//为我们创建的分布式会话（这是其中一种，比较推荐的是我们自己创建一个token存储到Redis中来创建分布式令牌，通过这个令牌
//就实现了分布式会话的模式，详情请看第4章最开始的分布式会话的那一部分）
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
