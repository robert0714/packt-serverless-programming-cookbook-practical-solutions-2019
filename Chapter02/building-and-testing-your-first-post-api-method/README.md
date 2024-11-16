# Building and testing your first POST API method
In this recipe, we will create a simple POST API method that has an AWS integration with Lambda. A REST client will send a JSON request body to the API method in the format required by our Lambda, which will be passed through to the Lambda. We will use the same Lambda from the previous recipe. 

GET is the default HTTP method for web browser requests; hence, we could invoke GET API methods from the browser in the previous recipes. To invoke other HTTP methods or override various HTTP options, we need to use a REST client. We will use the Postman REST client, which is available as a standalone app, as well as an extension to the Chrome browser. 

## How to do it...
As with the previous recipes, we will first look at how to create the API using CLI commands, and we will then look at how to use CloudFormation templates. We will also cover testing it with a REST client.

### Creating the API with CLI commands
First, let's create the REST API by using AWS CLI commands. We will not show how to use the commands that we already discussed in previous recipes. However, the complete commands will be available with the code files:

1. Use the `aws apigateway` command to create a REST API in API Gateway, using the `apigateway` sub-command `create-rest-api`.
2. Get the root resource (`/`) of our API by using the `apigateway` sub-command `get-resources`.
3. Create our `path-part`, `lambdagreeting`, by using the `apigateway` sub-command `create-resource`.
4. Execute the `aws apigateway put-method` command with the `http-method` as `POST`, as follows:
   ```bash
   aws apigateway put-method \
       --rest-api-id 7uwav24q1f \
       --resource-id s6rij6 \
       --http-method POST \
       --authorization-type "NONE" \
       --region us-east-1 \
       --profile admin
   ``` 
5. Execute `aws apigateway put-method-response` with the `status-code` as `200` for the `http-method POST`, as follows:
   ```bash
    aws apigateway put-method-response \
        --rest-api-id 7uwav24q1f \
        --resource-id s6rij6 \
        --http-method POST \
        --status-code 200 \
        --region us-east-1 \
        --profile admin
   ``` 
6. Execute the `aws apigateway put-integration` command with the `http-method` as `POST`, the type as `AWS`, and a Lambda URI, as per the required format:
   ```bash
    aws apigateway put-integration \
        --rest-api-id 7uwav24q1f \
        --resource-id s6rij6 \
        --http-method POST \
        --type AWS \
        --integration-http-method POST \
        --uri 'arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:<account_id>:function:lambda-for-api-gateway/invocations' \
        --region us-east-1 \
        --profile admin
   ``` 
   > We have omitted the `request-templates` property, and we are now passing parameters within the body of our `POST` request.    

   The default pass through behavior is to pass the request body to the Lambda as is (if no matching templates are defined), as you can see in the response of `put-integration`, as follows:
   ```json
    {
    "type": "AWS",
    "httpMethod": "POST",
    "uri": "arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:□□□□□□□□□□□□:function:lambda-for-api-gateway/invocations",
    "passthroughBehavior": "WHEN_NO_MATCH",
    "timeoutInMillis": 29000,
    "cacheNamespace": "s6rij6",
    "cacheKeyParameters": []
    }
   ``` 
7. Execute the `aws apigateway put-integration-respons`e command for the `http-method POST`, with a `selection-pattern` of "":
   ```bash
    aws apigateway put-integration-response \
        --rest-api-id 7uwav24q1f \
        --resource-id s6rij6 \
        --http-method POST \
        --status-code 200 \
        --region us-east-1 \
        --selection-pattern "" \
        --profile admin
   ``` 
8. Deploy our API to a stage, `dev`, using the `apigateway` sub-command `create-deployment`.
9. Give permission for the API to invoke the Lambda, as follows: 
   ```bash
    aws lambda add-permission \
        --function-name lambda-for-api-gateway \
        --statement-id apigateway-st-2 \
        --action lambda:InvokeFunction \
        --principal apigateway.amazonaws.com \
        --source-arn "arn:aws:execute-api:us-east-1:<account_id>:7uwav24q1f/*/POST/lambdagreeting" \
        --profile admin
   ``` 

### Creating the API with a CloudFormation template
Now, let's create the API by using a CloudFormation template. We will not discuss the components that were already discussed in previous recipes, nor will we discuss the theory behind commands that were already discussed within the CLI commands section. The complete code is available in the code files:
1. Start to create the template by defining `AWSTemplateFormatVersion` and a suitable `Description`.
2. Create the REST API by using `AWS::ApiGateway::RestApi`.
3. Create the `path-part`, `lambdagreeting`, by using `AWS::ApiGateway::Resource`.
4. Define the method, with the `http-method` as `POST`, as follows:
   ```yaml
    MyMethod:
    Type: AWS::ApiGateway::Method
    Properties:
        AuthorizationType: NONE
        HttpMethod: POST
        Integration:
        Type: AWS
        IntegrationHttpMethod: POST
        IntegrationResponses:
        - StatusCode: 200
        Uri:
            !Sub
            - 'arn:aws:apigateway:${AWS::Region}:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:${AWS::AccountId}:function:${LAMBDA_NAME}/invocations'
            - LAMBDA_NAME: !ImportValue LambdaForApiGateway

        ResourceId: !Ref GreetingResource
        RestApiId: !Ref MyRestAPI
        MethodResponses:
        - StatusCode: 200
   ```
   > We have omitted the request-templates property, and we are now passing parameters within the body of our POST message.
5. Deploy our API to a stage, `dev`, by using `AWS::ApiGateway::Deployment`.
6. Add permission for the API to invoke the Lambda, as follows:
   ```yaml
    LambdaInvokePermission:
    Type: AWS::Lambda::Permission
    Properties:
        FunctionName: !ImportValue LambdaForApiGateway
        Action: 'lambda:InvokeFunction'
        Principal: apigateway.amazonaws.com
        SourceArn: !Sub
        - arn:aws:execute-api:${AWS::Region}:${AWS::AccountId}:${API_ID}/*/POST/lambdagreeting
        - API_ID: !Ref MyRestAPI
   ```
7. Add an `Outputs` section with the updated URI, as follows:
   ```yaml
    Outputs:
    SampleEndpoint:
        Description: 'POST Endpoint'
        Value: !Sub
            - https://${API_ID}.execute-api.${AWS::Region}.amazonaws.com/dev/lambdagreeting
            - API_ID: !Ref MyRestAPI
   ```
### Testing with Postman 
We will now look at how to test our API by using the Postman client. You can also use any other REST client that you are comfortable with:
1. Search for `Postman Chrome extension`, and follow the search results to install the Postman extension in Chrome.
> Postman is also available as a native app, to download, install, and use. It will also be the preferred way to use the Postman client, from this point forward.
2. Once Postman has been added as an extension in Chrome, you can launch it from `chrome://apps/`. You can log in to your Google account, or skip logging in.
3. Select the Request option. You can specify a folder to save the request in, or close the Save dialog box.
4. Configure Postman to send requests to our API method, and click on Send:
   1. Select the POST method.
   2. Add our endpoint URL for the POST method.
   3. Go to the Body tab, click on raw, and select JSON (application/json) for the content type.
   4. Add our JSON payload, as follows:


## How it works...
In this recipe, we created a POST API method whose request body is passed to the Lambda as is. To use the `POST` method, we used the `POST` HTTP method. To allow pass-through behavior, we did not use any `request-templates` property, and made use of the default passthrough behavior.    

## Passthrough behavior
In the `put-integration` response, you saw that the `passthroughBehavior` had the value `WHEN_NO_MATCH`, which means that, if we do not define a template for the request content type, API Gateway will `passthrough` the request body to the Lambda. 

We can override the passthrough behavior with the `passthrough-behavior` parameter in the CLI, or with the `PassthroughBehavior` property within the CloudFormation template. The valid values for this parameter are as follows:
* `WHEN_NO_MATCH`: This option allows the pass through of the request body for unmapped content types to the backend.
* `WHEN_NO_TEMPLATES`: This option allows pass through only if the templates are not defined for any content type. If a template is defined for at least one content type, the others will be rejected with a `415` HTTP response status code. The HTTP `415` response status code stands for `Unsupported Media Type`. This is the recommended option.
* `NEVER`: This option rejects all unmapped content types with an HTTP `415` response code.

## There's more...
Having a decent understanding of all of the common HTTP headers used with HTTP requests and HTTP responses can help you to design good REST APIs. To see the actual HTTP requests and responses for our `POST` method invocation, you can use a such as like Fiddler (www.telerik.com/fiddler) or Wireshark (www.wireshark.org). 


## See also
You can read more about using Fiddler and Wireshark for monitoring HTTP requests on their respective websites:
* https://www.telerik.com/fiddler
* https://www.wireshark.org