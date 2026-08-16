package br.com.didi.mail.sender;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TemplateService {

    private final JdbcTemplate database;

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TemplateService(JdbcTemplate database) {
        this.database = database;
    }

    @PostConstruct
    public void load() {
        String sql = "SELECT id, content FROM email_template WHERE active = true";

        cache.clear();
        database.query(sql, rs -> {
            cache.put(rs.getString("id"), rs.getString("content"));
        });
    }

    public String buildHtml(String id, Map<String, String> variables) {
        // TODO: Implement build email logic
        return cache.get(id);
    }
}
