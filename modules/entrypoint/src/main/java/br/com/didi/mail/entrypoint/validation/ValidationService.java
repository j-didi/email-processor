package br.com.didi.mail.entrypoint.validation;

import br.com.didi.email.client.SendEmailRequest;
import br.com.didi.mail.entrypoint.apikey.ApiKeyService;
import br.com.didi.mail.entrypoint.blocklist.BlockListService;
import br.com.didi.mail.entrypoint.template.TemplateService;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ValidationService {
    private static final EmailValidator VALIDATOR = EmailValidator.getInstance();

    private final ApiKeyService apiKeyService;
    private final BlockListService blockListService;
    private final TemplateService templateService;

    public ValidationService(
            ApiKeyService apiKeyService,
            BlockListService blockListService,
            TemplateService templateService
    ) {
        this.apiKeyService = apiKeyService;
        this.blockListService = blockListService;
        this.templateService = templateService;
    }

    public SendEmailValidation validateRequest(SendEmailRequest request, String apiKey) {
        boolean apiKeyIsValid = apiKeyService.isValid(apiKey);
        boolean templateExists = templateService.templateExists(request.getTemplateId());

        boolean templateVariablesMatch = false;

        if (templateExists) {
            Set<String> inputVariables = request.getVariablesMap().keySet();
            templateVariablesMatch = templateService.templateMatchesVariables(request.getTemplateId(), inputVariables);
        }

        List<String> allRecipients = mergeRecipients(request);
        List<String> blockedEmails = new ArrayList<>();
        List<String> invalidSyntaxEmails = new ArrayList<>();

        for (String email : allRecipients) {
            if (email == null || email.isBlank()) {
                continue;
            }

            if (!VALIDATOR.isValid(email)) {
                invalidSyntaxEmails.add(email);
            }

            if (blockListService.isEmailBlocked(email)) {
                blockedEmails.add(email);
            }
        }

        return new SendEmailValidation(
                blockedEmails,
                invalidSyntaxEmails,
                apiKeyIsValid,
                templateExists,
                templateVariablesMatch
        );
    }

    private List<String> mergeRecipients(SendEmailRequest request) {
        List<String> allRecipients = new ArrayList<>();
        allRecipients.addAll(request.getToList());
        allRecipients.addAll(request.getCcList());
        allRecipients.addAll(request.getBccList());
        return allRecipients;
    }
}
