package com.site.utils;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jfinal.plugin.activerecord.Record;
import com.site.core.model.Dealer;
import com.site.core.model.User;

public class ConvertUtil {
   public static List<Dealer> toDealerList(List<Record> list)
   {
	   
	   List<Dealer> resultList = new ArrayList<Dealer>();
	   
	   if (null != list && !list.isEmpty())
	   {
		   for (Record record : list)
		   {
			   Dealer e = new Dealer();
			   e.setId(record.getLong("id"));
			   e.setName(record.getStr("name"));
			   resultList.add(e);
		   }
	   }
	   
	   return resultList;
   }
   
   public static List<User> toUserList(List<Record> list)
   {
	   
	   List<User> resultList = new ArrayList<User>();
	   
	   if (null != list && !list.isEmpty())
	   {
		   for (Record record : list)
		   {
			   User e = new User();
			   e.setId(record.getLong("id"));
			   e.setName(record.getStr("name"));
			   e.setUsername(record.getStr("username"));
			   e.setPassword(record.getStr("password"));
			   e.setEmail(record.getStr("email"));
			   e.setRoleName(record.getStr("role_name"));
			   e.setEmployeeNumber(record.getStr("employee_number"));
			   resultList.add(e);
		   }
	   }
	   
	   return resultList;
   }
   
}
