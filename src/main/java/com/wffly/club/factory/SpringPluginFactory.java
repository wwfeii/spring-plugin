package com.wffly.club.factory;

import java.util.List;

import com.wffly.club.bean.PluginBean;

/**
 * 插件工厂接口
 * @author wangfei
 *
 */
public interface SpringPluginFactory {
	
	/**
	 * 装载指定插件
	 * 
	 * @param pluginId
	 */
	public void activePlugin(String pluginId);

	/**
	 * 禁用指定插件
	 * 
	 * @param pluginId
	 */
	public void disablePlugin(String pluginId);
	
	/**
	 * 安装插件
	 * @param plugin
	 */
	public void installPlugin(PluginBean plugin,Boolean load);
	
	/**
	 * 卸载插件
	 * @param plugin
	 */
	public void uninstallPlugin(String id);
	
	/**
	 * 得到插件列表
	 * @return
	 */
	public List<PluginBean> getPluginList();
	
	/**
	 * 得到插件状态
	 * @param id
	 * @return
	 */
	public String getPluginStatus(String id);

}
