# Integrating Cognito with the API gateway
In this recipe, we will integrate Cognito Authorizer with the API gateway, and we will get one step closer to our goal of building an end-to-end Serverless web application. After integrating Cognito Authorizer with the API gateway, we will test it by using the Postman REST client. 

## Getting ready
The following are the prerequisites for completing this recipe:
* Ensure that you have created a Cognito user pool, following the recipe [Creating a Cognito user pool](../creating-a-cognito-user-pool/README.md).
* Ensure that you have created a Cognito app client with a `USER_PASSWORD_AUTH` explicit flow declaration, following the recipe [Client-side authentication](../cognito-client-side-authentication-flow/README.md).
## How to do it...
I will list the API gateway CLI commands and provide the CloudFormation templates. However, I will not discuss much theory around the API gateway, as we already discussed that in Chapter 2, [Building Serverless REST APIs with API Gateway](../../Chapter02/README.md). If you are not familiar with API gateway, please refer to the recipes in Chapter 2, [Building Serverless REST APIs with API Gateway](../../Chapter02/README.md).

### The AWS CLI
We will cover the steps to create an API gateway API and integrate Cognito Authorizer with it, as follows:

1. Create an API gateway REST API, as follows:
    ```bash
    aws apigateway create-rest-api  \
        --name "API Gateway With Cognito" \
        --region us-east-1 \
        --profile admin
    ```
2. Call get-resources to get the root resource ID, as follows:
    ```bash
    aws apigateway get-resources \
        --rest-api-id 3t0t98ifdh \
        --region us-east-1  \
        --profile admin
    ```
3. Create a resource with the path greeting and the parent ID as the ID of the root resource:
    ```bash
    aws apigateway create-resource \
        --rest-api-id 3t0t98ifdh \
        --region us-east-1 \
        --parent-id ufgvoiu8yh \
        --path-part greeting \
        --profile admin
     ```
4. Create an authorizer for API gateway, of the type COGNITO_USER_POOLS, as follows:
    ```bash
    aws apigateway create-authorizer \
        --rest-api-id 3t0t98ifdh \
        --name First_Cognito_Custom_Authorizer \
        --type COGNITO_USER_POOLS \
        --provider-arns arn:aws:cognito-idp:us-east-1:<account id>:userpool/us-east-1_fYsb1Gyec \
        --identity-source method.request.header.Authorization \
        --profile admin
    ```
    Replace the user pool ID (`us-east-1_fYsb1Gyec`) with your user pool ID, and `account id` with your account ID.
    If this is successful, you should get the following response:
    ```json
    {
        "id": "dxr47i",
        "name": "First_Cognito_Custom_Authorizer",
        "type": "COGNITO_USER_POOLS",
        "providerARNs": [
            "arn:aws:cognito-idp:us-east-1:userpool/us-east-1_fYsb1Gyec"
        ],
        "authType": "cognito_user_pools",
        "identitySource": "method.request.header.Authorization"
    }
    ```

5. Execute the `put-method` sub-command, with the `authorization-type` as `COGNITO_USER_POOLS` and the `authorizer-id` received as the response to the `create-authorizer` command, as follows:
    ```bash
    aws apigateway put-method \
        --rest-api-id 3t0t98ifdh \
        --resource-id rebvv7 \
        --http-method GET \
        --authorization-type COGNITO_USER_POOLS \
        --authorizer-id dxr47i \
        --region us-east-1 \
        --profile admin
    ```
6. Execute the `put-method-response` sub-command:
    ```bash
    aws apigateway put-method-response \
        --rest-api-id  3t0t98ifdh \
        --resource-id  rebvv7 \
        --http-method  GET \
        --status-code  200 \
        --region  us-east-1 \
        --profile  admin
    ```
7. Execute the `put-integration` sub-command:
    ```bash
    aws apigateway put-integration \
        --rest-api-id 3t0t98ifdh \
        --resource-id rebvv7 \
        --http-method GET \
        --type MOCK \
        --integration-http-method GET \
        --request-templates '{"application/json": "{\"statusCode\": 200}" }' \
        --region us-east-1 \
        --profile admin
    ```
8. Execute the `put-integration-response` sub-command:
    ```bash
    aws apigateway put-integration-response \
        --rest-api-id 3t0t98ifdh  \
        --resource-id b0549c \
        --http-method GET \
        --status-code 200 \
        --selection-pattern "" \
        --response-templates '{"application/json": "{\"message\": \"Welcome $context.authorizer.claims.given_name\"}"}' \
        --region us-east-1 \
        --profile admin
    ```    
    We use `$context.authorizer.claims.given_name` to retrieve the user attribute `given_name` that was used when creating the user. The sub-commands `put-method`, `put-method-response`, put-integration, and `put-integration-response` are simplified into a single block within the CloudFormation template for creating the API. In any case, CloudFormation templates are the preferred way to provision resources in AWS programmatically. I have included the CLI commands for a better understanding of the CloudFormation templates. 

9. Create the `deployment`, as follows:
    ```bash
    aws apigateway create-deployment \
        --rest-api-id 3t0t98ifdh \
        --region us-east-1 \
        --stage-name dev \
        --stage-description "Dev stage" \
        --description "First deployment" \
        --profile admin
    ```    
    A sample URL for this deployment will look as follows: https://3t0t98ifdh.execute-api.us-east-1.amazonaws.com/dev/greeting

10. Create the user pool client, as follows:
    ```bash
    aws cognito-idp create-user-pool-client \
        --user-pool-id us-east-1_fYsb1Gyec \
        --client-name my-user-pool-client \
        --explicit-auth-flows USER_PASSWORD_AUTH \
        --profile admin
    ```    
11. Create a user `sign-up`, as follows:
    ```bash
    aws cognito-idp sign-up \
        --client-id 45l9ureterrdqt0drbphk4q3pd \
        --username testuser5 \
        --password Passw0rd$
        --user-attributes Name=given_name,Value=Heartin
    ```    
12. Confirm the user as an administrator, as follows:
    ```bash
    aws cognito-idp admin-confirm-sign-up \
        --user-pool-id us-east-1_fYsb1Gyec \
        --username testuser5 \
        --profile admin
    ```    
13. Do an `initiate-auth` API call with the `auth` flow as `USER_PASSWORD_AUTH`, to allow for simple authentication based on username and password:
    ```bash
    aws cognito-idp initiate-auth \
        --client-id 45l9ureterrdqt0drbphk4q3pd \
        --auth-flow USER_PASSWORD_AUTH \
        --auth-parameters USERNAME=testuser5,PASSWORD=Passw0rd$
    ```    
    If it is successful, this command will return the access token, ID token, and refresh token. 

14. Finally, you can execute the URL by using a REST client, such as Postman. You need to select the authorization type as `Bearer Token` and copy the ID token value that you received in the `initiate-auth` request into the token field, as follows:
    If it is successful, you should get the following results:

### The CloudFormation template
The template starts as usual, with a template version and a description:
```yaml
---
AWSTemplateFormatVersion: '2010-09-09'
Description: Building Cognito API with AWS CloudFormation
We will then create the RestApi resource, as follows:

Resources:
  MyFirstRestAPI:
    Type: AWS::ApiGateway::RestApi
    Properties:
      Name: Greeting API
      Description: API for greeting an user
      FailOnWarnings: true
```      
Next, we will create an authorizer of the type COGNITO_USER_POOLS:
```yaml
CustomCognitoAuthorizer:
  Type: AWS::ApiGateway::Authorizer
  Properties:
      Name: FirstCognitoAuthorizer
      RestApiId: !Ref MyFirstRestAPI
      Type: COGNITO_USER_POOLS
      ProviderARNs:
      - Fn::Sub:
        - arn:aws:cognito-idp:${AWS::Region}:${AWS::AccountId}:userpool/${UserPoolId}
        - UserPoolId: !ImportValue MyFirstUserPoolId
      IdentitySource: method.request.header.Authorization 
```      
The value for the `Name` property cannot contain spaces, unlike many other name properties. Also, note that we have imported the user pool stack from the first recipe of the chapter, to create the provider `ARN`.

The resource definition is similar to what you have seen before:
```yaml
GreetingResource:
  Type: AWS::ApiGateway::Resource
  Properties:
    RestApiId: !Ref MyFirstRestAPI
    ParentId: !GetAtt MyFirstRestAPI.RootResourceId
    PathPart: 'greeting'
```    
The method definition is also similar to what you have seen before, except that now, we specify the `AuthorizationType` as `COGNITO_USER_POOLS`, and reference the `AuthorizerId` from the `authorizer` resource that we defined previously:
```yaml
MyMockMethod:
  Type: AWS::ApiGateway::Method
  Properties:
    AuthorizationType: COGNITO_USER_POOLS
    AuthorizerId: !Ref CustomCognitoAuthorizer
    HttpMethod: GET
    Integration:
      Type: MOCK
      IntegrationHttpMethod: GET
      IntegrationResponses:
      - StatusCode: 200
        ResponseTemplates:
          application/json: "{\"message\": \"Welcome $context.authorizer.claims.given_name\" }"
      RequestTemplates:
        application/json: "{\"statusCode\": 200}"
    ResourceId: !Ref GreetingResource
    RestApiId: !Ref MyFirstRestAPI
    MethodResponses:
    - StatusCode: 200
```    
We use context.authorizer.claims.given_name to retrieve the user attribute given_name that we passed to the initiate auth API for retrieving the tokens. The ID token also contains this information, embedded inside of it. 

The Deployment type definition and Output section are similar to what you have seen before:
```yaml
  MyFirstDeployment:
    DependsOn: MyMockMethod
    Type: AWS::ApiGateway::Deployment
    Properties:
      Description: 'First Deployment'
      RestApiId: !Ref MyFirstRestAPI
      StageDescription:
        Description: 'Dev Stage'
      StageName: 'dev'

Output:
  SampleEndpoint:
    Description: 'Sample Endpoint'
    Value: !Sub
        - https://${API_ID}.execute-api.${AWS::Region}.amazonaws.com/dev/greeting
        - API_ID: !Ref MyFirstRestAPI
```        
Now, you need to run the following API CLI commands (from the previous section):
```bash
aws cognito-idp sign-up
aws cognito-idp admin-confirm-sign-up
aws cognito-idp initiate-auth
```
Finally, you can execute the URL by using a REST client, such as Postman. You need to select the authorization type as Bearer Token, and copy the ID token value that you received in the `initiate-auth` request into the Token field. Refer to the screenshots in the previous section for the CLI commands. 

## How it works...
In this recipe, we created an API gateway API and an authorizer of the type `COGNITO_USER_POOLS`, and integrated them together. The API gateway API CLI commands and the CloudFormation templates are similar to the ones that we discussed in Chapter 2, Building Serverless REST APIs with API Gateway; hence, we won't get into the related theory and concepts.

In addition to Integrating Cognito with API Gateway, we demonstrated the use of `context.authorizer.claims`, in order to retrieve additional user information from the ID token. The attributes that are used with `claims` need to be passed to the initiate auth API call that generates the ID token.

## Claim-based identity
**Claim-based identity** is an approach to authentication in which the access tokens contain the access key information required for authentication, as well as additional information attributes (claims) about the users. Such tokens are referred to as **identity tokens**.

Claim-based authentication allows a user to use a single token to sign-in to multiple websites, which is referred to as **single sign-on**. Since some information attributes (claims) are already a part of the token, the user does not have to enter them again after signing in to the application.

## There's more...
In this recipe, we returned the response by using mock integration. You can follow the recipes in Chapter 2, Building Serverless REST APIs with API Gateway, to do a Lambda integration instead of a mock integration. We will be building and hosting an application with end-to-end integration in the next chapter.

## See also
* https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-enable-cognito-user-pool.html
* https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims