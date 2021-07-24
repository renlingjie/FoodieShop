package com.rlj.resource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/**
 * @author Renlingjie
 * @name
 * @date 2021-07-23
 */

@Component
@PropertySource("classpath:file.properties")
@ConfigurationProperties(prefix = "file")
public class FileResource {
    private String host;//加上前缀后就是properties中的file.host这个键
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
}
