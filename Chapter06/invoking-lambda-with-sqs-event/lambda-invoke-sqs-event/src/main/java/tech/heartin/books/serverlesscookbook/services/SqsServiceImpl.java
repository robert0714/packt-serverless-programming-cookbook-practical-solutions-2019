package tech.heartin.books.serverlesscookbook.services;

import java.util.ArrayList;
import java.util.Collection;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;

/**
 * Implementation class for SqsService.
 */
public class SqsServiceImpl implements SqsService {

    private final SqsClient sqsClient;

    public SqsServiceImpl(final SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public final Boolean processEvent(final SQSEvent event, final String outputQueueURL, final LambdaLogger logger) {

        try {

            logger.log("Number of messages in event: " + event.getRecords().size());

            logger.log("Output Queue URL: " + outputQueueURL);

            Collection<SendMessageBatchRequestEntry> entries = new ArrayList<>();

            int idVal = 1;
            for (SQSMessage m : event.getRecords()) {
                logger.log("Adding message: " + m.getBody());
                entries.add(SendMessageBatchRequestEntry.builder()
                        .id("id_" + idVal)
                        .messageBody(m.getBody())
                        .build());
                idVal++;
            }

            SendMessageBatchRequest sendBatchRequest = SendMessageBatchRequest.builder()
                    .queueUrl(outputQueueURL)
                    .entries(entries)
                    .build();

            SendMessageBatchResponse response = sqsClient.sendMessageBatch(sendBatchRequest);
            if (!response.failed().isEmpty()) {
                response.failed().forEach(failure ->
                    logger.log("Failed message ID: " + failure.id() + ", Reason: " + failure.message())
                );
                return false;
            } else {
                logger.log("All messages sent successfully.");
            }

        } catch (Exception e) {
            final String errorMessage = "Error occurred: " + e.getMessage();
            logger.log(errorMessage);
            return false;
        }

        return true;

    }

}
