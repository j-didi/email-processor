package br.com.didi.mail.analytics.events;

import br.com.didi.mail.shared.EmailEventMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class Repository {
    private final JdbcTemplate database;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Sao_Paulo");

    public Repository(JdbcTemplate database) {
        this.database = database;
    }

    public void processCompactEventsInBatch(List<EmailEventMessage> events) {

        List<EmailEventMessage> processedBatch = new ArrayList<>();
        List<EmailEventMessage> deliveredBatch = new ArrayList<>();
        List<EmailEventMessage> openedBatch = new ArrayList<>();
        List<EmailEventMessage> bouncedBatch = new ArrayList<>();
        List<EmailEventMessage> droppedBatch = new ArrayList<>();

        for (EmailEventMessage event : events) {
            if (event.event() == null) continue;

            switch (event.event().toLowerCase()) {
                case "processed" -> processedBatch.add(event);
                case "delivered" -> deliveredBatch.add(event);
                case "open" -> openedBatch.add(event);
                case "bounce" -> bouncedBatch.add(event);
                case "dropped" -> droppedBatch.add(event);
            }
        }

        executeUpsert("total_processed", processedBatch);
        executeUpsert("total_delivered", deliveredBatch);
        executeUpsert("total_opened", openedBatch);
        executeUpsert("total_bounce", bouncedBatch);
        executeUpsert("total_dropped", droppedBatch);
    }

    private void executeUpsert(String targetColumn, List<EmailEventMessage> batchList) {
        if (batchList.isEmpty()) {
            return;
        }

        String sql = """
                    INSERT INTO email_daily_stats (stats_date, tenant_id, subject, %s)
                    VALUES (?, ?, ?, 1)
                    ON CONFLICT (stats_date, tenant_id, subject)
                    DO UPDATE SET
                        %s = email_daily_stats.%s + 1,
                        last_update_date = CURRENT_TIMESTAMP;
                """.formatted(targetColumn, targetColumn, targetColumn);

        try {
            database.batchUpdate(sql, batchList, batchList.size(), (PreparedStatement ps, EmailEventMessage event) -> {
                LocalDate eventDate = LocalDate.now(DEFAULT_ZONE);

                if (event.timestamp() != null && event.timestamp() > 0) {
                    long ts = event.timestamp();
                    Instant instant = (ts > 9999999999L) ? Instant.ofEpochMilli(ts) : Instant.ofEpochSecond(ts);
                    eventDate = instant.atZone(DEFAULT_ZONE).toLocalDate();
                }

                ps.setObject(1, eventDate);
                ps.setString(2, event.tenantId());
                ps.setString(3, event.subject());
            });
        } catch (Exception e) {
            System.err.println("Erro crítico no batchUpdate da coluna " + targetColumn + ": " + e.getMessage());
        }
    }
}
