package com.kdx.jenkins.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import lombok.Data;

@Service
@EnableConfigurationProperties
@ConfigurationProperties(locations = "classpath:env.yml")
@Data
public class ScriptService implements InitializingBean {

	@Autowired
	Configuration cfg;
	
	/** 根路径 */
	private String rootPath;

	/** 环境配置信息 */
	private Map<String, Map<String, Map<String, String>>> env;
	
	/** 单独执行的服务 */
	private List<String> singles;

	/** 启用的环境 */
	private List<String> enableEnv;
	
	/** 启用的工程 */
	private List<String> enableProject;
	
	/** 默认配置 */
	private Map<String, String> defaultCfg;
	
	/**
	 * 生成脚本
	 * @Title: generate
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @throws TemplateNotFoundException
	 * @throws MalformedTemplateNameException
	 * @throws ParseException
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void generate() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException,
			IOException, TemplateException {
		for (String envName : enableEnv) {
			Template temp = cfg.getTemplate(envName + ".ftl");
			List<Map<String, String>> l = new ArrayList<>();
			for (String projectName : enableProject) {
				Map<String, String> props = null;
				Map<String, Map<String, String>> projects = env.get(envName);
				if (projects.containsKey(projectName)) {
					props = projects.get(projectName);
				}
				if (!"dev".equals(envName)) {
					props = assambleProps(envName, projectName, props);
				}
				if (!singles.contains(projectName)) {
					l.add(props);
				}
				String proPath = rootPath + "/" + props.get("fullName");
				generateScript(temp, proPath + "/Jenkins/" + envName, assambleMap("item", props, envName));
			}
			temp = cfg.getTemplate("list.ftl");
			generateScript(temp, rootPath + "/" + envName, assambleMap("env", l, envName));
		}
	}

	/**
	 * 生成脚本
	 * @Title: generate
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param temp		模板
	 * @param filePath	生成文件路径
	 * @param params	参数
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void generateScript(Template temp, String filePath, Map<String, Object> params)
			throws IOException, TemplateException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getFile(filePath))));
		try {
			temp.process(params, pw);
			pw.flush();
		} finally {
			pw.close();
		}
	}

	/**
	 * 组装模板参数
	 * @Title: assambleProps
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param envName
	 * @param projectName
	 * @param props
	 * @return
	 */
	private Map<String, String> assambleProps(String envName, String projectName, Map<String, String> props) {
		Map<String, String> params = env.get("dev").get(projectName);
		if (props != null) {
			for (Map.Entry<String, String> e : props.entrySet()) {
				params.put(e.getKey(), e.getValue());
			}
		}
		return params;
	}

	/**
	 * 生成模板map
	 * @Title: generateMap
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param key
	 * @param val
	 * @param envName 
	 * @return
	 */
	public Map<String, Object> assambleMap(String key, Object val, String envName) {
		Map<String, Object> m = new HashMap<>();
		m.put(key, val);
		m.put("envName", envName);
		for (Entry<String, String> e : defaultCfg.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}
		return m;
	}

	/**
	 * 返回指定file
	 * @Title: getFile
	 * @Description: 会生成父路径
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static File getFile(String path) throws IOException {
		File f = new File(path);
		File p = f.getParentFile();
		if (!p.exists()) {
			p.mkdirs();
		}
		return f;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		generate();
	}

}
