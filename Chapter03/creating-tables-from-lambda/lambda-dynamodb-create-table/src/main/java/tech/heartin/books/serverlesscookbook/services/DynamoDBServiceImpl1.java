package tech.heartin.books.serverlesscookbook.services;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DynamoDBService that uses AWS SDK v2 DynamoDB client..<br/>
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_dynamodb_code_examples.html
 */
public class DynamoDBServiceImpl1 implements DynamoDBService {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDBServiceImpl1() {
        this.dynamoDbClient = DynamoDbClient.builder().build();
    }

    @Override
    public final Response createTable(final Request request) {
        try {
            if (tableExists(request.getTableName())) {
                return new Response(null, request.getTableName() + " already exists. Checked with version V2.");
            }

            // Prepare attribute definitions
            List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(
                AttributeDefinition.builder()
                    .attributeName(request.getPartitionKey())
                    .attributeType(ScalarAttributeType.S)
                    .build()
            );
            attributeDefinitions.add(
                AttributeDefinition.builder()
                    .attributeName(request.getSortKey())
                    .attributeType(ScalarAttributeType.N)
                    .build()
            );

            // Prepare key schema
            List<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(
                KeySchemaElement.builder()
                    .attributeName(request.getPartitionKey())
                    .keyType(KeyType.HASH)
                    .build()
            );
            keySchema.add(
                KeySchemaElement.builder()
                    .attributeName(request.getSortKey())
                    .keyType(KeyType.RANGE)
                    .build()
            );

            // Create table request
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(request.getTableName())
                .keySchema(keySchema)
                .attributeDefinitions(attributeDefinitions)
                .provisionedThroughput(
                    ProvisionedThroughput.builder()
                        .readCapacityUnits(request.getReadCapacityUnits())
                        .writeCapacityUnits(request.getWriteCapacityUnits())
                        .build()
                )
                .build();

            // Create table
            CreateTableResponse createTableResponse = dynamoDbClient.createTable(createTableRequest);

            // Wait for table to become active if requested
            if (request.isWaitForActive()) {
                waitForTableToBeActive(request.getTableName());
            }

            return new Response(request.getTableName() + " created with API version V2.", null);

        } catch (DynamoDbException e) {
            return new Response(null, "Error creating table: " + e.getMessage());
        }
    }

    private boolean tableExists(final String tableName) {
        try {
            dynamoDbClient.describeTable(
                DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build()
            );
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private void waitForTableToBeActive(final String tableName) {
        try {
            dynamoDbClient.waiter().waitUntilTableExists(
                DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error waiting for table to become active", e);
        }
    }
}
