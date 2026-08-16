package br.com.didi.mail.entrypoint.blocklist;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockListService {

    private final JdbcTemplate database;

    private final Set<String> cache = ConcurrentHashMap.newKeySet();

    public BlockListService(JdbcTemplate database) {
        this.database = database;
    }

    @PostConstruct
    public void load() {
        String sql = "SELECT email FROM block_list WHERE active = true";
        List<String> emails = database.queryForList(sql, String.class);
        cache.clear();
        cache.addAll(emails);
    }

    public boolean isEmailBlocked(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return cache.contains(email.trim().toLowerCase());
    }
}