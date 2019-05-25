/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wffly.club;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.springframework.aop.MethodBeforeAdvice;

import com.wffly.club.bean.PluginConsole;


/**
 */
@SuppressWarnings("serial")
public class CountingBeforeAdvice extends MethodCounter implements MethodBeforeAdvice, PluginConsole {

	public void before(Method m, Object[] args, Object target) throws Throwable {
		count(m);
		System.out.println(String.format("方法%s 执行次数%s", m.getName(), getCalls()));
	}

	public String getStatus() {
		String time = SimpleDateFormat.getDateTimeInstance().format(new Date());
		String result = "";
		for (Entry<String, Integer> m : map.entrySet()) {
			result += "方法:" + m.getKey();
			result += "执行次数:" + m.getValue();
			result += "\r\n";
		}
		return String.format("时间:%s,执行信息:%s", time, result);
	}

	public String command(String cmd) {
		// TODO Auto-generated method stub
		return null;
	}

}
