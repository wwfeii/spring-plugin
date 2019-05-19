package com.wffly.club.bean;

import java.io.Serializable;

/**
 * 
 * @author wangfei
 *	插件实体类
 */
public class PluginBean implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;//插件名称
	private String className;//对应jar包里面的类全限定名
	private String jarRemoteUrl; // jar包地址
	private Boolean active;//状态
	private String version;//版本
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getJarRemoteUrl() {
		return jarRemoteUrl;
	}
	public void setJarRemoteUrl(String jarRemoteUrl) {
		this.jarRemoteUrl = jarRemoteUrl;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "PluginBean [id=" + id + ", name=" + name + ", className=" + className + ", jarRemoteUrl=" + jarRemoteUrl
				+ ", active=" + active + ", version=" + version + "]";
	}
	

}
