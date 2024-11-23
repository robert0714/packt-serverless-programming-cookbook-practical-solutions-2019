package tech.heartin.books.serverlesscookbook.services;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

/**
 * Implementation of DynamoDBService that uses AWS SDK v2 DynamoDbClient..<br/>
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_dynamodb_code_examples.html
 */
public class DynamoDBServiceImpl2 implements DynamoDBService {

    private final DynamoDbClient dynamoDBClient;

    public DynamoDBServiceImpl2() {
        this.dynamoDBClient = DynamoDbClient.builder().build();
    }

    @Override
    public final Response putItem(final Request request) {
        // Wait for table to become active if requested
        if (request.isWaitForActive()) {
            try (DynamoDbWaiter waiter = DynamoDbWaiter.builder()
                    .client(this.dynamoDBClient)
                    .build()) {
                waiter.waitUntilTableExists(r -> r.tableName(request.getTableName()));
            } catch (Exception e) {
                return new Response(null,
                    "Error while waiting for table to become active with API version V2: " + e.getMessage());
            }
        }

        // Prepare attribute values map
        Map<String, AttributeValue> attributeValueMap = new HashMap<>();

        // Add partition key
        attributeValueMap.put(request.getPartitionKey(),
            AttributeValue.builder().s(request.getPartitionKeyValue()).build());

        // Add sort key
        attributeValueMap.put(request.getSortKey(),
            AttributeValue.builder().n(request.getSortKeyValue().toString()).build());

        // Add string attributes
        if (request.getStringData() != null) {
            request.getStringData().forEach((k, v) ->
                attributeValueMap.put(k, AttributeValue.builder().s(v).build()));
        }

        // Add integer attributes
        if (request.getIntegerData() != null) {
            request.getIntegerData().forEach((k, v) ->
                attributeValueMap.put(k, AttributeValue.builder().n(v.toString()).build()));
        }

        // Create and execute PutItem request
        PutItemRequest putItemRequest = PutItemRequest.builder()
            .tableName(request.getTableName())
            .item(attributeValueMap)
            .build();

        this.dynamoDBClient.putItem(putItemRequest);

        return new Response("Item added into " + request.getTableName() + " with API version V2.", null);
    }

}
