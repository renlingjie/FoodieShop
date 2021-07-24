package com.rlj.service.impl;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.rlj.service.FdfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Renlingjie
 * @name
 * @date 2021-07-22
 */
@Service
public class FdfsServiceImpl implements FdfsService {
    @Autowired
    FastFileStorageClient fastFileStorageClient;//Storage服务器的客户端
    @Override
    public String upload(MultipartFile file,String fileExtName) throws Exception {
        //通过这个方法就会做一个上传的动作，返回给我们的就是这个文件上传后的存储路径信息对象（可以get路径名、全路径、组卷名等）
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileExtName, null);
        String path = storePath.getFullPath();
        return path;
    }
}
