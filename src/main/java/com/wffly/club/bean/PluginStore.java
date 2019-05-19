package com.wffly.club.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
/**
 * 本地插件持久化实体类
 * @author wangfei
 *
 */
public class PluginStore implements Serializable{


	private static final long serialVersionUID = 1L;
	
	private Date lastModify;
	//插件库
	private List<PluginBean> plugins;
	public Date getLastModify() {
		return lastModify;
	}
	public void setLastModify(Date lastModify) {
		this.lastModify = lastModify;
	}
	public List<PluginBean> getPlugins() {
		return plugins;
	}
	public void setPlugins(List<PluginBean> plugins) {
		this.plugins = plugins;
	}
	

}
