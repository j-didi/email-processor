package br.com.didi.mail.entrypoint.template;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService {

    private final JdbcTemplate database;

    private final Map<String, Set<String>> cache = new ConcurrentHashMap<>();

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");

    public TemplateService(JdbcTemplate database) {
        this.database = database;
    }

    @PostConstruct
    public void load() {
        String sql = "SELECT id, content FROM email_template WHERE active = true";

        cache.clear();
        database.query(sql, rs -> {
            cache.put(rs.getString("id"), extractUniqueVariables(rs.getString("content")));
        });
    }

    public boolean templateExists(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        return cache.containsKey(id);
    }

    public boolean templateMatchesVariables(String templateId, Set<String> variables) {
        Set<String> requiredVariables = cache.get(templateId);
        return requiredVariables.equals(variables);
    }

    private Set<String> extractUniqueVariables(String html) {

        Set<String> uniqueVariables = new HashSet<>();
        if (html == null || html.isBlank()) {
            return uniqueVariables;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(html);
        while (matcher.find()) {
            uniqueVariables.add(matcher.group(1));
        }
        return uniqueVariables;
    }
}
