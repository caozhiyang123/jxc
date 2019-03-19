package com.site.config;

import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.site.core.model._MappingKit;
import com.site.interceptor.ExceptionInterceptor;
import com.site.interceptor.LoginInterceptor;
import com.site.quartz.Cron4jPluginConfig;

/**
 * API引导式配置
 */
public class Config extends JFinalConfig {

    /**
     * 配置常量
     */
    public void configConstant(Constants me) {
        // 加载少量必要配置，随后可用PropKit.get(...)获取值
        PropKit.use("properties/application.properties");
        me.setDevMode(true);
//        me.setDevMode(PropKit.getBoolean("devMode", false));
//        me.setBaseUploadPath(PropKit.get("file.dir"));
        me.setError500View("/views/500.html");
    }

    /**
     * 配置路由
     */
    public void configRoute(Routes me) {
        me.add(new AutoRoute("com.site.controller"));
    }

    @Override
    public void configEngine(Engine engine) {
        engine.addSharedFunction("/common/_admin_layout.html");
        engine.addSharedFunction("/common/_layout.html");
    }

    public static DruidPlugin createC3p0Plugin() {
        return new DruidPlugin(PropKit.get("jdbc.url"), PropKit.get("jdbc.user"), PropKit.get("jdbc.password").trim());
    }

    /**
     * 配置插件
     */
    public void configPlugin(Plugins me) {
        // 配置C3p0数据库连接池插件
        DruidPlugin core = createC3p0Plugin();
        core.setTestWhileIdle(true);

        me.add(core);

        // 配置ActiveRecord插件
        ActiveRecordPlugin arp_core = new ActiveRecordPlugin("core", core);
        arp_core.setShowSql(true);

        me.add(arp_core);

        // 所有配置在 MappingKit 中搞定
        _MappingKit.mapping(arp_core);


        //第二个锁定期开始计算
        me.add(Cron4jPluginConfig.getCron4jPlugin());
    }

    /**
     * 配置全局拦截器
     */
    public void configInterceptor(Interceptors me) {
        me.add(new ExceptionInterceptor());
        me.add(new LoginInterceptor());
    }

    /**
     * 配置处理器
     */
    public void configHandler(Handlers me) {
        me.add(new ContextPathHandler("contextPath"));
    }

    /**
     * 建议使用 JFinal 手册推荐的方式启动项目
     * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
     */
    public static void main(String[] args) {
        /**
         * 特别注意：IDEA 之下建议的启动方式，仅比 eclipse 之下少了最后一个参数
         */
        JFinal.start("src/main/webapp", 80, "/");
    }
}
