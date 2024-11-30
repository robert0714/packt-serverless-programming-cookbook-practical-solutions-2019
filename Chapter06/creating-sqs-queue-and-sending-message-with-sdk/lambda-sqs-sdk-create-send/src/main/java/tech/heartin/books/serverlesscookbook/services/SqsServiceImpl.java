package tech.heartin.books.serverlesscookbook.services;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

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
    public final Response createQueueAndSendMessage(final Request request, final LambdaLogger logger) {

        String errorMessage;

        try {
            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(request.getQueueName())
                    .build();

            CreateQueueResponse createQueueResponse = this.sqsClient.createQueue(createQueueRequest);
            logger.log("Created queue: " +  createQueueResponse.queueUrl());

        } catch (SdkException e) {

            if (e.getMessage().contains("QueueAlreadyExists")) {
                errorMessage = "QueueAlreadyExists: " + request.getQueueName();
            } else {
                errorMessage = "Error during queue creation: " + e.getMessage();
            }

            logger.log(errorMessage);
            return new Response(errorMessage);
        }

        String queueUrl;
        try {
            queueUrl = this.sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                    .queueName(request.getQueueName())
                    .build()).queueUrl();
        } catch (SdkException e) {
            errorMessage = "Error fetching queue URL: " + e.getMessage();
            logger.log(errorMessage);
            return new Response(errorMessage);
        }

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(request.getMessage())
                .delaySeconds(5)
                .build();
        try {
            this.sqsClient.sendMessage(sendMessageRequest);
        } catch (Exception e) {
            errorMessage = "Exception while sending message: " + e.getMessage();
            logger.log(errorMessage);
            return new Response(errorMessage);
        }

        return new Response("Successfully sent message to queue: " + request.getQueueName());

    }

}
