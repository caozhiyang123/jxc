package com.site.controller;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.jfinal.upload.UploadFile;
import com.site.base.BaseController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传统一处理类
 */
public class FileController extends BaseController {

    public void upload() {
        List<String> fileList = new ArrayList<>();
        String saveDirectory = PropKit.get("file.dir");
        List<UploadFile> uploadFileList = getFiles();
        if (uploadFileList == null) {
            System.out.println("上传失败！");
            this.renderJson("上传失败！");
            return;
        }
        JSONObject result = new JSONObject();
        for (UploadFile uploadFile : uploadFileList) {
            String originalName = uploadFile.getFileName();
            String sufName = originalName.substring(originalName.lastIndexOf("."), originalName.length());
            String prefixName = originalName.substring(0, originalName.lastIndexOf("."));
            String fileName = prefixName + "_" + System.currentTimeMillis() + sufName;
            File dest = new File(saveDirectory + File.separator + fileName);
            uploadFile.getFile().renameTo(dest);
            System.out.println("上传成功！文件位置：" + saveDirectory + File.separator + fileName);
            //获取http和https
            String requestUrl = getRequest().getRequestURL().toString();
            String httpPath = requestUrl.substring(0, requestUrl.indexOf("//") + 2);
//            String filePath = httpPath + getRequest().getServerName() + ":" + getRequest().getServerPort() + "/file/" + fileName;
            String filePath = httpPath + getRequest().getServerName() + ":8080/file/" + fileName;
            fileList.add(filePath);
            result.put("name", fileName);
            result.put("originalName", originalName);
            result.put("size", dest.length());
            result.put("state", "SUCCESS");
            result.put("type", sufName);
            result.put("url", "file/" + fileName);
        }
        renderJson(result);
    }
}
