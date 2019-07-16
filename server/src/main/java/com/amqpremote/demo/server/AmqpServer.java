package com.amqpremote.demo.server;

import com.amqpremote.demo.api.CabBookingService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * rpc服务端只是监听rpc调用的队列, 所以可以不用做exchange及绑定队列的操作. amqpTemplate也只是用默认的就行.
 * 而做为rpc client端只有所不同. 参见 AmqpClient类的注释
 */
@SpringBootApplication
public class AmqpServer {

    /*
    Please note that
    - CachingConnectionFactory
    - RabbitAdmin
    - AmqpTemplate
    are automatically declared by SpringBoot.
     */

    @Bean
    CabBookingService bookingService() {
        return new CabBookingServiceImpl();
    }

    @Bean
    Queue queue() {
        return new Queue("remotingQueue");
    }

    @Bean
    AmqpInvokerServiceExporter exporter(CabBookingService implementation, AmqpTemplate template) {
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
