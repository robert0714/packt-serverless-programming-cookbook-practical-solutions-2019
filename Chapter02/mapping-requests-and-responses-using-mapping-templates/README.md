# Mapping requests and responses with mapping templates
Amazon API Gateway allows us to map our incoming requests to a format that's required by our Lambda, and map the response from the Lambda into a format that's required by the client, all by using the mapping templates. The API Gateway body mapping templates are based on the Apache **Velocity Template Language (VTL)** and JSONPath expressions.

In this recipe, we will see how to map a JSON body coming from a request into another JSON structure, as required by the backend (Lambda). We will also map the JSON response from the backend (Lambda) into a different JSON response structure which is sent back as the response. We will use the same Lambda from the recipe *Building your first API with Lambda integration*, but the client will send the request in a different format.

## How to do it...
We will map the client request with the expected request format for the Lambda, and we will also map the Lambda response to the expected response format for the client.

### Mapping requests
The expected request structure for our Lambda, based on the request POJO, is as follows:
```json
{
 "name" : "Heartin",
 "time" : "Morning"
}
```
The request payload sent from the client via the POST request is as follows:
```json
{
 "user" : {
 "name" : "Heartin"
 },
 "greeting" : {
 "time" : "Morning"
 }
}
```
We can map the request payload to the expected JSON format by using the following mapping template:
```json
{
 "name" : $input.json('$.user.name'),
 "time" : $input.json('$.greeting.time')
}
```

### Mapping responses
The response from the Lambda is as follows:
```json
{
 "message" : "Good Morning, Heartin"
}
```
The expected response from the client is as follows:
```json
{
 "greeting" : "Good Morning, Heartin"
}
```
We can map the response returned from the Lambda into the expected response format by using the following mapping template:
```json
{
 "greeting" : $input.json('$.message'),
}
```

### Creating the API using CLI commands
First, let's create the REST API by using AWS CLI commands. We will not show how to use the commands that we already discussed in previous recipes. However, the complete commands will be available in the code files:
1. Create a REST API in API Gateway by using the `apigateway` sub-command `create-rest-api`.
2. Get the root resource (`/`) of our API by using the `apigateway` sub-command `get-resources`.
3. Create our `path-part`, `lambdagreeting`, by using the `apigateway` sub-command `create-resource`.
4. Create a `POST` method by using the `apigateway` sub-command `put-method`.
5. Set a response status code for our POST method by using the `apigateway` sub-command `put-method-response`.
6. Execute the `aws apigateway put-integration` command with the request mapping template, as follows:
   ```bash
   aws apigateway put-integration \
      --rest-api-id y3yftanqp7 \
      --resource-id e4w7ka \
      --http-method POST \
      --type AWS \
      --integration-http-method POST \
      --uri 'arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:<account_id>:function:lambda-for-api-gateway/invocations' \
      --request-templates '{ "application/json" : "{ \"name\" : $input.json('"'"'$.user.name'"'"'), \"time\" : $input.json('"'"'$.greeting.time'"'"')}" }' \
      --passthrough-behavior WHEN_NO_TEMPLATES \
      --region us-east-1 \
      --profile admin
   ```
   > Aside from the request-template property, we also set the value of passthrough-behavior with WHEN_NO_TEMPLATES. 
7. Execute the `aws apigateway put-integration-response` command with the response mapping template, as follows:
   ```bash
   aws apigateway put-integration-response \
      --rest-api-id y3yftanqp7 \
      --resource-id e4w7ka \
      --http-method POST \
      --status-code 200 \
      --region us-east-1 \
      --selection-pattern "" \
      --response-templates '{ "application/json" : "{ \"greeting\" : $input.json('"'"'$.message'"'"')}" }' \
      --profile admin
   ```
8. Deploy our API into a stage by using the `apigateway` sub-command `create-deployment`.
9. Give permission for the API method to invoke the Lambda function by using the command `aws lambda add-permission`.

### Creating the API with a CloudFormation template
Now, let's create the API by using a CloudFormation template. We will not discuss the components that were already discussed in previous recipes, nor will we discuss the theory for commands that were already discussed within the CLI commands section. The complete code is available in the code files.

With the CloudFormation template, the major change is within the `Integration` property of the resource type `AWS::ApiGateway::Method`, as follows:
```yaml
Integration:
  Type: AWS
  IntegrationHttpMethod: POST
  PassthroughBehavior: WHEN_NO_TEMPLATES
  RequestTemplates:
    application/json: "{ \"name\" : $input.json('$.user.name'), \"time\": $input.json('$.greeting.time') }"
  IntegrationResponses:
  - StatusCode: 200
    ResponseTemplates:
      application/json: "{ \"greeting\" : $input.json('$.message')}"
  Uri:
    !Sub
      - 'arn:aws:apigateway:${AWS::Region}:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:${AWS::AccountId}:function:${LAMBDA_NAME}/invocations'
      - LAMBDA_NAME: !ImportValue LambdaForApiGateway
```
> All of the other components (for example, template version, description, API, resource, deployment, adding permission, and output) remain the same, except for the descriptions and names (in a few places), which are changed to match the current recipe.

### Testing the API
You can test the API by using Postman (or any other REST/HTTP client), as follows:


The response should be the same as in the previous recipe, as shown in the following screenshot:

## How it works...
In this recipe, we used mapping templates with the variable `$input` that is provided by API gateway to transform the request JSON and response JSON. We also specified the recommended passthrough behavior of `WHEN_NO_TEMPLATES`, instead of the default `WHEN_NO_MATCH`. We discussed passthrough behavior in the previous recipe.
 

### Mapping templates and variables
You can map the requests and responses in API Gateway by using API Gateway's body mapping templates, based on the Apache VTL and JSONPath expressions. 

The `$input` variable provided by the API Gateway represents the input payload (a request payload or response payload, based on the case) and the parameters available to the template. The function `$input.json()` retrieves part of the JSON, as specified by a JSONPath expression. In JSONPath, $ represents the outer-level JSON object.  $input.json('$.user.name')  returns the value of the field name within the user object, and ``$input.json('$.greeting.time')`` returns the value of the field time within the greeting object. Similarly, ``$input.json('$.message')`` returns the value of the message field.

Aside from `$input`, API Gateway also provides `$context`, `$stageVariables`, and $util variables. The `$context` variable holds the contextual information, such as the `apiId`, `authorizer properties`, `principalId`, `httpMethod`, `error details`, `accountId`, `apiKey`, `cognitoAuthenticationProvider details`, `sourceIp`, `path`, `protocol`, `status`, `stage`, and so on. The variable `$stageVariables` can be used to refer to stage variables. The `$util` variable has utility functions that can be used in mapping templates, such as `escapeJavaScript()`, `parseJson()`, `urlEncode()`, `urlDecode()`, `base64Encode()`, and `base64Decode()`. 


## Using #set
You can use `#set` to define a variable that can be used within the template.

Our (original) template is as follows:
```json
{ 
 "name" : $input.json('$.user.name'), 
 "time": $input.json('$.greeting.time') 
}
```
The template, when rewritten by using #set, is as follows:
```json
#set($inputRoot = $input.path('$'))
{ 
 "name" : "$inputRoot.user.name", 
 "time": "$inputRoot.greeting.time"
}
```
> The `$inputRoot` is also the variable name that is autogenerated by API Gateway in the **AWS Management Console**. However, you are free to use any name.

## There's more...
We have used Apache Velocity Language and JSONPath expressions within our recipe. You can read the following sections to learn more about them, for advanced use cases.
### The Apache Velocity Language
Apache Velocity is a Java-based templating engine. It was developed for web designers, to get access to Java objects without knowing the Java programming language. Velocity is currently used as a templating engine for a variety of use cases, such as generating web pages, SQL, PostScript, and so on. API Gateway uses it for its mapping templates. You can learn more about the velocity language at http://velocity.apache.org. 

### JSONPath expressions
JSONPath expressions are used with a JSON object, similar to how XPath expressions are used with an XML document. $ represents the root-level object, and @ represents the current object. JSONPath expressions can use the dot notation or the square bracket notation. You can read more about JSONPath at http://goessner.net/articles/JsonPath. 

## See also
Read more about creating models and mapping templates at https://docs.aws.amazon.com/apigateway/latest/developerguide/models-mappings.html.

Variables available to use within API Gateway mapping template is available at https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-mapping-template-reference.html.