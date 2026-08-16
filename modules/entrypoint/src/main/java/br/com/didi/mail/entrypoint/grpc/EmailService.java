package br.com.didi.mail.entrypoint.grpc;

import br.com.didi.email.client.EmailServiceGrpc;
import br.com.didi.email.client.SendEmailRequest;
import br.com.didi.email.client.SendEmailResponse;
import br.com.didi.mail.entrypoint.queue.Mapper;
import br.com.didi.mail.entrypoint.apikey.ApiKeyService;
import br.com.didi.mail.entrypoint.queue.QueueService;
import br.com.didi.mail.entrypoint.validation.SendEmailValidation;
import br.com.didi.mail.entrypoint.validation.ValidationService;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class EmailService extends EmailServiceGrpc.EmailServiceImplBase {

    private final ValidationService validationService;
    private final ApiKeyService apiKeyService;
    private final QueueService queueService;
    public static final Context.Key<String> API_KEY = Context.key("apiKey");

    public EmailService(
            ValidationService ValidationService,
            ApiKeyService apiKeyService,
            QueueService queueService
    ) {
        this.validationService = ValidationService;
        this.apiKeyService = apiKeyService;
        this.queueService = queueService;
    }

    @Override
    public void sendEmail(SendEmailRequest request, StreamObserver<SendEmailResponse> responseObserver) {

        String apiKey = API_KEY.get();
        SendEmailValidation validationResult = validationService.validateRequest(request, apiKey);

        if (validationResult.isInvalid()) {
            responseObserver.onNext(validationResult.buildResponse());
            responseObserver.onCompleted();
            return;
        }

        if (apiKeyService.isPriority(apiKey)) {
            queueService.sendToPriorityQueue(Mapper.fromProtoToMessage(request));
        } else {
            queueService.sendToQueue(Mapper.fromProtoToMessage(request));
        }

        SendEmailResponse response = SendEmailResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}