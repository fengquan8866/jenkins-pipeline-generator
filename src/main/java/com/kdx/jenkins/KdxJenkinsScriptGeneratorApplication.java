package com.kdx.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.kdx.jenkins.service.HelloService;

@SpringBootApplication
@ComponentScan("com.kdx.jenkins.service")
public class KdxJenkinsScriptGeneratorApplication {

	@Autowired
	HelloService helloService;
	
	public static void main(String[] args) {
		SpringApplication.run(KdxJenkinsScriptGeneratorApplication.class, args);
	}
}
