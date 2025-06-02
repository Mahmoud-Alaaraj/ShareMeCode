package org.example.executionserver.service;

import org.example.executionserver.dto.CodeMessage;
import org.example.executionserver.dto.OutputMessage;
import org.example.executionserver.enums.MessageQueueConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeConsumer {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CodeExecutionService codeExecutionService;


    @RabbitListener(queues = {"codeQueue"})
    public void consume(CodeMessage codeMessage) {
        try {
            System.out.println(codeMessage);
            String output = codeExecutionService.runProjectInDocker(codeMessage);
            OutputMessage outputMessage = OutputMessage
                    .builder()
                    .output(output)
                    .submitID(codeMessage.getSubmitID())
                    .build();
            rabbitTemplate.convertAndSend(MessageQueueConstants.EXCHANGE_NAME.getValue(),
                    MessageQueueConstants.OUTPUT_KEY.getValue(),
                    outputMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
