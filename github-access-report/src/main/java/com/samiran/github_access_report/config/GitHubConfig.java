package com.samiran.github_access_report.config;



//Two things Here we only needed :1. A RestTemplate bean — pre-configured with my
//GitHub token attached to every request automatically via an interceptor.
//AND ->2.An ExecutorService bean — a pool of 20 threads for parallel calls running .
//Both are marked @Bean so Spring creates them once at startup and injects them wherever needed.

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//this class has tools that need to be prepared at startup.
@Configuration
public class GitHubConfig {
    @Value("${github.token}")
    private String githubToken;
    @Value("${github.thread-pool.size}")
    private int threadPoolSize;
//    @Bean — tells Spring: "this method creates one of those tools. " +
//        "Create it once, keep it ready, give it to anyone who needs it."
    @Bean
    public RestTemplate gitHubRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setBearerAuth(githubToken);
            request.getHeaders().set("Accept", "application/vnd.github+json");
            request.getHeaders().set("X-GitHub-Api-Version", "2022-11-28");
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService gitHubExecutor() {
        return Executors.newFixedThreadPool(threadPoolSize);
    }


}
