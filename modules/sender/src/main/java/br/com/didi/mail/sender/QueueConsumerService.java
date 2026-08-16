package br.com.didi.mail.sender;

import br.com.didi.mail.shared.SendEmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QueueConsumerService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SendGridClient sendGridClient;
    private final TemplateService templateService;

    public QueueConsumerService(
            SendGridClient sendGridClient,
            TemplateService templateService
    ) {
        this.sendGridClient = sendGridClient;
        this.templateService = templateService;
    }

    @RabbitListener(
            queues = "${queue.name}",
            concurrency = "${queue.concurrency}"
    )
    public void processEmailMessage(String payload) {
        try {
            SendEmailMessage email = mapper.readValue(payload, SendEmailMessage.class);
            String emailHtml = templateService.buildHtml(email.templateId(), email.variables());
            sendGridClient.sendAsync(email, emailHtml);
        } catch (Exception e) {
            System.err.println("Erro crítico ao desserializar o JSON da fila: " + e.getMessage());
            throw new RuntimeException("Falha na leitura da mensagem", e);
        }
    }
}
