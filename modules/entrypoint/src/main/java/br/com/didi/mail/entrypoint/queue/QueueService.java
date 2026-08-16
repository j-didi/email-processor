package br.com.didi.mail.entrypoint.queue;

import br.com.didi.mail.shared.SendEmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    private final RabbitTemplate rabbitMq;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${queue.default}")
    private String mainQueue;

    @Value("${queue.priority}")
    private String priorityQueue;

    public QueueService(RabbitTemplate rabbitMq) {
        this.rabbitMq = rabbitMq;
    }

    public void sendToQueue(SendEmailMessage message) {
        send(mainQueue, message);
    }

    public void sendToPriorityQueue(SendEmailMessage message) {
        send(priorityQueue, message);
    }

    private void send(String queueName, SendEmailMessage message) {
        try {
            String json = mapper.writeValueAsString(message);
            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            rabbitMq.send("", queueName, new Message(json.getBytes(), props));
        } catch (Exception e) {
            throw new RuntimeException("Falha no envio para o RabbitMQ", e);
        }
    }


}

