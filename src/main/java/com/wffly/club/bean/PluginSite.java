package com.wffly.club.bean;

import java.io.Serializable;
import java.util.List;
/**
 * 
 * @author wangfei
 *	jar站点实体类
 */
public class PluginSite implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;//站点名称
	private List<PluginBean> pluginBeans;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PluginBean> getPluginBeans() {
		return pluginBeans;
	}
	public void setPluginBeans(List<PluginBean> pluginBeans) {
		this.pluginBeans = pluginBeans;
	}
	
}
