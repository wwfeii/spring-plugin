package com.wffly.club;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.aop.MethodBeforeAdvice;

/**
 * 输出Bean方法执行日志
 * MethodBeforeAdvice：方法调用前执行
 *  实现接口   AfterReturningAdvice该拦截器会在调用方法后执行
            实现接口  MethodInterceptor该拦截器会在调用方法前后都执行，实现环绕结果。
 * 
 *
 */
public class ServerLogPlugin implements MethodBeforeAdvice {

	public void before(Method method, Object[] args, Object target) throws Throwable {
		String result = String.format("%s.%s() 参数:%s", method.getDeclaringClass().getName(), method.getName(),
				Arrays.toString(args));
		System.out.println(result);
	}

	
}
