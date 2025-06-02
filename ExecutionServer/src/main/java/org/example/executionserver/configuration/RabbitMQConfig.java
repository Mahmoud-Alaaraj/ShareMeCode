package org.example.executionserver.configuration;

import org.example.executionserver.enums.MessageQueueConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue codeQueue() {
        return new Queue(MessageQueueConstants.CODE_QUEUE.getValue());
    }

    @Bean
    public Queue outputQueue() {
        return new Queue(MessageQueueConstants.OUTPUT_QUEUE.getValue());
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(MessageQueueConstants.EXCHANGE_NAME.getValue());
    }

    @Bean
    public Binding codeQueueBinding() {
        return BindingBuilder
                .bind(codeQueue())
                .to(exchange())
                .with(MessageQueueConstants.CODE_KEY.getValue());
    }

    @Bean
    public Binding outputQueueBinding() {
        return BindingBuilder
                .bind(outputQueue())
                .to(exchange())
                .with(MessageQueueConstants.OUTPUT_KEY.getValue());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}