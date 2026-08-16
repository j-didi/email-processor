package br.com.didi.mail.analytics.presenter;

import br.com.didi.email.analytics.client.AnalyticsReportRequest;
import br.com.didi.email.analytics.client.AnalyticsReportResponse;
import br.com.didi.email.analytics.client.DailyStatsDetail;
import br.com.didi.email.analytics.client.EmailAnalyticsServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
public class EmailAnalyticsService extends EmailAnalyticsServiceGrpc.EmailAnalyticsServiceImplBase {

    private final Repository queryService;

    public EmailAnalyticsService(Repository queryService) {
        this.queryService = queryService;
    }

    @Override
    public void getDailyStatsReport(
            AnalyticsReportRequest request,
            StreamObserver<AnalyticsReportResponse> responseObserver) {

        try {

            List<DailyStatsDetail> details = queryService.getReportDetails(request);

            int periodProcessed = 0;
            int periodDelivered = 0;
            int periodOpened = 0;
            int periodBounce = 0;
            int periodDropped = 0;

            for (DailyStatsDetail detail : details) {
                periodProcessed += detail.getTotalProcessed();
                periodDelivered += detail.getTotalDelivered();
                periodOpened += detail.getTotalOpened();
                periodBounce += detail.getTotalBounce();
                periodDropped += detail.getTotalDropped();
            }

            AnalyticsReportResponse response = AnalyticsReportResponse.newBuilder()
                    .setTenantId(request.getTenantId())
                    .setPeriodTotalProcessed(periodProcessed)
                    .setPeriodTotalDelivered(periodDelivered)
                    .setPeriodTotalOpened(periodOpened)
                    .setPeriodTotalBounce(periodBounce)
                    .setPeriodTotalDropped(periodDropped)
                    .addAllDetails(details)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            System.err.println("Erro ao processar consulta gRPC de relatorios: " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Erro interno ao processar o relatorio analitico.")
                    .asRuntimeException());
        }
    }
}