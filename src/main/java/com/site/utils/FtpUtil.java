package com.site.utils;

import com.jfinal.kit.PropKit;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by richard on 2016/8/18.
 */
public class FtpUtil {

    /**
     * ftp上传单个文件
     *
     * @param directory   上传至ftp的路径名不包括ftp地址
     * @param srcFileName 要上传的文件全路径名
     * @param destName    上传至ftp后存储的文件名
     * @throws IOException
     */
    public static boolean upload(String directory, String srcFileName, String destName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        boolean result = false;
        try {
            String ftpUrl = PropKit.get("ftp.url");
            Integer port = PropKit.getInt("ftp.port");
            String userName = PropKit.get("ftp.userName");
            String password = PropKit.get("ftp.password");

            ftpClient.connect(ftpUrl, port);
            ftpClient.login(userName, password);
            ftpClient.enterLocalPassiveMode();
            File srcFile = new File(srcFileName);
            fis = new FileInputStream(srcFile);
            // 设置上传目录
            ftpClient.changeWorkingDirectory(directory);
            ftpClient.setBufferSize(1024);
//            ftpClient.setControlEncoding("GBK");
            // 设置文件类型（二进制）
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            result = ftpClient.storeFile(new String(destName.getBytes("GBK"), "iso-8859-1"), fis);
            return result;
        } catch (NumberFormatException e) {
            System.out.println("FTP端口配置错误:不是数字:");
            throw e;
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            if (fis != null)
                fis.close();
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
    }

    public static boolean removeFile(String srcFname) throws IOException {
        FTPClient ftpClient = new FTPClient();
        boolean result = false;
        String ftpUrl = PropKit.get("ftp.url");
        Integer port = PropKit.getInt("ftp.port");
        String userName = PropKit.get("ftp.userName");
        String password = PropKit.get("ftp.password");

        ftpClient.connect(ftpUrl, port);
        ftpClient.login(userName, password);
        ftpClient.enterLocalPassiveMode();
        result = ftpClient.deleteFile(srcFname);

        return result;
    }

}
