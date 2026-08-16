package br.com.didi.mail.webhook;

import br.com.didi.mail.shared.EmailEventMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {

    private final QueueService queueService;

    public Controller(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/emails/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody List<SendGridEvent> events) {
        if (events == null || events.isEmpty()) {
            return ResponseEntity.ok().build();
        }

        List<EmailEventMessage> compactBatch = events.stream()
                .map(event -> {
                    String tenantId = (String) event.customArgs().get("tenant_id");
                    String subject = (String) event.customArgs().get("email_subject");

                    return new EmailEventMessage(
                            event.email(),
                            event.event(),
                            event.timestamp(),
                            tenantId,
                            subject
                    );
                })
                .toList();

        queueService.sendToQueue(compactBatch);

        return ResponseEntity.ok().build();
    }
}
