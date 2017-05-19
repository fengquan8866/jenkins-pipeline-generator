package com.kdx.jenkins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.kdx.jenkins.service.HelloService;

@SpringBootApplication
public class KdxJenkinsPipelineGeneratorApplication {

	@Autowired
	HelloService helloService;
	
	public static void main(String[] args) {
		SpringApplication.run(KdxJenkinsPipelineGeneratorApplication.class, args);
	}
}
