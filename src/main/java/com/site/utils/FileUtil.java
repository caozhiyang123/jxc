package com.site.utils;

import com.jfinal.kit.StrKit;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;

/**
 * Created by admin on 2016/6/25.
 */
public class FileUtil {
    /**
     * 获取文件MD5
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        FileInputStream in = null;
        byte buffer[] = new byte[8192];
        int len;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存文件信息
     * @param type
     * @param map
     * @return
     */
//    public static boolean saveInfo(String type,Map map){
//        if (StrKit.isBlank(type)){
//            return false;
//        }
//        if (map == null){
//            return false;
//        }
//        switch(type) {
//            case "image":
//                Image image = (Image) ToolFunction.me.mapToModel(map,new Image(),null);
//                image.setCreateTime(new Date());
//                image.save();
//                return true;
//            default:
//                return false;
//        }
//    }
//
//    /**
//     * 根据MD5值查找文件路径
//     * @param md5
//     * @return
//     */
//    public static Map<String,Object> getUrlByMd5(String md5){
//        Image image = Image.dao.findByMd5(md5);
//        if (image!=null && image.getUrl()!=null){
//            return ToolFunction.me.modelToMap(image,null);
//        }else {
//            return null;
//        }
//    }

    /**
     * 下载文件
     * @param urlString
     * @param downloadPath
     * @param filename
     * @throws Exception
     */
    public void download(String urlString,String downloadPath, String filename) throws Exception {
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        // 输入流
        InputStream is = con.getInputStream();
        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        //创建文件夹
        File file =new File(downloadPath);
        //如果文件夹不存在则创建
        if  (!file .exists()  && !file .isDirectory()){
            System.out.println("创建文件夹："+downloadPath);
            file .mkdir();
        }
        // 输出的文件流
        OutputStream os = new FileOutputStream(downloadPath+filename);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }

}
