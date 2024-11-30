package tech.heartin.books.serverlesscookbook;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.regions.Region;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;
import tech.heartin.books.serverlesscookbook.services.SqsService;
import tech.heartin.books.serverlesscookbook.services.SqsServiceImpl;

/**
 * RequestHandler implementation.
 */
public final class LambdaSqsSdkCreateSendHandler implements RequestHandler<Request, Response> {

    private final SqsClient sqsClient;

    public LambdaSqsSdkCreateSendHandler() {
        this.sqsClient = SqsClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }


    /**
     * Handle request.
     *
     * @param request  - input to lambda handler
     * @param context - context object
     * @return greeting text
     */
    public Response handleRequest(final Request request, final Context context) {
        context.getLogger().log("Received Request: " + request);

        final SqsService sqsService = new SqsServiceImpl(this.sqsClient);
        return sqsService.createQueueAndSendMessage(request, context.getLogger());

    }
}
