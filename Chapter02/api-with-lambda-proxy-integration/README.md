# Lambda and API with proxy integration
In this recipe, we will configure our API to act as a proxy for a Lambda. We will create a new Lambda function that extracts the required data from the incoming request. While using proxy integration, Lambda needs to implement the low-level `RequestStreamHandler` that gives us access to the `InputStream` and `OutputStream`. 

## How to do it...
We will use proxy integration for our API, in order to pass the request to the Lambda without any changes. We will use the greedy path param `{proxy+}` under the root resource \, in order to catch all of the requests to its sub-resources. We will also add the `ANY` HTTP method over the greedy path to match for any type. 

> The requests to the root resource, \ are still not matched. If we need to match for \, as well, we should define and configure an ANY method over it.

## Deploying the Lambda
Upload the Lambda to our S3 bucket with the `aws s3 cp` command. Then, use the CloudFormation template that is provided to deploy the Lambda stack. There are not many changes in the template, except for the name and description. The export parameter name for the Lambda is defined as `LambdaForProxyIntegration`.


## Creating the Proxy API with CLI commands
First, let's create the REST API by using AWS CLI commands. We will not show how to use the commands that we already discussed in previous recipes. However, the complete commands will be available in the code files:
1. Create a REST API in API Gateway, using the `apigateway` sub-command `create-rest-api`.
2. et the root resource (/) of our API, using the `apigateway` sub-command `get-resources`.
3. Add a greedy path param, `'{proxy+}'`, for the proxy resource:
   ```bash
   aws apigateway create-resource \
      --rest-api-id qacob6w4v7 \
      --region us-east-1 \
      --parent-id xitaiyjnuf \
      --path-part '{proxy+}' \
      --profile admin
   ```
   > This resource will match any sub-resources for the parent /.
4. Use the `ANY` method over the proxy resource, in order to match any HTTP method, as follows:
   ```bash
   aws apigateway put-method \
    --rest-api-id qacob6w4v7 \
    --resource-id k7zima \
    --http-method ANY \
    --authorization-type "NONE" \
    --region us-east-1 \
    --profile admin
   ```
5. Add a response code for the ANY method, as follows:
   ```bash
   aws apigateway put-method-response \
    --rest-api-id qacob6w4v7 \
    --resource-id k7zima \
    --http-method ANY \
    --status-code 200 \
    --region us-east-1 \
    --profile admin
   ```
6. Execute `put-integration` with the `AWS_PROXY` integration type, as follows:
   ```bash
   aws apigateway put-integration \
      --rest-api-id qacob6w4v7 \
      --resource-id k7zima \
      --http-method ANY \
      --type AWS_PROXY \
      --integration-http-method POST \
      --uri 'arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:<account_id>:function:lambda-for-proxy-integration/invocations' \
      --region us-east-1 \
      --profile admin
   ```
7. Execute `put-integration-response` for the ANY method, as follows:
   ```bash
   aws apigateway put-integration-response \
   --rest-api-id qacob6w4v7 \
   --resource-id k7zima \
   --http-method ANY \
   --status-code 200 \
      --region us-east-1 \
      --selection-pattern "" \
      --profile admin
   ```
8. Create a deployment with the `dev` stage, using the `apigateway` sub-command `create-deployment`.
9. Give permission for the API to invoke the Lambda, as follows:
   ```bash
   aws lambda add-permission \
      --function-name lambda-for-proxy-integration \
      --statement-id apigateway-st-3 \
      --action lambda:InvokeFunction \
      --principal apigateway.amazonaws.com \
      --source-arn "arn:aws:execute-api:us-east-1:<account_id>:qacob6w4v7/*/*/{proxy+}" \
      --profile admin
   ```
10. Execute the API from a REST client (for example, Postman), as shown in the following screenshot:
    You should get a response similar to the following:
    ```json
    {
      "message": "Good Morning, Heartin. Welcome to MyApp. (Client User-Agent is Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36."
    }
   ```
    I sent a `POST` request from the Postman client, but a GET request from a browser will also work in this case, as the API's proxy resource has an ANY method, which can accept any HTTP method. Also, remember to replace the IDs with your own.

## Creating the API using CloudFormation templates
Now, let's create the API using a CloudFormation template. We will not discuss the components that were already discussed in previous recipes, nor will we discuss the theory for commands that were already discussed within the CLI commands section. The complete code is available in the code files:
1. Start to create the template with the version, description, and RestApi resource.
2. Add the proxy resource, `'{proxy+}'`, as follows:
   ```yaml
   MyProxyResource:
   Type: AWS::ApiGateway::Resource
   Properties:
      RestApiId: !Ref MyRestAPI
      ParentId: !GetAtt MyRestAPI.RootResourceId
      PathPart: '{proxy+}'
   ```    
3. Add the `Method` definition, with the `AWS_PROXY` integration type and `ANY` HTTP method, as follows:
   ```yaml
   MyMethod :
   Type : AWS::ApiGateway::Method
   Properties:
      AuthorizationType: NONE
      HttpMethod: ANY
      Integration:
         Type: AWS_PROXY
         IntegrationHttpMethod: POST
         IntegrationResponses:
         - StatusCode: 200
         Uri:
         !Sub
            - 'arn:aws:apigateway:${AWS::Region}:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:${AWS::AccountId}:function:${LAMBDA_NAME}/invocations'
            - LAMBDA_NAME: !ImportValue LambdaForProxyIntegration

      ResourceId: !Ref MyProxyResource
      RestApiId: !Ref MyRestAPI
      MethodResponses:
      - StatusCode: 200
   ```    
   > We are importing the Lambda LambdaForProxyIntegration. Therefore, the Lambda's CloudFormation template has to be executed first.
4. Add the deployment resource in a stage: `dev`.
5. Provide permission to the API to invoke the Lambda, as follows:
   ```yaml
   LambdaInvokePermission:
   Type: AWS::Lambda::Permission
   Properties:
      FunctionName: !ImportValue LambdaForProxyIntegration
      Action: 'lambda:InvokeFunction'
      Principal: apigateway.amazonaws.com
      SourceArn: !Sub
         - arn:aws:execute-api:${AWS::Region}:${AWS::AccountId}:${API_ID}/*/*/{proxy+}
         - API_ID: !Ref MyRestAPI
   ```       
6. Provide an `Outputs` section with a sample endpoint, as follows:
   ```yaml
   Outputs:
   SampleEndpoint:
      Description: 'Sample Endpoint'
      Value: !Sub
         - https://${API_ID}.execute-api.${AWS::Region}.amazonaws.com/dev/MyApp
         - API_ID: !Ref MyRestAPI
   ```        
7. Execute the `create-stack` command to deploy the API.
   Since we are importing the Lambda `LambdaForProxyIntegration`, the Lambda's CloudFormation template has to be executed first. 
8. Finally, test the API from a REST client, such as Postman (similar to what we did for the API that we created using the CLI commands). You can also use a browser for testing, which uses the `GET` method, as the API can accept any HTTP method.

## How it works...
Let's try to understand the theory behind Lambda proxy integration. You will also see how to define Lambdas for proxy integration, and how they are different from the other Lambdas that you have seen.

### The greedy path, the ANY HTTP method, and proxy integration
We can use the greedy path param, `{proxy+}`, under a resource, in order to catch all of the requests to the resource's sub-resources. For example, `/hello/{proxy+}` catches all of the resources under `hello/`. The `ANY` HTTP method matches for any HTTP method. Enabling proxy integration will make the API pass the raw request to the Lambda, as is.

We used all three of these together, but that is not a requirement. You can use any one of these, or a combination of them. For example, we can define a regular path parameter and a regular HTTP method, but enable proxy integration. This will forward raw requests to the Lambda only when the path and HTTP method matches.

### RequestStreamHandler versus RequestHandler
Implementations of the interface `RequestHandler<I, O>` accept and return POJOs. JSON payloads are mapped to the request POJO, and the response POJO is mapped to a JSON response. It can also accept and return a string payload.

The interface `RequestStreamHandler` is used for low-level request handling. The handler method provides access to InputStream for input, and OutputStream for output. `RequestStreamHandler` is generally used along with proxy integration. However, a good practice is to use `RequestHandler` and do all of the mappings within the API, whenever possible.


### The input and output format of a Lambda function for proxy integration
While using proxy integration with API Gateway, API Gateway passes the HTTP request to the Lambda in a particular format. Similarly, API Gateway expects the output in a particular format. Refer to the recipe to see how most of these are used in Java code.

The input format is as follows:
```json
{
 "resource": "The resource path",
 "path": "The path parameter",
 "httpMethod": "Incoming request's method name"
 "headers": {request headers}
 "queryStringParameters": {query string parameters }
 "pathParameters": {path parameters}
 "stageVariables": {Available stage variables}
 "requestContext": {Request context with authorizer-returned key-value pairs}
 "body": "A JSON string of the request payload."
 "isBase64Encoded": "A boolean flag that indicate if the applicable request payload is Base64-encoded"
}
```
The output format is as follows:
```json
{
 "isBase64Encoded": true|false,
 "statusCode": httpStatusCode,
 "headers": { headerName: headerValue key value pairs },
 "body": "body content"
}
```

## There's more...
In this chapter, you learned about building API Gateway APIs with mock integration, Lambda integration (AWS integration), and Lambda proxy integration (AWS_PROXY integration). You can also do HTTP and HTTP proxy integrations, and you can integrate API Gateway with other AWS services. Try to experiment with other integrations. 

There is more to learn about Lambda and API Gateway in the context of Serverless programming, in areas such as security, hosting, deployment, scalability, performance, and so on. We will focus on some of these in the upcoming chapters.

## See also
* To learn more about JSON schema, you can refer to https://json-schema.org/understanding-json-schema/index.html.