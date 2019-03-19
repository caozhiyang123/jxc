package com.site.quartz;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.cron4j.ITask;
import com.site.core.model.User;
import com.site.core.model.common.OpenTimeStep;
import com.site.service.RoleConfigService;
import com.site.utils.EMailUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件提醒定时任务
 */
public class EmailTask implements ITask {

    @Override
    public void stop() {

    }

    @Override
    public void run() {
        //一级经销商开放期
        OpenTimeStep openTimeStepOne = RoleConfigService.me.getCurrentOpenTimeStep(1);
        sendFirstLevelEmail(openTimeStepOne);
        //二级经销商开放期
        OpenTimeStep openTimeStepTwo = RoleConfigService.me.getCurrentOpenTimeStep(2);
        sendSecondaryLevelEmail(openTimeStepTwo);
    }

    /**
     * 发送一级经销商邮件
     */
    private void sendFirstLevelEmail(OpenTimeStep openTimeStepOne) {
        int step = openTimeStepOne.getStep();
        int desc = openTimeStepOne.getDesc();
        int month = openTimeStepOne.getYearAndMonth();
        //第一个开放期最后一天需要给大区经理发送邮件
        if (step == 1 && desc == 1) {
            sendEmail(month, "一");
        }
        //第二个开放期最后一天需要给大区经理发送邮件
        if (step == 3 && desc == 1) {
            sendEmail(month, "一");
        }
    }

    /**
     * 发送二级经销商邮件
     */
    private void sendSecondaryLevelEmail(OpenTimeStep openTimeStepTwo) {
        int step = openTimeStepTwo.getStep();
        int desc = openTimeStepTwo.getDesc();
        int month = openTimeStepTwo.getYearAndMonth();
        //第一个开放期最后一天需要给大区经理发送邮件
        if (step == 1 && desc == 1) {
            sendEmail(month, "二");
        }
    }

    private void sendEmail(int month, String level) {
        String host = PropKit.get("email.smtp");
        String from = PropKit.get("email.from");
        String copyto = PropKit.get("email.copyto");
        String username = PropKit.get("email.username");
        String password = PropKit.get("email.password");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("level", level);
        paramMap.put("month", month + "");
        List<User> users =  User.dao.find("select email from user where role_name='大区经理' and is_delete='0' and enable='0'");
        users.forEach(user -> {
            String email = user.getEmail();
            if (StringUtils.isNotBlank(email)) {
                EMailUtil.sendAndCc(host, from, email, copyto, "数据审批提醒", paramMap, username, password);
            }
        });
    }
}
