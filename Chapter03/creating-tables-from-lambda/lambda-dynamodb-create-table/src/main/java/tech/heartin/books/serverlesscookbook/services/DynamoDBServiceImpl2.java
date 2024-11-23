package tech.heartin.books.serverlesscookbook.services;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

/**
 * Implementation of DynamoDBService that uses DynamoDbClient from AWS SDK v2..<br/>
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_dynamodb_code_examples.html
 */
public class DynamoDBServiceImpl2 implements DynamoDBService {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDBServiceImpl2() {
        this.dynamoDbClient = DynamoDbClient.create();
    }

    @Override
    public final Response createTable(final Request request) {
        try {
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName(request.getPartitionKey())
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName(request.getSortKey())
                        .attributeType(ScalarAttributeType.N)
                        .build()
                )
                .tableName(request.getTableName())
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName(request.getPartitionKey())
                        .keyType(KeyType.HASH)
                        .build(),
                    KeySchemaElement.builder()
                        .attributeName(request.getSortKey())
                        .keyType(KeyType.RANGE)
                        .build()
                )
                .provisionedThroughput(ProvisionedThroughput.builder()
                    .readCapacityUnits(request.getReadCapacityUnits())
                    .writeCapacityUnits(request.getWriteCapacityUnits())
                    .build())
                .build();

            // Check if table exists
            try {
                DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                    .tableName(request.getTableName())
                    .build();
                dynamoDbClient.describeTable(describeTableRequest);
                return new Response(request.getTableName() + " already exists.", null);
            } catch (ResourceNotFoundException e) {
                // Table doesn't exist, create it
                dynamoDbClient.createTable(createTableRequest);
            }

            // Wait for table to become active
            DynamoDbWaiter waiter = dynamoDbClient.waiter();
            DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
                .tableName(request.getTableName())
                .build();

            waiter.waitUntilTableExists(describeTableRequest);

            return new Response(request.getTableName() + " created with API version V2.", null);

        } catch (Exception e) {
            return new Response(null, "Failed to create table in API version V2: " + e.getMessage());
        }
    }
}
