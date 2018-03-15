package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author wcy
 * 2018/1/16
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService{

    private static Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file, String path){
        String fileName =  file.getOriginalFilename();
        //拓展名
        String fileExtensionName = fileName.substring(fileName.indexOf(".")+1);
        String uploadFileName= UUID.randomUUID().toString()+"."+fileExtensionName;

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);

        try{
            file.transferTo(targetFile);
            //文件已经上传成功

            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器上
            targetFile.delete();
        }catch (IOException e){
            log.error("上传文件异常",e);
        }
        return targetFile.getName();
    }
}
