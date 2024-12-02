package tech.heartin.books.serverlesscookbook;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import tech.heartin.books.serverlesscookbook.domain.Request;

/**
 * RequestHandler implementation.
 */
public final class LambdaSnsPublishHandler implements RequestHandler<Request, String> {

    private final SnsClient  snsClient;

    public LambdaSnsPublishHandler() {
        this.snsClient = SnsClient.builder()
              .region(Region.of(System.getenv("AWS_REGION")))
              .build();
    }

    /**
     * Handle request.
     *
     * @param request  - input to lambda handler.
     * @param context - context object.
     * @return Message id of the published message.
     */
    public String handleRequest(final Request request, final Context context) {
        context.getLogger().log("Received Request: " + request);

        final PublishResponse  result;
        try {
            PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(request.getTopicArn())
                .message(request.getMessage())
                .build();
            result = snsClient.publish(publishRequest);
        } catch (Exception e) {
            return "Exception occurred: " + e.getMessage();
        }

        return "Message Id: " + result.messageId();
    }
}
