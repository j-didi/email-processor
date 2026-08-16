package br.com.didi.mail.entrypoint.apikey;

import br.com.didi.mail.entrypoint.grpc.EmailService;
import io.grpc.*;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
public class ApiKeyInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <Request, Response> ServerCall.Listener<Request> interceptCall(
            ServerCall<Request, Response> call,
            Metadata headers,
            ServerCallHandler<Request, Response> next
    ) {

        Context context = Context.current().withValue(
                EmailService.API_KEY,
                headers.get(AUTHORIZATION_METADATA_KEY)
        );

        return Contexts.interceptCall(context, call, headers, next);
    }
}
