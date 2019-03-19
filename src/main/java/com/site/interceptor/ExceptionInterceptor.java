package com.site.interceptor;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Before;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.site.base.ExceptionForJson;
import com.site.base.ExceptionForLogin;
import com.site.base.Result;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

/**
 * 全局异常处理
 * Created by WSQ on 2016/8/19.
 */
public class ExceptionInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        final Invocation invoke = inv;
        final Controller controller = inv.getController();
        Method method = inv.getMethod();
        final HttpServletRequest request = controller.getRequest();
        Map<String, String[]> map = request.getParameterMap();
        final String params = map.keySet().size() > 0 ? JSON.toJSONString(map) : "null";
        final Boolean isAjax = !StrKit.isBlank(controller.getRequest().getHeader("X-Requested-With"));
        final String requestMethod = isAjax ? "post" : "get";
        boolean isTx = false;
        Before before = method.getAnnotation(Before.class);
        if (before != null && before.value().length > 0) {
            for (Class cls : before.value()) {
                if (cls.getName().equals(Tx.class.getName())) {
                    isTx = true;
                    break;
                }
            }
        }

        final StringBuilder sb = new StringBuilder();
        final Logger log = Logger.getLogger(controller.getClass());
        if (isTx) {
            Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    try {
                        invoke.invoke();
                    } catch (ExceptionForJson e) {
                        e.printStackTrace();
                        sb.append(requestMethod + "|" + "jxc" + "|" + request.getRequestURI() + "|" + params + "|");
                        sb.append(e.getMessage() + "-->");
                        for (StackTraceElement elem : e.getStackTrace()) {
                            sb.append(elem + "-->");
                        }
                        sb.deleteCharAt(sb.length() - 3);
                        log.warn(sb.toString());
                        Result result = new Result();
                        result.addError(e.getMessage());
                        controller.renderJson(result);
                        return false;
                    } catch (ExceptionForLogin e) {
                        e.printStackTrace();
                        sb.append(requestMethod + "|" + "manager" + "|" + request.getRequestURI() + "|" + params + "|");
                        sb.append(e.getMessage() + "-->");
                        for (StackTraceElement elem : e.getStackTrace()) {
                            sb.append(elem + "-->");
                        }
                        sb.deleteCharAt(sb.length() - 3);
                        log.warn(sb.toString());
                        controller.render("login.html");
                        return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (isAjax) {
                            sb.append(requestMethod + "|" + "manager" + "|" + request.getRequestURI() + "|" + params + "|");
                            sb.append(e.getMessage() + "-->");
                            for (StackTraceElement elem : e.getStackTrace()) {
                                sb.append(elem + "-->");
                            }
                            sb.deleteCharAt(sb.length() - 3);
                            log.warn(sb.toString());
                            Result result = new Result();
                            result.addError("系统错误，错误原因：" + e.getMessage());
                            controller.renderJson(result);
                            return false;
                        } else {
                            sb.append(requestMethod + "|" + "manager" + "|" + request.getRequestURI() + "|" + params + "|");
                            sb.append(e.getMessage() + "-->");
                            for (StackTraceElement elem : e.getStackTrace()) {
                                sb.append(elem + "-->");
                            }
                            sb.deleteCharAt(sb.length() - 3);
                            log.warn(sb.toString());
                            controller.setAttr("message", e.getMessage());
                            controller.render("/view/500.html");
                            return false;
                        }
                    }
                    return true;
                }
            });
        } else {
            try {
                inv.invoke();
            } catch (ExceptionForJson e) {
                e.printStackTrace();
                sb.append(requestMethod + "|" + "manager" + "|" + request.getRequestURI() + "|" + params + "|");
                sb.append(e.getMessage() + "-->");
                for (StackTraceElement elem : e.getStackTrace()) {
                    sb.append(elem + "-->");
                }
                sb.deleteCharAt(sb.length() - 3);
                log.warn(sb.toString());
                Result result = new Result();
                result.addError(e.getMessage());
                controller.renderJson(result);
                return;
            } catch (ExceptionForLogin e) {
                e.printStackTrace();
                sb.append(requestMethod + "|" + "manager" + "|" + request.getRequestURI() + "|" + params + "|");
                sb.append(e.getMessage() + "-->");
                for (StackTraceElement elem : e.getStackTrace()) {
                    sb.append(elem + "-->");
                }
                sb.deleteCharAt(sb.length() - 3);
                log.warn(sb.toString());
                controller.render("/view/500.html");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                sb.append(requestMethod + "|" + "manager" + "|" + request.getRequestURI() + "|" + params + "|");
                sb.append(e.getMessage() + "-->");
                for (StackTraceElement elem : e.getStackTrace()) {
                    sb.append(elem + "-->");
                }
                sb.deleteCharAt(sb.length() - 3);
                log.warn(sb.toString());
                controller.setAttr("message", e.getMessage());
                controller.render("/view/500.html");
                return;
            }
        }
    }

}
