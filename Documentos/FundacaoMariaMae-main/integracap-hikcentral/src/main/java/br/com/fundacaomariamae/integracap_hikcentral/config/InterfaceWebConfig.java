package br.com.fundacaomariamae.integracap_hikcentral.config;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serve as telas existentes pela mesma origem da API, sem mover a pasta FrontEnd. */
@Configuration
public class InterfaceWebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pasta = Path.of("FrontEnd").toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/interface/**").addResourceLocations(pasta.endsWith("/") ? pasta : pasta + "/");
    }
}
