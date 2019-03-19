package com.site.quartz;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.cron4j.ITask;
import com.site.core.model.SendEmailList;
import com.site.core.model.User;
import com.site.utils.EMailUtil;
import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;
import java.util.*;

public class SendEmailTask implements ITask {
    @Override
    public void stop() {

    }

    @Override
    public void run() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        SendEmailList email = SendEmailList.dao.findFirst("select * from send_email_list where send_time=? and is_delete='0' and status='-1'", sdf.format(new Date()));
        sendEmail(email);
    }

    private void sendEmail(SendEmailList sendEmail) {
        if (sendEmail == null) {
            return;
        }

        String host = PropKit.get("email.smtp");
        String from = PropKit.get("email.from");
        String copyto = PropKit.get("email.copyto");
        String username = PropKit.get("email.username");
        String password = PropKit.get("email.password");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("content", sendEmail.getContent());
        List<User> users = User.dao.find("select email from user where is_delete='0' and enable='0'");
        users.forEach(user -> {
            String email = user.getEmail();
            if (StringUtils.isNotBlank(email)) {
                EMailUtil.sendAndCc(host, from, email, copyto, sendEmail.getSubject(), paramMap, username, password);
            }
        });
    }
}
