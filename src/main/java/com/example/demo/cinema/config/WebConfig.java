package com.example.demo.cinema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    
    @Value("${app.upload.dir}")
    private String uploadDir; 

    @Value("${app.upload.people.dir}")
    private String peopleUploadDir;

    @Value("${app.upload.movie-posters.dir}")
    private String moviePostersUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        registry.addResourceHandler("/images/avatars/**")
                .addResourceLocations("file:///" + uploadDir); 

        registry.addResourceHandler("/people-photos/**")
                .addResourceLocations("file:" + peopleUploadDir);

        registry.addResourceHandler("/movie-posters/**")
        .addResourceLocations("file:" + moviePostersUploadDir);

        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/webfonts/**")
                .addResourceLocations(
                    "classpath:/static/css/",
                    "classpath:/static/js/",
                    "classpath:/static/images/",
                    "classpath:/static/webfonts/"
                );
    }
}
