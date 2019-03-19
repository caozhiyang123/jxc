package com.site.base;

import com.jfinal.core.Controller;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseService {
    public Controller controller;
    private static Map<Class<? extends BaseService>, BaseService> INSTANCE_MAP = new HashMap<Class<? extends BaseService>, BaseService>();

    public static <Ser extends BaseService> Ser getInstance(Class<Ser> clazz, Controller controller) {
        Ser service = (Ser) INSTANCE_MAP.get(clazz);
        if (service == null) {
            try {
                service = clazz.newInstance();
                INSTANCE_MAP.put(clazz, service);
            } catch (InstantiationException e) {
                System.out.println(e.getMessage());
            } catch (IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
        }
        service.controller = controller;
        return service;
    }

}
