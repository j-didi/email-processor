package br.com.didi.mail.analytics.presenter;

import br.com.didi.email.analytics.client.AnalyticsReportRequest;
import br.com.didi.email.analytics.client.DailyStatsDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class Repository {

    private final JdbcTemplate database;

    public Repository(JdbcTemplate database) {
        this.database = database;
    }

    public List<DailyStatsDetail> getReportDetails(AnalyticsReportRequest analyticsReportRequest) {
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                    SELECT stats_date,
                           tenant_id,
                           subject,
                           total_processed,
                           total_delivered,
                           total_opened,
                           total_bounce,
                           total_dropped
                    FROM email_daily_stats
                    WHERE tenant_id = ? AND stats_date BETWEEN ? AND ?
                """);

        params.add(analyticsReportRequest.getTenantId());
        params.add(LocalDate.parse(analyticsReportRequest.getStartDate()));
        params.add(LocalDate.parse(analyticsReportRequest.getEndDate()));

        if (!analyticsReportRequest.getSubject().isBlank()) {
            sql.append(" AND subject = ?");
            params.add(analyticsReportRequest.getSubject());
        }

        sql.append(" ORDER BY stats_date ASC");

        return database.query(sql.toString(), (rs, rowNum) -> DailyStatsDetail.newBuilder()
                .setStatsDate(rs.getDate("stats_date").toString())
                .setSubject(rs.getString("subject"))
                .setTotalProcessed(rs.getInt("total_processed"))
                .setTotalDelivered(rs.getInt("total_delivered"))
                .setTotalOpened(rs.getInt("total_opened"))
                .setTotalBounce(rs.getInt("total_bounce"))
                .setTotalDropped(rs.getInt("total_dropped"))
                .build(), params.toArray());
    }
}
