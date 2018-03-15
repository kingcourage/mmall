package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author wcy
 * 2018/1/16
 */
public interface IFileService {
    public String upload(MultipartFile file, String path);
}
