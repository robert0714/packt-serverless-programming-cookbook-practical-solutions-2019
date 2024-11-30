package tech.heartin.books.serverlesscookbook.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.lambda.runtime.LambdaLogger; 
import software.amazon.awssdk.services.sqs.SqsClient; 
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest; 
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry; 
import software.amazon.awssdk.services.sqs.model.Message;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

/**
 * Implementation class for SqsService.
 */
public class SqsServiceImpl implements SqsService {

    private final SqsClient  sqsClient;

    public SqsServiceImpl(final SqsClient  sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public final Response sendMessage(final Request request, final LambdaLogger logger) {

        try {

            // Receive messages
            final ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(request.getInputQueueURL())
                    .maxNumberOfMessages(request.getMaxMessagesToReceive())
                    .build();

            final List<Message> messages = this.sqsClient.receiveMessage(receiveMessageRequest).messages();

            logger.log("Number of messages: " + messages.size());

            Collection<SendMessageBatchRequestEntry> entries = new ArrayList<>();

            int idVal = 1;
            for (Message m : messages) {
                logger.log("Adding message: " + m.body());
                entries.add(SendMessageBatchRequestEntry.builder()
                        .id("id_" + idVal)
                        .messageBody(m.body())
                        .delaySeconds(request.getDelay())
                        .build());
                idVal++;
            }

            // Send batch messages
            if (!entries.isEmpty()) {
                final SendMessageBatchRequest sendBatchRequest = SendMessageBatchRequest.builder()
                        .queueUrl(request.getOutputQueueURL())
                        .entries(entries)
                        .build();
                this.sqsClient.sendMessageBatch(sendBatchRequest);
            }

            // delete messages from the queue with receipt handle.
            for (Message m : messages) {
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(request.getInputQueueURL())
                        .receiptHandle(m.receiptHandle())
                        .build());
            }
        } catch (Exception e) {
            final String errorMessage = "Error occurred: " + e.getMessage();
            logger.log(errorMessage);
            return new Response(errorMessage);
        }

        return new Response("Message forwarded successfully");

    }


}
