package com.site.controller;

import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;
import com.site.base.BaseController;
import com.site.utils.EMailUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TestController extends BaseController {

    private static final Log log = Log.getLog(NoticeController.class);

    public void sendEmail() {
        String port = getPara("port");
        String host = PropKit.get("email.smtp");
        String from = PropKit.get("email.from");
        String copyto = PropKit.get("email.copyto");
        String username = PropKit.get("email.username");
        String password = PropKit.get("email.password");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("content", "测试一下");
        if (StringUtils.isNotBlank(port)) {
            EMailUtil.sendAndCc(host, port, from, "291379613@qq.com", copyto, "数据审批提醒", paramMap, username, password);
        } else {
            EMailUtil.sendAndCc(host, from, "291379613@qq.com", copyto, "数据审批提醒", paramMap, username, password);
        }
        renderJson("success");
    }
}
