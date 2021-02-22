package com.rlj.controller;

import com.rlj.mapper.StuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//该注解让返回的所有请求都是json对象
@RestController
public class HelloController {
    //slf4j的;记录HelloController这个类的所有内容
    final static Logger logger= LoggerFactory.getLogger(HelloController.class);
    @GetMapping("/hello")
    public Object hello(){
        logger.info("info:hello");
        logger.debug("debug:hello");
        logger.error("error:hello");
        logger.warn("warn:hello");
        return "hello world~";
    }
}
