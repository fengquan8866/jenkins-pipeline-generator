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
		for (Entry<String, Map<String, Map<String, String>>> m : env.entrySet()) {
			Template temp = cfg.getTemplate(m.getKey() + ".ftl");
			List<Map<String, String>> l = new ArrayList<>();
			for (Entry<String, Map<String, String>> p : m.getValue().entrySet()) {
				if (!singles.contains(p.getKey())) {
					l.add(p.getValue());
				}
				String proPath = rootPath + "/" + p.getValue().get("fullName");
				generate(temp, proPath + "/Jenkins/" + m.getKey(), generateMap("item", p.getValue(), m.getKey()));
			}
			temp = cfg.getTemplate("list.ftl");
			generate(temp, rootPath + "/" + m.getKey(), generateMap("env", l, m.getKey()));
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
	private void generate(Template temp, String filePath, Map<String, Object> params)
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
	 * 生成map
	 * @Title: generateMap
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param key
	 * @param val
	 * @param envName 
	 * @return
	 */
	public Map<String, Object> generateMap(String key, Object val, String envName) {
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
