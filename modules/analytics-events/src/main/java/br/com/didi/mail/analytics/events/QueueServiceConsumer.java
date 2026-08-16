package br.com.didi.mail.analytics.events;

import br.com.didi.mail.shared.EmailEventMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueueServiceConsumer {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Repository repository;

    public QueueServiceConsumer(Repository repository) {
        this.repository = repository;
    }

    @RabbitListener(
            queues = "${queue.name}",
            concurrency = "${queue.concurrency}"
    )
    public void consumeAnalyticsBatch(String payload) {
        try {
            List<EmailEventMessage> compactEvents = mapper.readValue(payload, new TypeReference<>(){});
            repository.processCompactEventsInBatch(compactEvents);
        } catch (Exception e) {
            System.err.println("Erro ao processar lote analítico na fila: " + e.getMessage());
            throw new RuntimeException("Falha no processamento do lote de métricas", e);
        }
    }
}
