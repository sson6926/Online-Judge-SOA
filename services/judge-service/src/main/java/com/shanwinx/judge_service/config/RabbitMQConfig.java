package com.shanwinx.judge_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String JOB_QUEUE = "judge.jobs";
    public static final String RESULT_QUEUE = "judge.results";
    public static final String RESULT_DLQ = "judge.results.dlq";
    public static final String DLX_EXCHANGE = "judge.dlx";

    @Bean
    public Queue jobQueue() {
        return new Queue(JOB_QUEUE, true);
    }

    @Bean
    public Queue resultQueue() {
        return QueueBuilder.durable(RESULT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "result.error")
                .build();
    }

    @Bean
    public Queue resultDlq() {
        return new Queue(RESULT_DLQ, true);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding resultDlqBinding() {
        return BindingBuilder.bind(resultDlq()).to(deadLetterExchange()).with("result.error");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
