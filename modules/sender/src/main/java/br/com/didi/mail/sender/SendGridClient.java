package br.com.didi.mail.sender;

import br.com.didi.mail.shared.SendEmailMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class SendGridClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${sendgrid.url}")
    private String sendGridUrl;

    @Value("${sendgrid.api-key}")
    private String apiKey;

    public void sendAsync(SendEmailMessage sendEmailMessage, String emailHtml) {
        try {

            Map<String, Object> payload = buildPayload(sendEmailMessage, emailHtml);
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = buildRequest(json);

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 202 || response.statusCode() == 200) {
                            System.out.println("E-mail aceito de forma assíncrona pelo SendGrid!");
                        } else {
                            System.err.println("SendGrid recusou o envio assíncrono. Status: " + response.statusCode() + " | Erro: " + response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("Falha grave de rede ao enviar e-mail de forma assíncrona: " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("Erro ao serializar payload para o SendGrid: " + e.getMessage());
        }
    }

    private HttpRequest buildRequest(String jsonBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(sendGridUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(5))
                .build();
    }

    private static @NonNull Map<String, Object> buildPayload(SendEmailMessage sendEmailMessage, String emailHtml) {
        return Map.of(
                "personalizations", buildPersonalization(sendEmailMessage),
                "from", Map.of("email", sendEmailMessage.from()),
                "subject",sendEmailMessage.subject(),
                "content", List.of(Map.of(
                        "type", "text/html",
                        "value", emailHtml
                )),
                "custom_args", Map.of(
                        "tenant_id", sendEmailMessage.tenantId(),
                        "email_subject", sendEmailMessage.subject()
                )
        );
    }

    private static @NonNull List<Map<String, Object>> buildPersonalization(SendEmailMessage sendEmailMessage) {
        return List.of(Map.of(
                "to", sendEmailMessage.to().stream().map(email -> Map.of("email", email)).toList(),
                "cc", sendEmailMessage.cc().stream().map(email -> Map.of("email", email)).toList(),
                "bcc", sendEmailMessage.bcc().stream().map(email -> Map.of("email", email)).toList()
        ));
    }
}
