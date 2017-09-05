package com.hc.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.hc.jenkins.service.ScriptService;

@SpringBootApplication
@ComponentScan("com.hc.jenkins.service")
public class JenkinsPipelineGeneratorApplication {

	@Autowired
	ScriptService scriptService;
	
	public static void main(String[] args) {
		SpringApplication.run(JenkinsPipelineGeneratorApplication.class, args);
	}
}
