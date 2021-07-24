package com.rlj.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Renlingjie
 * @name
 * @date 2021-07-22
 */
public interface FdfsService {
    public String upload(MultipartFile file, String fileExtName) throws Exception;
}
