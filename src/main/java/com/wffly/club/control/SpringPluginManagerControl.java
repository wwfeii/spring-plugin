package com.wffly.club.control;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
/**
 * 
 * @author wangfei
 *	插件管理控制类
 */
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.fastjson.JSON;
import com.wffly.club.bean.PluginBean;
import com.wffly.club.bean.PluginSite;
import com.wffly.club.factory.SpringPluginFactory;
@WebServlet("/plugin")
public class SpringPluginManagerControl extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8114327316757032792L;
	private WebApplicationContext context;//应用上下文
	private SpringPluginFactory pluginFactory;//插件工厂
	
	@Override
	public void init() throws ServletException {
		super.init();
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		pluginFactory = context.getBean(SpringPluginFactory.class);
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if("list".equals(action)) {//插件列表
			getHavePlugins(req,resp);
		}else if("install".equals(action)) {//安装插件
			installPlugin(req,resp);
		}else if("active".equals(action)) {//启用插件
			activePlugin(req,resp);
		}else if("uninstall".equals(action)) {//卸载插件
			unInstallPlugin(req,resp);
		}else if("disable".equals(action)) {//禁用插件
			disablePlugin(req,resp);
		}else if("site".equals(action)) {//插件站点
			openPluginSite(req,resp);
		}else if("console".equals(action)) {
			openConsole(req,resp);
		}
			
	}
	
	private void openConsole(HttpServletRequest req, HttpServletResponse resp) {
		String statusText = pluginFactory.getPluginStatus(req.getParameter("id"));
		try {
			req.setAttribute("name", "");
			req.setAttribute("statusText", statusText);
			req.getRequestDispatcher("/pluginConsole.jsp").forward(req, resp);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 打开插件站点
	 * @param req
	 * @param resp
	 */
	private void openPluginSite(HttpServletRequest req, HttpServletResponse resp) {
		try {
			String url = req.getParameter("url");
			PluginSite site;
			if(!StringUtils.isEmpty(url)) {
				site = getSite(url);
			}else {
				site = new PluginSite();
				site.setPluginBeans(new ArrayList<PluginBean>());
				site.setName("空站点");
				url = "empty";
			}
			req.setAttribute("site", site);
			req.setAttribute("siteUrl",url);
			req.getRequestDispatcher("/pluginSite.jsp").forward(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 禁用指定插件
	 * @param req
	 * @param resp
	 */
	private void disablePlugin(HttpServletRequest req, HttpServletResponse resp) {
		try {
		String pluginId = req.getParameter("id");
		if(StringUtils.isEmpty(pluginId)) {
			resp.getWriter().append("参数不能为空");
		}
		pluginFactory.disablePlugin(pluginId);
		
			resp.getWriter().append("disable succeed!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 卸载插件
	 * @param req
	 * @param resp
	 */
	private void unInstallPlugin(HttpServletRequest req, HttpServletResponse resp) {
		pluginFactory.uninstallPlugin(req.getParameter("id"));
		try {
			resp.getWriter().append("uninstall succeed!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 启用指定插件
	 * @param req
	 * @param resp
	 */
	private void activePlugin(HttpServletRequest req, HttpServletResponse resp) {
		pluginFactory.activePlugin(req.getParameter("id"));
		try {
			resp.getWriter().append("active success!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 安装指定插件
	 * 
	 * @param req
	 * @param resp
	 */
	private void installPlugin(HttpServletRequest req, HttpServletResponse resp) {
		try {
		String url = req.getParameter("url");
		String id = req.getParameter("id");
		if(StringUtils.isEmpty(url) || StringUtils.isEmpty(id)) {
			resp.getWriter().write("参数不能为空");
		}
			PluginSite site = getSite(url);
			boolean flag = false;
			for(PluginBean pluginBean : site.getPluginBeans()) {
				if(id.equals(pluginBean.getId())) {
					pluginFactory.installPlugin(pluginBean, true);
					flag = true;
					break;
				}
			}
			if (flag) {
				resp.getWriter().append("install succeed!!");
			} else {
				resp.getWriter().append("install fail!!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * 根据url得到站点信息
	 * @param siteUrl
	 * @return
	 * @throws Exception
	 */
	private PluginSite getSite(String siteUrl) throws Exception{
		URL url = new URL(siteUrl);
		InputStream in = url.openStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(in, output);
		String jsonValue = output.toString("UTF-8");
		return JSON.parseObject(jsonValue,PluginSite.class);
	}
	/**
	 * 获取已安装插件列表
	 * @param req
	 * @param resp
	 */
	private void getHavePlugins(HttpServletRequest req, HttpServletResponse resp) {
		req.setAttribute("havePlugins", pluginFactory.getPluginList());
		try {
			req.getRequestDispatcher("/pluginManager.jsp").forward(req, resp);
		} catch (ServletException e) {
			e.printStackTrace();
			// 异常堆栈打印到WEB页
			try {
				e.printStackTrace(new PrintStream(resp.getOutputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private static long copy(InputStream source, OutputStream sink) throws IOException {
		long nread = 0L;
		byte[] buf = new byte[8192];
		int n;
		while ((n = source.read(buf)) > 0) {
			sink.write(buf, 0, n);
			nread += n;
		}
		return nread;
	}

}
