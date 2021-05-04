package com.rlj.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

//该注解让返回的所有请求都是json对象
@RestController
@RequestMapping("redis")
public class RedisController {
    @Autowired
    private RedisTemplate redisTemplate;
    @GetMapping("/set")
    public Object set(String key,String value){
        //opsForValue方法专门是给Stirng类型来设值的
        redisTemplate.opsForValue().set(key,value);
        return "OK";
    }

    @GetMapping("/get")
    public String get(String key){
        return (String) redisTemplate.opsForValue().get(key);
    }

    @GetMapping("/delete")
    public Object delete(String key){
        //delete不再针对String类型，所有的类型都是可以直接通过delete一个key来进行删除的
        redisTemplate.delete(key);
        return "OK";
    }
}
