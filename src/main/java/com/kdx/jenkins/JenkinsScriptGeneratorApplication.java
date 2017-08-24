package com.kdx.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.kdx.jenkins.service.ScriptService;

@SpringBootApplication
@ComponentScan("com.kdx.jenkins.service")
public class JenkinsScriptGeneratorApplication {

	@Autowired
	ScriptService scriptService;
	
	public static void main(String[] args) {
		SpringApplication.run(JenkinsScriptGeneratorApplication.class, args);
	}
}
