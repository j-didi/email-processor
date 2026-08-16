package br.com.didi.mail.shared;

public record EmailEventMessage(
        String email,
        String event,
        Long timestamp,
        String tenantId,
        String subject
) {}
