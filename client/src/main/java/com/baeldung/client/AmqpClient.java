package com.baeldung.client;

import com.baeldung.api.BookingException;
import com.baeldung.api.CabBookingService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.remoting.client.AmqpProxyFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static java.lang.System.out;

@SpringBootApplication
public class AmqpClient {

    @Bean
    Queue queue() {
        return new Queue("remotingQueue");
    }

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange("remoting.exchange");
    }

    @Bean
    public Binding b2() {
        return BindingBuilder.bind(queue()).to(directExchange()).with("remoting.binding");
    }

    @Bean
    RabbitTemplate amqpTemplate(ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setRoutingKey("remoting.binding");
        template.setExchange("remoting.exchange");
        return template;
    }

    /*@Bean
    AmqpProxyFactoryBean amqpFactoryBean(ConnectionFactory factory) {
        AmqpProxyFactoryBean factoryBean = new AmqpProxyFactoryBean();
        factoryBean.setServiceInterface(CabBookingService.class);
        factoryBean.setAmqpTemplate(amqpTemplate(factory));
        return factoryBean;
    }
*/
    @Bean
    CabBookingService amqpFactoryBean(RabbitTemplate amqpTemplate) {
        AmqpProxyFactoryBean factoryBean = new AmqpProxyFactoryBean();
        factoryBean.setServiceInterface(CabBookingService.class);
        // factoryBean.setAmqpTemplate(amqpTemplate(factory));
        factoryBean.setAmqpTemplate(amqpTemplate);
        factoryBean.afterPropertiesSet();
        return (CabBookingService) factoryBean.getObject();
    }

    public static void main(String[] args) throws BookingException {
        CabBookingService service = SpringApplication.run(AmqpClient.class, args).getBean(CabBookingService.class);
        out.println(service.bookRide("13 Seagate Blvd, Key Largo, FL 33037"));
    }

}
