package ex.org.project.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = {
	"ex.org.project.userservice.repository",  // User service repositories
	"ex.org.project.datahub.auth.repository"  // Keycloak library repositories
})
@EntityScan(basePackages = {
	"ex.org.project.userservice.entity",      // User service entities
	"ex.org.project.datahub.auth.model"       // Keycloak library entities
})
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}
