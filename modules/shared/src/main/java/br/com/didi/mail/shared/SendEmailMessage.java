package br.com.didi.mail.shared;

import java.util.List;
import java.util.Map;

public record SendEmailMessage(
        String from,
        List<String> to,
        List<String> cc,
        List<String> bcc,
        String subject,
        String templateId,
        String tenantId,
        Map<String, String> variables
) {}
