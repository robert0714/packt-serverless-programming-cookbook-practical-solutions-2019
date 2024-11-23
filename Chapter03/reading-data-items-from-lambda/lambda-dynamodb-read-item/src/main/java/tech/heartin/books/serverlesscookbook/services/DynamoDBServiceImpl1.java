package tech.heartin.books.serverlesscookbook.services;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of DynamoDBService that uses DynamoDB SDK v2 client..<br/>
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_dynamodb_code_examples.html
 */
public class DynamoDBServiceImpl1 implements DynamoDBService {

    private final DynamoDbClient dynamoDB;

    public DynamoDBServiceImpl1() {
        this.dynamoDB = DynamoDbClient.builder().build();
    }

    @Override
    public final Response getItem(final Request request) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(request.getPartitionKey(), AttributeValue.builder().s(request.getPartitionKeyValue()).build());
        key.put(request.getSortKey(), AttributeValue.builder().n(request.getSortKeyValue()).build());

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(request.getTableName())
                .key(key)
                .build();

        GetItemResponse getItemResponse = dynamoDB.getItem(getItemRequest);
        Map<String, AttributeValue> item = getItemResponse.item();

        return new Response("PK of item read using get-item (V1): " + prepareKeyStr(item, request), null);
    }

    @Override
    public final Response query(final Request request) {
        String keyConditionExpression = request.getPartitionKey() + "=:" + request.getPartitionKey();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":" + request.getPartitionKey(),
            AttributeValue.builder().s(request.getPartitionKeyValue()).build());

        QueryRequest.Builder queryRequestBuilder = QueryRequest.builder()
                .tableName(request.getTableName())
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionAttributeValues);

        if (request.getFilterData() != null) {
            Map<String, String> filterExpressionMap = new HashMap<>();
            processFilterData(request, filterExpressionMap, expressionAttributeValues);
            queryRequestBuilder.filterExpression(String.join(" and ", filterExpressionMap.values()));
        }

        QueryResponse queryResponse = dynamoDB.query(queryRequestBuilder.build());

        StringBuilder response = new StringBuilder();
        response.append("PK of items read with query (V1): ");
        for (Map<String, AttributeValue> item : queryResponse.items()) {
            response.append(prepareKeyStr(item, request));
        }

        return new Response(response.toString(), null);
    }

    @Override
    public final Response scan(final Request request) {
        String projectionExpression = request.getPartitionKey() + ", " + request.getSortKey();

        ScanRequest.Builder scanRequestBuilder = ScanRequest.builder()
                .tableName(request.getTableName())
                .projectionExpression(projectionExpression);

        if (request.getFilterData() != null) {
            Map<String, String> filterExpressionMap = new HashMap<>();
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            processFilterData(request, filterExpressionMap, expressionAttributeValues);

            scanRequestBuilder
                .filterExpression(String.join(" and ", filterExpressionMap.values()))
                .expressionAttributeValues(expressionAttributeValues);
        }

        ScanResponse scanResponse = dynamoDB.scan(scanRequestBuilder.build());

        StringBuilder response = new StringBuilder();
        response.append("PK of items read with scan (V2): ");
        for (Map<String, AttributeValue> item : scanResponse.items()) {
            response.append(prepareKeyStr(item, request));
        }

        return new Response(response.toString(), null);
    }

    private String prepareKeyStr(final Map<String, AttributeValue> item, final Request request) {
        String partitionKeyValue = item.get(request.getPartitionKey()).s();
        String sortKeyValue = item.get(request.getSortKey()).n();
        return "(" + partitionKeyValue + ", " + sortKeyValue + ") ";
    }

    private void processFilterData(
            final Request request,
            final Map<String, String> filterExpressionMap,
            final Map<String, AttributeValue> expressionAttributeValues) {

        request.getFilterData().forEach((key, value) -> {
            String placeholder = ":" + key;
            filterExpressionMap.put(key, key + "=" + placeholder);

            // 假設所有過濾值都是字串類型，實際使用時可能需要根據值的類型進行調整
            expressionAttributeValues.put(placeholder,
                AttributeValue.builder().s(value.toString()).build());
        });
    }
}
