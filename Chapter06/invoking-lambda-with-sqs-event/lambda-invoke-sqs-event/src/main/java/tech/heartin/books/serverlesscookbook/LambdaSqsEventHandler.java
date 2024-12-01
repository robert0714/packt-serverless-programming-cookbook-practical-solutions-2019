package tech.heartin.books.serverlesscookbook;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.regions.Region;

import tech.heartin.books.serverlesscookbook.services.SqsService;
import tech.heartin.books.serverlesscookbook.services.SqsServiceImpl;

/**
 * RequestHandler implementation.
 */
public final class LambdaSqsEventHandler implements RequestHandler<SQSEvent, Boolean> {

    private final SqsClient sqsClient;

    public LambdaSqsEventHandler() {
         this.sqsClient = SqsClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .build();
    }

    /**
     * Handle request.
     *
     * @param sqsEvent  - SQS Event passed as input to lambda handler
     * @param context - context object
     * @return true if success, else false.
     */
    public Boolean handleRequest(final SQSEvent sqsEvent, final Context context) {
        context.getLogger().log("Received SQS event: " + sqsEvent);

        final SqsService sqsService =  new SqsServiceImpl(this.sqsClient);
        // It is a good practice to prefix environment variables with a project specific prefix.
        // E.g. SPC is a prefix that denote Serverless Programming Cookbook.
        return sqsService.processEvent(sqsEvent, System.getenv("SPC_OUTPUT_QUEUE_URL"), context.getLogger());

    }
}
