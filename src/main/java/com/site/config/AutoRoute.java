package com.site.config;

import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.site.base.BaseController;
import com.site.utils.ClassUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by richard on 2017-04-12.
 */
public class AutoRoute extends Routes {
    public static AutoRoute me = new AutoRoute();
    private String packageName;

    public AutoRoute(String packageName) {
        this.packageName = packageName;
    }

    public AutoRoute() {
    }

    @SuppressWarnings("unchecked")
    public List<Class<? extends Controller>> getControllerClass() throws ClassNotFoundException {

        List<Class<?>> allClass = ClassUtils.scanPackage(packageName);
        List<Class<? extends Controller>> controllerClasses = new ArrayList<Class<? extends Controller>>();
        for (Class<?> controllerClass : allClass) {
            controllerClasses.add((Class<? extends Controller>) controllerClass);
        }
        return controllerClasses;
    }

    @Override
    public void config() {
        try {
            List<Class<? extends Controller>> controllerClassList = getControllerClass();
            for (Class<? extends Controller> controllerClass : controllerClassList) {
                String controller_old_name = controllerClass.getSimpleName().replace("Controller", "");
                String controllerKey = controller_old_name.substring(0, 1).toLowerCase() + controller_old_name.substring(1);
                if (controllerKey.equals("index")) {
                    controllerKey = "";
                }
                add("/" + controllerKey, controllerClass, "views/" + controllerKey);
            }
            System.out.println("路由注册完成");
        } catch (ClassNotFoundException e) {
            System.out.println("auto route exception ");
        }
    }

    public void addMapping(String name, String className) throws ClassNotFoundException {
        this.add("/" + name, (Class<? extends Controller>) Class.forName(className));
    }

}
