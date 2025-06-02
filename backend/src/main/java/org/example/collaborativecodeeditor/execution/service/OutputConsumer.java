package org.example.collaborativecodeeditor.execution.service;


import org.example.collaborativecodeeditor.execution.dto.OutputMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutputConsumer {

    @Autowired
    private ExecutionService executionService;

    @RabbitListener(queues = {"outputQueue"})
    public void consume(OutputMessage outputMessage) {
        try {
            System.out.println(outputMessage);
            executionService.handleResult(outputMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
