package com.kdx.jenkins.freemarker;

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * 生成上级目录
 * @ClassName:  ParentDirMethod
 * @Description: 目录结尾含'/'
 * @author: huangchao
 * @date:   2017年8月31日 下午2:56:45
 *
 */
public class ParentDirMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Wrong arguments");
        }
        String str = arguments.get(0).toString();
        return str.replaceAll("\\w+/?$", "");
    }

}
