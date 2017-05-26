package com.kdx.jenkins.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.kdx.jenkins.util.MapUtil;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
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
	private Configuration cfg;

	private Configuration stringCfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

	/** 根路径 */
	private String rootPath;

	/** 环境配置信息 */
	private Map<String, Map<String, Object>> env;

	/** 单独执行的服务 */
	private List<String> singles;

	/** 启用的环境 */
	private List<String> enableEnv;

	/** 启用的工程 */
	private List<String> enableProject;

	/**
	 * 生成脚本
	 * 
	 * @Title: generate
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
			List<Map<String, Object>> l = new ArrayList<>();
			for (String projectName : enableProject) {
				Map<String, Object> projectProps = null, projects = env.get(envName);
				if (projects != null && projects.containsKey(projectName)) {
					projectProps = (Map<String, Object>) projects.get(projectName);
				}
				projectProps = assambleProps(envName, projectName, projectProps);
				if (!singles.contains(projectName)) {
					l.add(projectProps);
				}
				String proPath = rootPath + "/" + projectProps.get("fullName");
				generateScript(temp, proPath + "/Jenkins/" + envName, assambleMap("item", projectProps, envName));
			}
			temp = cfg.getTemplate("list.ftl");
			generateScript(temp, rootPath + "/" + envName, assambleMap("env", l, envName));
		}
	}

	/**
	 * 生成脚本
	 * 
	 * @Title: generate
	 * @param temp
	 *            模板
	 * @param filePath
	 *            生成文件路径
	 * @param params
	 *            参数
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
	 * 
	 * @Title: assambleProps
	 * @param envName
	 * @param projectName
	 * @param projectProps
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws MalformedTemplateNameException
	 * @throws TemplateNotFoundException
	 * @throws TemplateException
	 */
	private Map<String, Object> assambleProps(String envName, String projectName, Map<String, Object> projectProps)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException {
		Map<String, Object> defaultProps = env.get("default"),
				envDefaultProps = (Map<String, Object>) env.get(envName).get("default"),
				props = MapUtil.mergeNew(defaultProps, envDefaultProps);
		if (!"dev".equals(envName)) {
			props = MapUtil.mergeLast((Map<String, Object>) env.get("dev").get("default"),
					(Map<String, Object>) env.get("dev").get(projectName), props);
		}
		projectProps = MapUtil.mergeLast(props, projectProps);
		projectProps.put("envName", envName);
		projectProps.put("projectName", projectName);

		Boolean hasExpression;
		do {
			hasExpression = false;
			Object originalVal;
			for (Map.Entry<String, Object> e : projectProps.entrySet()) {
				originalVal = e.getValue();
				if (originalVal != null && originalVal instanceof String && ((String) originalVal).contains("$")) {
					String key = "env." + envName + "." + projectName + "." + e.getKey();
					TemplateLoader tl = stringCfg.getTemplateLoader();
					if (!(tl instanceof StringTemplateLoader)) {
						tl = new StringTemplateLoader();
						stringCfg.setTemplateLoader(tl);
					}
					((StringTemplateLoader) tl).putTemplate(key, (String) e.getValue());
					Template t = stringCfg.getTemplate(key);
					StringWriter sw = new StringWriter();
					t.process(projectProps, sw);
					e.setValue(sw.toString());
					hasExpression = (hasExpression
							|| (((String) e.getValue()).contains("$") && !originalVal.equals(e.getValue())));
				}
			}
		} while (hasExpression);
		return projectProps;
	}

	/**
	 * 生成模板map
	 * 
	 * @Title: generateMap
	 * @param key
	 * @param val
	 * @param envName
	 * @return
	 */
	public Map<String, Object> assambleMap(String key, Object val, String envName) {
		Map<String, Object> m = new HashMap<>();
		m.put(key, val);
		m.put("envName", envName);
		return m;
	}

	/**
	 * 返回指定file
	 * 
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
