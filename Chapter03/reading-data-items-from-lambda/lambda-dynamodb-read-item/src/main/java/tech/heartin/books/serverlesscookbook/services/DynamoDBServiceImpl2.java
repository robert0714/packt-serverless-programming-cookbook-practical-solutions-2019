package tech.heartin.books.serverlesscookbook.services;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

/**
 * Implementation of DynamoDBService that uses DynamoDbClient (SDK v2)..<br/>
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_dynamodb_code_examples.html
 */
public class DynamoDBServiceImpl2 implements DynamoDBService {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDBServiceImpl2() {
        this.dynamoDbClient = DynamoDbClient.create();
    }

    @Override
    public final Response getItem(final Request request) {
        final Map<String, AttributeValue> primaryKey = new HashMap<>();
        primaryKey.put(request.getPartitionKey(),
            AttributeValue.builder().s(request.getPartitionKeyValue()).build());
        primaryKey.put(request.getSortKey(),
            AttributeValue.builder().n(request.getSortKeyValue()).build());

        final GetItemResponse getItemResult = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(request.getTableName())
                .key(primaryKey)
                .build());

        return new Response("PK of Item read using get-item (V2): "
                + prepareKeyStr(getItemResult.item(), request), null);
    }

    @Override
    public final Response query(final Request request) {
        final Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":" + request.getPartitionKey(),
                AttributeValue.builder().s(request.getPartitionKeyValue()).build());

        final String keyConditionExpression = request.getPartitionKey() + "=:" + request.getPartitionKey();

        QueryRequest.Builder queryRequestBuilder = QueryRequest.builder()
                .tableName(request.getTableName())
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionAttributeValues);

        if (request.getFilterData() != null) {
            StringBuilder filterExpressionBuilder = new StringBuilder();
            processFilterData(request, filterExpressionBuilder, expressionAttributeValues);
            queryRequestBuilder.filterExpression(filterExpressionBuilder.toString());
        }

        final QueryResponse queryResult = dynamoDbClient.query(queryRequestBuilder.build());

        final StringBuilder response = new StringBuilder();
        response.append("PK of items read with query (V2): ");
        for (Map<String, AttributeValue> item : queryResult.items()) {
            response.append(prepareKeyStr(item, request));
        }

        return new Response(response.toString(), null);
    }

    @Override
    public final Response scan(final Request request) {
        final String projectionExpression = request.getPartitionKey() + ", " + request.getSortKey();

        ScanRequest.Builder scanRequestBuilder = ScanRequest.builder()
                .tableName(request.getTableName())
                .projectionExpression(projectionExpression);

        if (request.getFilterData() != null) {
            StringBuilder filterExpressionBuilder = new StringBuilder();
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            processFilterData(request, filterExpressionBuilder, expressionAttributeValues);
            scanRequestBuilder
                .filterExpression(filterExpressionBuilder.toString())
                .expressionAttributeValues(expressionAttributeValues);
        }

        final ScanResponse scanResult = dynamoDbClient.scan(scanRequestBuilder.build());

        final StringBuilder response = new StringBuilder();
        response.append("PK of items read with scan (V2): ");
        for (Map<String, AttributeValue> item : scanResult.items()) {
            response.append(prepareKeyStr(item, request));
        }

        return new Response(response.toString(), null);
    }

    private String prepareKeyStr(final Map<String, AttributeValue> item, final Request request) {
        return "(" + item.get(request.getPartitionKey()) + ", " + item.get(request.getSortKey()) + ") ";
    }

    private void processFilterData(final Request request,
            final StringBuilder filterExpressionBuilder,
            final Map<String, AttributeValue> expressionAttributeValues) {

        if (request.getFilterData() == null) {
            return;
        }

        request.getFilterData().forEach((k, v) -> {
            final String var = ":" + k;
            if (!filterExpressionBuilder.toString().isEmpty()) {
                filterExpressionBuilder.append(" and ");
            }
            filterExpressionBuilder.append(k + "=" + var);
            expressionAttributeValues.put(var, AttributeValue.builder().s(v).build());
        });
    }
}
