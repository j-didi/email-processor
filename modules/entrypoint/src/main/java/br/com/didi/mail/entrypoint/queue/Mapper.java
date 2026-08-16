package br.com.didi.mail.entrypoint.queue;

import br.com.didi.email.client.SendEmailRequest;
import br.com.didi.mail.shared.SendEmailMessage;

public class Mapper {
    public static SendEmailMessage fromProtoToMessage(SendEmailRequest request) {
        return new SendEmailMessage(
                request.getFrom(),
                request.getToList(),
                request.getCcList(),
                request.getBccList(),
                request.getSubject(),
                request.getTemplateId(),
                request.getTenantId(),
                request.getVariablesMap()
        );
    }
}
