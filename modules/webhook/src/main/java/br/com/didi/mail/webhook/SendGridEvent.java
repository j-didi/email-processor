package br.com.didi.mail.webhook;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SendGridEvent(
        String email,
        Long timestamp,
        String event,
        Map<String, Object> customArgs
) {
    public SendGridEvent {
        if (customArgs == null) {
            customArgs = new HashMap<>();
        }
    }

    @JsonAnySetter
    public void addCustomArg(String key, Object value) {
        this.customArgs.put(key, value);
    }
}
