package com.amqpremote.demo.client;

import com.amqpremote.demo.api.BookingException;
import com.amqpremote.demo.api.CabBookingService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.remoting.client.AmqpProxyFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static java.lang.System.out;

/**
 * rpc 客户端要把rpc请求发送到exchange, 所以在这里要声明 exchange 及exchange与队列的绑定.
 * 同时由于发送rpc 请求是通过amqpTemplate的sendMessage进行发送, 没有指定exchange跟RoutingKey, 所以要给amqpTempate配制默认的
 * exchange跟RoutingKey, 且这里的RoutingKey还有跟前面绑定的RoutingKey一致才可以正常的发送.
 *
 * 同时这里还可以利用 AmqpProxyFactoryBean 导出具体类型
 */
@SpringBootApplication
public class AmqpClient {

    @Bean
    Queue queue() {
        return new Queue("remotingQueue");
    }

    /*@Bean
    AmqpProxyFactoryBean amqpFactoryBean(AmqpTemplate amqpTemplate) {
        AmqpProxyFactoryBean factoryBean = new AmqpProxyFactoryBean();
        factoryBean.setServiceInterface(CabBookingService.class);
        factoryBean.setAmqpTemplate(amqpTemplate);
        factoryBean.setRoutingKey("remoting.binding");
        return factoryBean;
    }*/

    @Bean
    AmqpProxyFactoryBean amqpFactoryBean(ConnectionFactory connectionFactory) {
        AmqpProxyFactoryBean factoryBean = new AmqpProxyFactoryBean();
        factoryBean.setServiceInterface(CabBookingService.class);
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        // template.setRoutingKey("remoting.binding"); // 这里不写的话, 就要用 factoryBean.setRoutingKey 设置值
        template.setExchange("remoting.exchange");  // 这个一定要写
        factoryBean.setAmqpTemplate(template);
        factoryBean.setRoutingKey("remoting.binding");
        return factoryBean;
    }


    /*  还可以这样做
    // 导出具体的类型
    @Bean
    CabBookingService amqpFactoryBean(AmqpTemplate amqpTemplate) {
        AmqpProxyFactoryBean factoryBean = new AmqpProxyFactoryBean();
        factoryBean.setServiceInterface(CabBookingService.class);
        factoryBean.setAmqpTemplate(amqpTemplate);
        factoryBean.afterPropertiesSet();
        return (CabBookingService) factoryBean.getObject();
    }*/

    @Bean
    Exchange directExchange() {
        return new DirectExchange("remoting.exchange");
    }

    // 这里的binding一定要独立提取出来, 配制成Bean, 这样rabbitAdmin才会帮你做binding
    @Bean
    Binding b1() {
        return BindingBuilder.bind(queue()).to(directExchange()).with("remoting.binding").noargs();
    }

    /* 还可以这样写, 直接返回具体的类型. 然后做binding里就可以少一步noargs. 因为已是具体的类型
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange("remoting.exchange");
    }

    // 这里的binding一定要独立提取出来, 配制成Bean, 这样rabbitAdmin才会帮你做binding
    @Bean
    Binding b1() {
        return BindingBuilder.bind(queue()).to(directExchange()).with("remoting.binding");
    }*/

    // 发起rpc调用的 amqpTemplate, 一定要给他指定默认的 RoutingKey, Exchange, 且要跟bind里绑定的一致. 不然 AmqpProxyFactoryBean 不知道往哪里发
    // 或者也可以在 AmqpProxyFactoryBean 设置 RoutingKey
    @Bean
    RabbitTemplate amqpTemplate(ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setRoutingKey("remoting.binding");
        template.setExchange("remoting.exchange");
        return template;
    }

    public static void main(String[] args) throws BookingException {
        CabBookingService service = SpringApplication.run(AmqpClient.class, args).getBean(CabBookingService.class);
        out.println(service.bookRide("13 Seagate Blvd, Key Largo, FL 33037"));
    }

}
