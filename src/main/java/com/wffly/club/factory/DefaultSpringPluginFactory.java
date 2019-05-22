package com.wffly.club.factory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.fastjson.JSON;
import com.wffly.club.bean.PluginBean;
import com.wffly.club.bean.PluginConsole;
import com.wffly.club.bean.PluginStore;

public class DefaultSpringPluginFactory implements ApplicationContextAware,InitializingBean,SpringPluginFactory{

	private ApplicationContext applicationContext; // 应用上下文
	private Map<String, PluginBean> pluginBeans = new HashMap<>();//总的插件库
	private Map<String, Advice> adviceCache = new HashMap<>();//advice缓存
	private static final String BASE_DIR;//本地插件持久化保存地址
	static {
		BASE_DIR = System.getProperty("user.home") + "/.plugins/";
		System.out.println(BASE_DIR);
	}
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {//得到应用上下文
		this.applicationContext = context;
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {//bean初始化的时候会调用该方法
		loaderLocals();//初始化bean的时候，装载本地已安装插件
		
	}
	/**
	 * 装载本地已安装插件
	 */
	private  void loaderLocals() {
		try {
			Map<String, PluginBean> localPluginBeans = readerLocalPluginBeans();
			if(localPluginBeans == null) {
				return;
			}
			//加载本地插件，放入插件map中
			pluginBeans.putAll(localPluginBeans);
			for(PluginBean pluginBean : localPluginBeans.values()) {
				if(pluginBean.getActive()) {//如果插件状态是激活
					//激活插件
					activePlugin(pluginBean.getId());
				}
			}
			
		} catch (Exception e) {
			System.out.println("装载本地本地已安装插件异常。。。");
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取本地持久化了的插件
	 * @return
	 * @throws Exception
	 */
	public Map<String, PluginBean> readerLocalPluginBeans() throws Exception {
		String baseDir = BASE_DIR;
		File configFile = new File(baseDir + "PluginConfigs.json");
		if (!configFile.exists()) {
			return null;
		}
		InputStream in = new FileInputStream(configFile);
		Map<String, PluginBean> result = new HashMap<>();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copy(in,out);
			PluginStore store = JSON.parseObject(out.toString("UTF-8"),PluginStore.class);
			for(PluginBean pluginBean : store.getPlugins()) {
				result.put(pluginBean.getId(), pluginBean);
			}
		}finally {
			in.close();
		}
		return result;
	}
	
	/**
	 * 复制
	 * @param source
	 * @param sink
	 * @return
	 * @throws IOException
	 */
	private static long copy(InputStream source, OutputStream sink) throws IOException {
		long nread = 0L;
		byte[]buf = new byte[8192];
		int n;
		while((n = source.read(buf))>0) {
			sink.write(buf,0,n);
			nread += n;
		}
		return nread;
	}

	/**
	 * 激活插件
	 */
	@Override
	public void activePlugin(String pluginId) {
		if(!pluginBeans.containsKey(pluginId)) {
			throw new RuntimeException("指定插件不存在,pluginId="+pluginId);
		}
		PluginBean plugin = pluginBeans.get(pluginId);
		for(String name : applicationContext.getBeanDefinitionNames()) {//遍历上下文中 所有bean
			Object bean = applicationContext.getBean(name);
			if(bean == this) {
				continue;
			}else if(!(bean instanceof Advised)) {//Advised:包含所有的Advisor和 Advice Advice:通知拦截器        Advisor:通知 +切入点的适配器
				continue;
			}else if(findAdvice(plugin.getClassName(), (Advised) bean) != null) {//判断对象是否已有该advice
				continue;
			}
			Advice advice = null;
			try {
				//加载插件
				advice = buildAdvice(plugin);
				//将通知拦截器加到Advised中
				((Advised)bean).addAdvice(advice);
			} catch (Exception e) {
				throw new RuntimeException("激活插件失败",e);
			}
		}
		try {
			plugin.setActive(true);
			//持久化
			storePlugins();
		} catch (Exception e) {
			throw new RuntimeException("激活失败",e);
		}
		
	}
	
	/**
	 * 持久化插件到本地，便于重启后重新加载插件
	 * @throws IOException
	 */
	private  void storePlugins()throws IOException{
		String baseDir = BASE_DIR;
		File pluginFile = new File(baseDir + "PluginConfigs.json");
		if (!pluginFile.exists()) {
			pluginFile.getParentFile().mkdirs();
			pluginFile.createNewFile();
		}
		PluginStore store = new PluginStore();
		store.setPlugins(new ArrayList<>(pluginBeans.values()));
		store.setLastModify(new Date());
		OutputStream out = new FileOutputStream(pluginFile);
		String jsonString = JSON.toJSONString(store,true);
		out.write(jsonString.getBytes("UTF-8"));
		out.flush();
		out.close();//最好在finally里关闭
	}
	
	/**
	 * 得到插件jar的本地路径
	 * @param pluginBean
	 * @return
	 */
	private String getLocalJarFile(PluginBean pluginBean) {
		System.out.println("oldJarUrl="+pluginBean.getJarRemoteUrl());
		String jarName = pluginBean.getJarRemoteUrl().substring(pluginBean.getJarRemoteUrl().lastIndexOf("/"));
		System.out.println("newJarUrl="+BASE_DIR+jarName);
		return BASE_DIR+jarName;
	}
	
	/**
	 * 加载插件的jar文件 ，调用URLClassLoader加载，实例化插件转化为Advice
	 * @param pluginBean
	 * @return
	 * @throws Exception
	 */
	private Advice buildAdvice(PluginBean pluginBean) throws Exception{
		if(adviceCache.containsKey(pluginBean.getClassName())) {//如果缓存中有就直接返回
			return adviceCache.get(pluginBean.getClassName());
		}
		File jarFile = new File(getLocalJarFile(pluginBean));//得到插件对应的jar文件,默认本地已经有jar文件了
		if(!jarFile.exists()) {//如果没有，就需要从远程下载plugin文件至本地
			URL url = new URL(pluginBean.getJarRemoteUrl());
			InputStream stream = url.openStream();
			jarFile.getParentFile().mkdirs();//创建上级目录
			try {
				Files.copy(stream, jarFile.toPath());//拷贝到本地
			}catch (Exception e) {
				jarFile.deleteOnExit();
				throw new RuntimeException("从远程下载文件到本地失败",e);
			}
		}
		//classLoader加载jar
		URLClassLoader loader = (URLClassLoader) getClass().getClassLoader();
		URL targetUrl = jarFile.toURI().toURL();
		boolean isLoader = false;
		//判断是否已经被加载了
		for(URL url : loader.getURLs()) {
			if(url.equals(targetUrl)) {
				isLoader = true;
				break;
			}
		}
		//还没有加载
		if(!isLoader) {
			//反射调用URLClassLoader的addURL方法加载jarFile
			Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
			addURLMethod.setAccessible(true);
			addURLMethod.invoke(loader, targetUrl);
		}
		//初始化Plugin Advice
		Class<?> adviceClass = loader.loadClass(pluginBean.getClassName());
		if(!Advice.class.isAssignableFrom(adviceClass)) {
			throw new RuntimeException(
					String.format("plugin 配置错误 %s非 %s的实现类 ", pluginBean.getClassName(), Advice.class.getName()));
		}
		adviceCache.put(adviceClass.getName(), (Advice)adviceClass.newInstance());
		return adviceCache.get(adviceClass.getName());
	}
	
	/**
	 * 查询对象是否已经有该advice
	 * @param className advice的className
	 * @param advised 对象
	 * @return
	 */
	private Advice findAdvice(String className, Advised advised) {
		for(Advisor advisor : advised.getAdvisors()) {
			if(advisor.getAdvice().getClass().getName().equals(className)) {
				return advisor.getAdvice();
			}
		}
		return null;
	}

	/**
	 * 1.判断是否有该插件
	 * 2、判断指定的bean 是否已切入了指定的通知
	 * 3、调用remove 方法进行移除 
	 * 4、保存配置至本地
	 */
	@Override
	public void disablePlugin(String pluginId) {
		if (!pluginBeans.containsKey(pluginId)) {
			throw new RuntimeException(String.format("指定插件不存在 id=%s", pluginId));
		}
		PluginBean pluginBean = pluginBeans.get(pluginId);
		for(String name : applicationContext.getBeanDefinitionNames()) {
			Object bean = applicationContext.getBean(name);
			if(bean instanceof Advised) {
				Advice advice = findAdvice(pluginBean.getClassName(), (Advised)bean);
				if(advice != null) {
					((Advised)bean).removeAdvice(advice);
				}
			}
			pluginBean.setActive(false);
			try {
				storePlugins();
			} catch (IOException e) {
				// TODO 保存失败需要回滚
				throw new RuntimeException("禁用失败", e);
			}
		}
		
	}

	/**
	 * 1、验证插件是否已安装
	 * 2、去缓存获取 插件对应的缓存的实例 
	 * 3、查地文件缓存查找对应的jar 
	 * 4、从远程下载jar 
	 * 5、动态的将jar 装载到classLoader 
	 * 6、 实例 化这个插件对象 (AOP 里面的一个通知对象)
	 * 7、动态将的将这个对像织入到spring aop
	 */
	@Override
	public void installPlugin(PluginBean plugin, Boolean active) {
		if (pluginBeans.containsKey(plugin.getId())) {
			throw new RuntimeException(String.format("已存在指定的插件 id=%s", plugin.getId()));
		}
		
		// 填充至插件库
		pluginBeans.put(plugin.getId(), plugin);
		
		// 下载远程插件
		try {
			buildAdvice(plugin);
		} catch (Exception e1) {
			pluginBeans.remove(plugin.getId());
			throw new RuntimeException(String.format("插件构建失败 id=%s", plugin.getId()), e1);
		}
		
		// 持久化至本地库
		try {
			storePlugins();
		} catch (IOException e) {
			pluginBeans.remove(plugin.getId());
			throw new RuntimeException(String.format("插件安装失败 id=%s", plugin.getId()), e);
		}
		
		if(active != null && active) {
			//安装当前插件
			activePlugin(plugin.getId());
		}
	}

	/**
	 * 卸载插件
	 */
	@Override
	public void uninstallPlugin(String id) {
		if (!pluginBeans.containsKey(id)) {
			throw new RuntimeException(String.format("不存在指定的插件 id=%s", id));
		}
		disablePlugin(id); // 禁用指定插件
		pluginBeans.remove(id);//从缓存中删除
		try {
			storePlugins();//更新本地持久化
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<PluginBean> getPluginList() {
		try {
			return new ArrayList<>(pluginBeans.values());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("插件获取失败", e);
		}
	}

	@Override
	public String getPluginStatus(String id) {
		if(!pluginBeans.containsKey(id)) {
			throw new RuntimeException(String.format("指定插件不存在 id=%s", id));
		}
		if(!adviceCache.containsKey(id)) {
			return "inactive";
		}
		Advice advice = adviceCache.get(id);
		if(advice instanceof PluginConsole) {
			return ((PluginConsole)advice).getStatus();
		}else {
			return "nonsupport";
		}
	}

}
