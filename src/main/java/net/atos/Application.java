package net.atos;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * A spring-boot application that includes a Camel route builder to setup the Camel routes
 */
@SpringBootApplication
public class Application extends RouteBuilder {

    // must have a main method spring-boot can run
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(
            new CamelHttpTransportServlet(), "/camel/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

    @Override
    public void configure() throws Exception {

        restConfiguration()
        	.component("servlet")
        	.dataFormatProperty("prettyPrint", "true")
        	.contextPath("/camel")
        	.apiContextPath("/api-doc")
            	.apiProperty("api.title", "User API").apiProperty("api.version", "1.0.0")
            	.apiProperty("cors", "true");
        
        rest("/ping").description("User REST service")
        	.consumes(MediaType.APPLICATION_JSON_VALUE)
        	.produces(MediaType.TEXT_PLAIN_VALUE)

	        .get().id("ping").description("heartbeat response")
	        	.to("direct:in");

    	
        from("direct:in").id("pong")
            .setBody()
                .constant("pong")
            .process((exchange) -> {
              CamelContext ctx = exchange.getContext();
              ctx.getEndpoints().forEach(endpoint -> System.out.println("*** : "+endpoint.getEndpointUri()));
              HttpServletRequest req = exchange.getIn().getBody(HttpServletRequest.class);
              if (req == null) System.out.println("*** : HTTP SERVLET is NULL");
              RequestDispatcher dispatcher = req.getServletContext().getNamedDispatcher("default");
              if (dispatcher == null) System.out.println("*** : Dispatcher is NULL");
              exchange.getOut().setBody(dispatcher.getClass().getName());
              System.out.println("*** : "+ dispatcher.getClass().getName());
            })
            .to("mock:end");
    }

}
