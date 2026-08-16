package br.com.didi.mail.entrypoint.apikey;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiKeyService {

    private final JdbcTemplate database;
    private final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    public ApiKeyService(JdbcTemplate database) {
        this.database = database;
    }

    @PostConstruct
    public void load() {
        String sql = "SELECT id, priority FROM api_key WHERE active = true";
        database.query(sql, rs -> {
            cache.put(rs.getString("id"), rs.getBoolean("priority"));
        });
    }

    public boolean isValid(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return cache.containsKey(key);
    }

    public boolean isPriority(String key) {
        Boolean isPriority = cache.get(key);
        return isPriority != null && isPriority;
    }
}
