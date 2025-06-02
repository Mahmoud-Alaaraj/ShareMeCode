package org.example.collaborativecodeeditor.execution.service;

import org.example.collaborativecodeeditor.execution.dto.CodeMessage;
import org.example.collaborativecodeeditor.execution.configuration.MessageQueueConstants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultCodePublisher implements CodePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendCode(CodeMessage codeMessage) {
        rabbitTemplate.convertAndSend(MessageQueueConstants.EXCHANGE_NAME.getValue(),
                MessageQueueConstants.CODE_KEY.getValue(),
                codeMessage);
    }
}
