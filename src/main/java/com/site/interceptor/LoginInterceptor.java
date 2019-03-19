package com.site.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.site.base.Result;
import com.site.core.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员权限拦截器
 *
 * @author
 */
public class LoginInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {

        Controller controller = inv.getController();
        Boolean isAjax = !StrKit.isBlank(controller.getRequest().getHeader("X-Requested-With"));
        String methodName = inv.getMethodName();

        if ("login".equals(methodName) || "logout".equals(methodName) || "removeAllLoginUser".equals(methodName)) {
            inv.invoke();
        } else {
            User user = controller.getSessionAttr("user");
            if (user == null) {
                if (isAjax) {
                    Result result = new Result();
                    result.setCode(8);
                    controller.renderJson(result);
                    return;
                }
                controller.render("/views/login.html");
                return;
            }

            controller.setAttr("user", controller.getSessionAttr("user"));
            inv.invoke();
        }
    }

}
