package com.site.utils;

import com.jfinal.kit.PropKit;
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EMailUtil {
    private MimeMessage mimeMsg;
    private Session session;
    private Properties props;
    private String username;
    private String password;
    private Multipart mp;
    public EMailUtil(String smtp, String port) {
        setSmtpHost(smtp, port);
        createMimeMessage();
    }
    public void setSmtpHost(String hostName, String port) {
        System.out.println("设置系统属性：mail.smtp.host=" + hostName);
        if (props == null) {
            props = new Properties();
        }
        props.put("mail.smtp.host", hostName);

        if (StringUtils.isNotBlank(port)) {
            props.put("mail.smtp.port", port);
            //使用 TLS 加密
            props.put("mail.smtp.starttls.enable", "true");

//            // 指定的端口连接到在使用指定的套接字工厂。如果没有设置,将使用默认端口。
//            props.put("mail.smtp.socketFactory.port", port);
//            // 如果设置,指定实现javax.net.SocketFactory接口的类的名称,这个类将被用于创建SMTP的套接字。
//            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//            // 如果设置为true,未能创建一个套接字使用指定的套接字工厂类将导致使用java.net.Socket创建的套接字类。默认值为true。
//            props.put("mail.smtp.socketFactory.fallback", "false");
//            //不做服务器证书校验
//            props.put("mail.smtp.ssl.checkserveridentity", "false");
//            //添加信任的服务器地址
//            props.put("mail.smtp.ssl.trust", "*");
        }
    }
    public boolean createMimeMessage() {
        try {
            System.out.println("准备获取邮件会话对象！");
            session = Session.getDefaultInstance(props, null);
        } catch (Exception e) {
            System.out.println("获取邮件会话错误！" + e);
            return false;
        }
        System.out.println("准备创建MIME邮件对象！");
        try {
            mimeMsg = new MimeMessage(session);
            mp = new MimeMultipart();

            return true;
        } catch (Exception e) {
            System.out.println("创建MIME邮件对象失败！" + e);
            return false;
        }
    }

    /*定义SMTP是否需要验证*/
    public void setNeedAuth(boolean need) {
        System.out.println("设置smtp身份认证：mail.smtp.auth = " + need);
        if (props == null)
            props = new Properties();
        if (need) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }
    }
    public void setNamePass(String name, String pass) {
        username = name;
        password = pass;
    }

    /*定义邮件主题*/
    public boolean setSubject(String mailSubject) {
        if (StringUtils.isBlank(mailSubject)) {
            return false;
        }
        System.out.println("开始定义邮件主题！");
        try {
            mimeMsg.setSubject(mailSubject);
            return true;
        } catch (Exception e) {
            System.err.println("定义邮件主题发生错误！");
            return false;
        }
    }

    /*定义邮件正文*/
    public boolean setBody(Map<String, String> map) {
        if (map == null || map.keySet().size() == 0) {
            return false;
        }

        String content = map.get("content");
        if (StringUtils.isBlank(content)) {
            content = setEmailTemplate(map);
        }
        System.out.println("开始定义邮件内容！");
        try {
            BodyPart bp = new MimeBodyPart();
            bp.setContent("" + content, "text/html;charset=GBK");
            mp.addBodyPart(bp);
            return true;
        } catch (Exception e) {
            System.err.println("定义邮件正文时发生错误！" + e);
            return false;
        }
    }

    /*设置发信人*/
    public boolean setFrom(String from) {
        if (StringUtils.isBlank(from)) {
            return false;
        }
        System.out.println("开始设置发信人！");
        try {
            mimeMsg.setFrom(new InternetAddress(from)); //发信人
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*定义收信人*/
    public boolean setTo(String to) {
        if (StringUtils.isBlank(to)) {
            return false;
        }
        System.out.println("开始定义收信人！");
        try {
            mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*定义抄送人*/
    public boolean setCopyTo(String copyto) {
        if (StringUtils.isBlank(copyto)) {
            return true;
        }
        System.out.println("开始定义抄送人!");
        try {
            mimeMsg.setRecipients(Message.RecipientType.CC, (Address[]) InternetAddress
                    .parse(copyto));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String setEmailTemplate(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p style=\"margin: 1em 0px; color: rgb(51, 51, 51); font-size: 16px;font-weight:bolder;\">您好：</p>");
        sb.append("<p class=\"body\" style=\"margin: 1em 0px; color: rgb(51, 51, 51); font-size: 16px;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style=\"color:#FF5722;\">" + map.get("month") + "月</span> <span style=\"color:#5FB878;\">"+ map.get("level") +"级经销商</span> 数据正在等待您审批，请尽快审批！");
        sb.append("<div class=\"body salutation\" style=\"margin: 1em 0px; color: rgb(51, 51, 51); font-size: 16px;float:right;margin-left:50%;\">来自系统邮件</div>");
        return sb.toString();
    }

    /*发送邮件模块*/
    public boolean sendOut() {
        try {
            mimeMsg.setContent(mp);
            mimeMsg.saveChanges();
            System.out.println("邮件发送中....");
            Session mailSession = Session.getInstance(props, null);
            Transport transport = mailSession.getTransport("smtp");
            transport.connect((String) props.get("mail.smtp.host"), username, password);
            System.out.println("邮件发送中....");
            transport.sendMessage(mimeMsg, mimeMsg
                    .getRecipients(Message.RecipientType.TO));
            System.out.println("发送邮件成功！");
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /*发送邮件模块*/
    public boolean sendOutByPort() {
        try {
            mimeMsg.setContent(mp);
            mimeMsg.saveChanges();
            System.out.println("邮件发送中....");
            Session mailSession = Session.getInstance(props, null);
            Transport transport = mailSession.getTransport("smtp");
            transport.connect((String) props.get("mail.smtp.host"), Integer.parseInt(props.get("mail.smtp.port").toString()), username, password);
            System.out.println("邮件发送中....");
            transport.sendMessage(mimeMsg, mimeMsg
                    .getRecipients(Message.RecipientType.TO));
            System.out.println("发送邮件成功！");
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /*调用sendOut方法完成发送*/
    public static boolean sendAndCc(String smtp, String from, String to, String copyto,
                                    String subject, Map<String, String> map, String username, String password) {
        EMailUtil theMail = new EMailUtil(smtp, null);
        theMail.setNeedAuth(true); // 验证
        if (!theMail.setFrom(from))
            return false;
        if (!theMail.setTo(to))
            return false;
        if (!theMail.setCopyTo(copyto))
            return false;
        if (!theMail.setSubject(subject))
            return false;
        if (!theMail.setBody(map))
            return false;
        theMail.setNamePass(username, password);
        if (!theMail.sendOut())
            return false;
        return true;
    }

    /*调用sendOut方法完成发送*/
    public static boolean sendAndCc(String smtp, String port, String from, String to, String copyto,
                                    String subject, Map<String, String> map, String username, String password) {
        EMailUtil theMail = new EMailUtil(smtp, port);
        theMail.setNeedAuth(true); // 验证
        if (!theMail.setFrom(from))
            return false;
        if (!theMail.setTo(to))
            return false;
        if (!theMail.setCopyTo(copyto))
            return false;
        if (!theMail.setSubject(subject))
            return false;
        if (!theMail.setBody(map))
            return false;
        theMail.setNamePass(username, password);
        if (!theMail.sendOutByPort())
            return false;
        return true;
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("level", "1");
        map.put("month", "201807");
        EMailUtil.sendAndCc("smtp.exmail.qq.com", "richard.xu@jetdata.com.cn", "291379613@qq.com", null, "测试一下", map, "richard.xu@jetdata.com.cn", "Xwd1992828");
//        EMailUtil.sendAndCc("owa.dsmpharm.com.cn", "587", "weishu@dsmpharm.com.cn", "291379613@qq.com", null, "邮件发送测试", map, "DSPC\\weishu", "123456@dsp");
    }
}