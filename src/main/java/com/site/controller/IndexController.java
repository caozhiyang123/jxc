package com.site.controller;

import com.jfinal.kit.StrKit;
import com.site.base.BaseController;
import com.site.core.model.Notice;
import com.site.core.model.User;
import com.site.utils.LoginUtils;
import com.site.utils.Md5Utils;

/**
 * IndexController
 */
public class IndexController extends BaseController {

    public void index() {
        Notice notice = Notice.dao.findFirst("select * from notice order by id desc limit 0,1");
        setAttr("gg", notice == null ? "暂无任何公告" : notice.getContent());
        render("/views/index/admin.html");
        return;
    }

    public void login() {
        String username = getPara("username");
        String password = getPara("password");

        if (StrKit.isBlank(username)) {
            setAttr("message", "用户名不能为空");
            render("/views/login.html");
            return;
        }

        if (StrKit.isBlank(password)) {
            setAttr("message", "密码不能为空");
            render("/views/login.html");
            return;
        }

        String md5PassWord = Md5Utils.toHex(password);
        User loginUser = User.dao.findFirst("select * from user where username=? and password =? and is_delete='0' and enable='0'", username, md5PassWord);
        if (loginUser == null) {
            setAttr("message", "用户名或者密码不正确");
            render("/views/login.html");
            return;
        }

//        boolean contains = LoginUtils.LOGIN_USERS.contains(loginUser.getId());
//        if (contains) {
//            setAttr("message", "此账号已经有人登录，请稍后重试");
//            render("/views/login.html");
//            return;
//        }
//        LoginUtils.LOGIN_USERS.add(loginUser.getId());

        //初始化服务器URL
        if (SERVER_PATH == null) {
            initServerPath();
        }

        setSessionAttr("user", loginUser);
        redirect("/");
        return;
    }

    public void logout() {
//        LoginUtils.LOGIN_USERS.remove(getLoginUser().getId());
        removeSessionAttr("user");
        redirect("/");
    }

    public void getPageHtml() {
        String folderPath = getPara("folderPath");
        render(folderPath + ".html");
        return;
    }

    private String genRadom() {
        String random = "";
        for (int i = 0; i < 10; i++) {
            int s = (int) (Math.random() * 10);
            random = random + s;
        }
        return random;
    }

    public void removeAllLoginUser() {
//        LoginUtils.LOGIN_USERS.clear();
        renderJson(result);
    }

}





