package br.com.didi.mail.entrypoint.validation;

import br.com.didi.email.client.ErrorDetails;
import br.com.didi.email.client.SendEmailResponse;

import java.util.List;

public record SendEmailValidation(
        List<String> blockedEmails,
        List<String> invalidSyntaxEmails,
        boolean apiKeyIsValid,
        boolean templateExists,
        boolean templateMatchesVariables
) {

    public boolean isValid() {
        return apiKeyIsValid
                && templateExists
                && templateMatchesVariables
                && blockedEmails.isEmpty()
                && invalidSyntaxEmails.isEmpty();
    }

    public boolean isInvalid() {
        return !isValid();
    }

    public SendEmailResponse buildResponse() {
        if (isValid()) {
            return null;
        }

        ErrorDetails errorDetails = ErrorDetails.newBuilder()
                .addAllBlockedEmails(blockedEmails())
                .addAllInvalidSyntaxEmails(invalidSyntaxEmails())
                .setApiKeyIsValid(apiKeyIsValid())
                .setTemplateExists(templateExists())
                .setTemplateMatchesVariables(templateMatchesVariables())
                .build();

        return SendEmailResponse.newBuilder()
                .setSuccess(false)
                .setErrorDetails(errorDetails)
                .build();
    }


}
