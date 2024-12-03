package tech.heartin.books.serverlesscookbook.services;

import java.util.ArrayList;
import java.util.Collection;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;

/**
 * Implementation class for SnsService.
 */
public class SnsServiceImpl implements SnsService {

    private final SqsClient sqsClient;

    public SnsServiceImpl(final SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public final Boolean processEvent(final SNSEvent event, final String outputQueueURL, final LambdaLogger logger) {

        try {

            logger.log("Number of records in event: " + event.getRecords().size());

            Collection<SendMessageBatchRequestEntry> entries = new ArrayList<>();

            int idVal = 1;
            for (SNSRecord r : event.getRecords()) {
                logger.log("Adding message: " + r.getSNS().getMessage());
                entries.add(SendMessageBatchRequestEntry.builder()
                        .id("id_" + idVal)
                        .messageBody(r.getSNS().getMessage())
                        .build());
                idVal++;
            }

            SendMessageBatchRequest sendBatchRequest = SendMessageBatchRequest.builder()
                        .queueUrl(outputQueueURL)
                        .entries(entries)
                        .build();
            SendMessageBatchResponse response = sqsClient.sendMessageBatch(sendBatchRequest);
            if (response.failed().isEmpty()) {
                logger.log("All messages sent successfully.");
            } else {
                logger.log("Some messages failed to send.");
                response.failed().forEach(f -> logger.log("Failed message ID: " + f.id()));
                return false;
            }

        } catch (Exception e) {
            final String errorMessage = "Error occurred: " + e.getMessage();
            logger.log(errorMessage);
            return false;
        }

        return true;

    }

}
