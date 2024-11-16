# Validating request payloads with models
In this recipe, we will add validation to request payloads by using models. We will define a model by using the JSON schema draft language, and we will then use it to validate our payloads. We will use the same Lambda that we used in the recipe Building your first API with Lambda integration. 


## How to do it...
We will define a request in the JSON format, and then define a model schema for JSON, using both the CLI commands and the CloudFormation templates.

### The request data format
We will use the same request format that we used in the previous recipe:
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
### Creating the API with CLI commands
First, let's create the REST API by using AWS CLI commands. We will not show how to use the commands that we already discussed in previous recipes. However, the complete commands will be available in the code files:
1. Create a REST API in API Gateway, using the `apigateway` sub-command `create-rest-api`.
2. Get the root resource (/) of our API, using the `apigateway` sub-command `get-resources`.
3. Create our `path-part`, `lambdagreeting`, using the `apigateway` sub-command `create-resource`.
4. Create a model schema for our JSON, using the `apigateway` sub-command `create-model`:
   ```bash
   aws apigateway create-model \
      --rest-api-id dqnqdyb3z2 \
      --name 'greetingRequestModel' \
      --description 'Greeting Request Model' \
      --content-type 'application/json'  \
      --schema '{
                     "$schema": "http://json-schema.org/draft-04/schema#",
                     "title": "greetingModel",
                     "type": "object",
                     "properties": {
                        "user" : {"type": "object",
                           "properties": {
                                 "name" : {"type" : "string"}
                           }
                        },
                        "greeting" : {"type": "object",
                           "properties" : {
                                 "time" : {"type" : "string"}
                           }
                        }
                     },
                     "required" : ["user", "greeting"]
               }' \
      --profile admin   
   ```
5. Create a request validator for our JSON, using the `apigateway` sub-command `create-request-validator`:
   ```bash
   aws apigateway create-request-validator \
      --rest-api-id dqnqdyb3z2 \
      --name greetingRequestValidator \
      --validate-request-body \
      --profile admin
   ```
6. Execute the `aws apigateway put-method` command with our request model and request validator IDs, as follows:
   ```bash
   aws apigateway put-method \
      --rest-api-id dqnqdyb3z2 \
      --resource-id ffknxp \
      --http-method POST \
      --authorization-type "NONE" \
      --request-models application/json=greetingRequestModel \
      --request-validator-id 549e4h \
      --region us-east-1 \
      --profile admin
   ```
7. Set a response status code for our `POST` method by using the apigateway sub-command `put-method-response`.
8. Set up the integration type, `request-template`, and the passthrough behavior with the `apigateway` sub-command `put-integration`.
9. Set up the response mapping, using the `apigateway` sub-command `put-integration-response`.
10. Deploy our API into a stage, `dev`, using the `apigateway` sub-command `create-deployment`.
11. Give permission for the API method to invoke the Lambda function, using the command `aws lambda add-permission`.

### The CloudFormation template
Now, let's create the API by using the CloudFormation template. We will not discuss the components that were already discussed in previous recipes, nor will we discuss the theory for commands that were already discussed within the CLI commands section. The complete code is available in the code files:
1. Start by defining the template version, `Description`, `RestApi`, and a `path-part lambdagreeting`.
2. Create a model for request validation, as follows:
   ```yaml
   MyRequestValidationModel:
   Type: AWS::ApiGateway::Model
   Properties:
      ContentType: application/json
      Description: Greeting Request Model
      Name: GreetingRequestModel
      RestApiId: !Ref MyRestAPI
      Schema: '{"$schema": "http://json-schema.org/draft-04/schema#",
               "title": "greetingModel",
               "type": "object",
               "properties": {
                  "user" : {"type": "object",
                     "properties": {
                     "name" : {"type" : "string"}
                     }
                  },
                  "greeting" : {"type": "object",
                     "properties" : {
                     "time" : {"type" : "string"}
                     }
                  }
               },
               "required" : ["user", "greeting"]
               }'
   ```
3. Create a request validator, as follows:
   ```yaml
   MyRequestValidator:
   Type: AWS::ApiGateway::RequestValidator
   Properties:
      Name: GreetingRequestValidator
      RestApiId: !Ref MyRestAPI
      ValidateRequestBody: true
      ValidateRequestParameters: false
   ```
4. Use the model and validator within the `AWS::ApiGateway::Method resource`, as follows:
   ```yaml
   MyMethod:
   Type: AWS::ApiGateway::Method
   Properties:
      AuthorizationType: NONE
      HttpMethod: POST
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
      RequestModels:
         application/json: !Ref MyRequestValidationModel
      RequestValidatorId: !Ref MyRequestValidator
      ResourceId: !Ref GreetingResource
      RestApiId: !Ref MyRestAPI
      MethodResponses:
      - StatusCode: 200
   ```
5. Add deployment, add Lambda permissions, and add an `Outputs` section, similar to the previous recipes.

#### Testing the API
You can test the API from Postman (or any other REST client of your choice) by passing the request in the expected format; you should get the expected response message. If you use the sample JSON method provided at the start of this section, you should get a response of `Good Morning, Heartin`.

If you send an invalid request (say, `{}`), you should get an error message with a response code of 400, as shown in the following screenshot:


## How it works...
In this recipe, we added a model based on the JSON schema for our input message format. To use the model to reject payloads that do not conform to our format, we did the following:
1. We specified all of the fields that are required (this is optional)
2. We created a validator and assigned it to this method

## There's more...
In our recipes, we created the template manually from AWS CLI. API Gateway Management Console also supports auto generating mappings, based on models. Once we define a model, we can create starter mappings from the integration request and integration response sections of the API gateway dashboard in the Management Console.

## See also
To learn more about JSON schema, you can refer to https://json-schema.org/understanding-json-schema/index.html.