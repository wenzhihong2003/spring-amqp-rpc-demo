package com.baeldung.server;

import com.baeldung.api.CabBookingService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AmqpServer {

    @Bean
    CabBookingService bookingService() {
        return new CabBookingServiceImpl();
    }

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

    @Bean
    AmqpInvokerServiceExporter exporter(CabBookingService implementation, RabbitTemplate template) {
        AmqpInvokerServiceExporter exporter = new AmqpInvokerServiceExporter();
        exporter.setServiceInterface(CabBookingService.class);
        exporter.setService(implementation);
        exporter.setAmqpTemplate(template);
        return exporter;
    }

    @Bean
    SimpleMessageListenerContainer listener(ConnectionFactory factory, AmqpInvokerServiceExporter exporter, Queue queue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
        container.setMessageListener(exporter);
        container.setQueueNames(queue.getName());
        return container;
    }

    public static void main(String[] args) {
        SpringApplication.run(AmqpServer.class, args);
    }

}