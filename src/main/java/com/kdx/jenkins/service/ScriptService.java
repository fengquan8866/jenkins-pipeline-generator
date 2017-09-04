package com.kdx.jenkins.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.kdx.jenkins.freemarker.ParentDirMethod;
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
import lombok.extern.slf4j.Slf4j;

@Service
@EnableConfigurationProperties
@ConfigurationProperties(locations = "classpath:env.yml")
@Data
@Slf4j
public class ScriptService implements InitializingBean {
    
    private static String DEFAULT_ENV = "dev";
    private static String DEFAULT_CONFIG_NAME = "default";

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
	
	/** 前端工程 */
	private List<String> frontProject;

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
		    String ftlName = null;
		    Template temp = null;
			List<Map<String, Object>> l = new ArrayList<>();
			for (String projectName : enableProject) {
			    ftlName = getFtlName(envName, projectName);
			    temp = cfg.getTemplate(ftlName);
				Map<String, Object> projectProps = assambleProps(envName, projectName, ftlName);
				if (!singles.contains(projectName)) {
					l.add(projectProps);
				}
				String proPath = rootPath + "/" + projectProps.get("fullName");
				log.info("工程：{}，   配置参数：{}", projectProps.get("serviceName"), projectProps);
				generateScript(temp, proPath + "/Jenkins/" + envName, assambleMap("item", projectProps, envName, (String) projectProps.get(ftlName)));
			}
			ftlName = "list.ftl";
			temp = cfg.getTemplate(ftlName);
			generateScript(temp, rootPath + "/" + envName, assambleMap("env", l, envName, ftlName));
		}
	}

	/**
	 * 获取模板名称
	 * @Title: getFtlName
	 * @Description: 
	 * @param envName
	 * @param projectName
	 * @return
	 */
	private String getFtlName(String envName, String projectName) {
        String ftlName = null;
        if (frontProject.contains(projectName)) {
            ftlName = "front-" + envName + ".ftl";
            URL url = ClassUtils.getDefaultClassLoader().getResource("templates/" + ftlName);
            if (url == null) {
                ftlName = "front.ftl";
                url = ClassUtils.getDefaultClassLoader().getResource("templates/" + ftlName);
                if (url == null) {
                    log.info("前端工程模板不存在! projectName：{}, envName:{}", projectName, envName);
                    return null;
                }
            }
        } else {
            ftlName = envName + ".ftl";
            URL url = ClassUtils.getDefaultClassLoader().getResource("templates/" + ftlName);
            if (url == null) {
                ftlName = DEFAULT_ENV + ".ftl";
                url = ClassUtils.getDefaultClassLoader().getResource("templates/" + ftlName);
                if (url == null) {
                    log.info("后端工程模板不存在! projectName：{}, envName:{}", projectName, envName);
                    return null;
                }
            }
        }
        return ftlName;
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
	 * @param ftlName
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws MalformedTemplateNameException
	 * @throws TemplateNotFoundException
	 * @throws TemplateException
	 */
	@SuppressWarnings("unchecked")
    private Map<String, Object> assambleProps(String envName, String projectName, String ftlName)
			throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
			TemplateException {
        Map<String, Object> projectProps = null, // 工程配置
                projects = env.get(envName); // 环境下所有工程配置
        // 获取当前环境下的工程配置
        if (projects != null && projects.containsKey(projectName)) {
            projectProps = (Map<String, Object>) projects.get(projectName);
        }
        
        Map<String, Object> defaultProps = env.get(DEFAULT_CONFIG_NAME), // 全局默认配置
				envDefaultProps = env.containsKey(envName) ? (Map<String, Object>) env.get(envName).get(DEFAULT_CONFIG_NAME) : null, // 当前环境默认配置
				props = MapUtil.mergeNew(defaultProps, envDefaultProps); // 合并以上2个默认配置
		// 非开发环境，合并 开发环境配置 -> 当前默认配置
		if (!DEFAULT_ENV.equals(envName)) {
			props = MapUtil.mergeLast((Map<String, Object>) env.get(DEFAULT_ENV).get(DEFAULT_CONFIG_NAME),
					(Map<String, Object>) env.get(DEFAULT_ENV).get(projectName), props);
		}
		// 合并 默认配置 -> 当前配置
		projectProps = MapUtil.mergeLast(props, projectProps);
		projectProps.putIfAbsent("envName", envName);
		projectProps.putIfAbsent("projectName", projectName);
		projectProps.putIfAbsent("ftlName", ftlName);

		// 表达式、变量处理
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
	 * @param ftlName
	 * @return
	 */
    private Map<String, Object> assambleMap(String key, Object val, String envName, String ftlName) {
        Map<String, Object> m = new HashMap<>();
        m.put(key, val);
        m.put("envName", envName);
        m.put("ftlName", ftlName);
        assambleCustomMethod(m);
        log.info("模板渲染参数: {}", m);
        return m;
    }

    /**
     * 添加自定义方法
     * @Title: assambleCustomMethod
     * @Description: 
     * @param m
     */
	private void assambleCustomMethod(Map<String, Object> m) {
        m.put("parent_dir", new ParentDirMethod());
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
