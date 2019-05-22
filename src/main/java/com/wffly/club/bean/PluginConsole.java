package com.wffly.club.bean;

/**
 * 控制接口
 * @author wangfei
 *
 */
public interface PluginConsole {
	public String getStatus();

	public String command(String cmd);

}
