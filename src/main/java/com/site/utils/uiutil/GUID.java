package com.site.utils.uiutil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GUID {

	public static String getGuid() {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");//小写的mm表示的是分钟  
		String codeId=sdf.format(new Date())+ (int) ((Math.random() * 9 + 1) * 100000); 
		return codeId;
	}
	
}
