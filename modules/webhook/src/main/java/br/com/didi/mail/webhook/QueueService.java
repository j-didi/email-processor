package br.com.didi.mail.webhook;

import br.com.didi.mail.shared.EmailEventMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QueueService {
    private final RabbitTemplate rabbitMq;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${queue.name}")
    private String queue;

    public QueueService(RabbitTemplate rabbitTemplate) {
        this.rabbitMq = rabbitTemplate;
    }

    public void sendToQueue(List<EmailEventMessage> events) {
        try {
            String json = mapper.writeValueAsString(events);
            MessageProperties props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);

            Message message = new Message(json.getBytes(), props);
            rabbitMq.send("", queue, message);

        } catch (Exception e) {
            System.err.println("Erro crítico ao postar lote de analytics no RabbitMQ: " + e.getMessage());
            throw new RuntimeException("Falha no envio para a fila de métricas", e);
        }
    }
}
