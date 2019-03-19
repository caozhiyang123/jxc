//package com.site.base;
//
//import com.jfinal.kit.StrKit;
//import com.jfinal.plugin.IPlugin;
//import com.site.core.model.store.Code;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class C implements IPlugin {
//    public static final Map<String, String> code = new CMap<>();
//    public static final Map<String, String> config = new CMap<>();
//    private static Map<String, String> getCodeName = new HashMap<>();
//
//    @Override
//    public boolean start() {
//        loadCode();
//        loadConfig();
//        System.out.println("=========C map start==========");
//        return true;
//    }
//
//    @Override
//    public boolean stop() {
//        code.clear();
//        config.clear();
//        System.out.println("=========C map stop==========");
//        return true;
//    }
//
//    private static boolean loadCode() {
//        List<Code> list_code = Code.dao.find("select type,code,name from pop_code");
//        for (int i = 0; i < list_code.size(); i++) {
//            Code m = list_code.get(i);
//            code.put(m.getType() + "_" + m.getName(), m.getCode());
//            getCodeName.put(m.getType() + "_" + m.getCode(), m.getName());
//        }
//        return true;
//    }
//
//    private static boolean loadConfig() {
////		List<Config> list_config = Config.dao.find("select * from config");
////		for (int i=0;i<list_config.size();i++){
////			Config m = list_config.get(i);
////			config.put(m.getConfigType()+"_"+m.getConfigName(), m.getConfigValue());
////		}
//        return true;
//    }
//
//    public static void reload() {
//        code.clear();
//        config.clear();
//        loadCode();
//        loadConfig();
//        System.out.println("=========C map reload==========");
//    }
//
//    public static String getCodeName(String type, String code) {
//        if (StrKit.isBlank(code)) return null;
//        return getCodeName.get(type.concat("_").concat(code));
//    }
//
//    public static class CMap<k, v> extends HashMap<k, v> {
//        private static final long serialVersionUID = 4565147949715265527L;
//
//        public v get(Object key) {
//            if (!super.containsKey(key)) {
//                throw new RuntimeException("C配置不正确:" + key.toString());
//            }
//            return super.get(key);
//        }
//    }
//
//}
