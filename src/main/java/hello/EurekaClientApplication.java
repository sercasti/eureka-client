package hello;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@EnableCircuitBreaker
@EnableEurekaClient
@SpringBootApplication
@RestController
@RibbonClient(name = "ClientApp", configuration = RibbonConfig.class)
public class EurekaClientApplication extends WebMvcConfigurerAdapter {

	@Autowired
	private DiscoveryClient discoveryClient;

	public static void main(String[] args) {
		SpringApplication.run(EurekaClientApplication.class, args);
	}

	@RequestMapping("/service-instances/{applicationName}")
	public @ResponseBody List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
		return discoveryClient.getInstances(applicationName);
	}

	@RequestMapping("/circuitBreaker")
	@HystrixCommand(fallbackMethod = "reliable")
	public String readingList() {
		RestTemplate restTemplate = new RestTemplate();
		URI uri = URI.create("http://localhost:8090/recommended");
		return restTemplate.getForObject(uri, String.class);
	}

	public String reliable() {
		return "Reliable Endpoint Called by Hystrix";
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.ignoreAcceptHeader(true).defaultContentType(MediaType.APPLICATION_JSON);
	}
}
