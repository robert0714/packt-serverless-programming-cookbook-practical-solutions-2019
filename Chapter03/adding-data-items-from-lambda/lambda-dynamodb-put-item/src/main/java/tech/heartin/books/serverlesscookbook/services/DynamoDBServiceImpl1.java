package tech.heartin.books.serverlesscookbook.services;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of DynamoDBService that uses DynamoDB SDK v2 client.<br/>
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_dynamodb_code_examples.html
 */
public class DynamoDBServiceImpl1 implements DynamoDBService {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDBServiceImpl1() {
        this.dynamoDbClient = DynamoDbClient.create();
    }

    @Override
    public final Response putItem(final Request request) {
        try {
            // Wait for table to become active if required
            if (request.isWaitForActive()) {
                waitForTableToBeActive(request.getTableName());
            }

            // Prepare the item attributes
            Map<String, AttributeValue> itemAttributes = new HashMap<>();

            // Add primary key
            itemAttributes.put(request.getPartitionKey(),
                AttributeValue.builder().s(request.getPartitionKeyValue()).build());

            if (request.getSortKey() != null) {
                itemAttributes.put(request.getSortKey(),
                    AttributeValue.builder().n(String.valueOf(request.getSortKeyValue())).build());
            }

            // Add string attributes
            if (request.getStringData() != null) {
                request.getStringData().forEach((k, v) ->
                    itemAttributes.put(k, AttributeValue.builder().s(v).build())
                );
            }

            // Add integer attributes
            if (request.getIntegerData() != null) {
                request.getIntegerData().forEach((k, v) ->
                    itemAttributes.put(k, AttributeValue.builder().n(String.valueOf(v)).build())
                );
            }

            // Create PutItem request
            PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(request.getTableName())
                .item(itemAttributes)
                .build();

            // Put item into DynamoDB
            dynamoDbClient.putItem(putItemRequest);

            return new Response("Item added into " + request.getTableName() + " with API version V1.", null);

        } catch (Exception e) {
            return new Response(null,
                "Error while adding item with API version V1: " + e.getMessage());
        }
    }

    /**
     * Custom method to wait for table to become active.
     * @param tableName Name of the DynamoDB table
     * @throws InterruptedException if waiting is interrupted
     */
    private void waitForTableToBeActive(final String tableName) throws InterruptedException {

        // Maximum number of attempts
        int maxAttempts = Integer.parseInt(Optional.ofNullable(System.getenv("dynamodb.max.attempts"))
            .orElse(Optional.ofNullable(System.getProperty("dynamodb.max.attempts")).orElse("20")));

        // seconds
        int waitTimeBetweenAttempts = Integer.parseInt(
             Optional.ofNullable(System.getenv("dynamodb.wait.time.between.attempts"))
              .orElse(Optional.ofNullable(System.getProperty("dynamodb.wait.time.between.attempts")).orElse("5")));

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();

                TableStatus status = dynamoDbClient.describeTable(describeTableRequest)
                    .table().tableStatus();

                if (status == TableStatus.ACTIVE) {
                    return; // Table is active, exit method
                }
            } catch (Exception e) {
                // Log or handle exception if needed
            }

            // Wait before next attempt
            TimeUnit.SECONDS.sleep(waitTimeBetweenAttempts);
        }

        throw new InterruptedException("Table did not become active within the expected time");
    }
}
