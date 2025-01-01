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
    or
    calculate SECRET_HASH     
     ```python
     import base64
     import hmac
     import hashlib
     
     def calculate_secret_hash(client_id, client_secret, username):
         message = username + client_id
         dig = hmac.new(
             client_secret.encode('utf-8'),
             msg=message.encode('utf-8'),
             digestmod=hashlib.sha256
         ).digest()
         return base64.b64encode(dig).decode()

     client_id = "45l9ureterrdqt0drbphk4q3pd"
     client_secret = "cse7iugt57ju3bfg9739ka1j4r36kpggtqmcbv47ola129v2ath"
     username = "testuser5"

     secret_hash = calculate_secret_hash(client_id, client_secret, username)
     print(secret_hash)	
     ```

    ```bash
    aws cognito-idp initiate-auth \
        --client-id 45l9ureterrdqt0drbphk4q3pd \
        --auth-flow USER_PASSWORD_AUTH \
        --auth-parameters '{"USERNAME":"testuser5","PASSWORD":"Passw0rd$","SECRET_HASH":"S+jqZWc4wAGGTzLN+dQ3DVp9LQ1ghIHydriwvgFkDoQ="}' 

     id_token=$(aws cognito-idp initiate-auth \
        --client-id 45l9ureterrdqt0drbphk4q3pd \
        --auth-flow USER_PASSWORD_AUTH \
        --auth-parameters '{"USERNAME":"testuser5","PASSWORD":"Passw0rd$","SECRET_HASH":"S+jqZWc4wAGGTzLN+dQ3DVp9LQ1ghIHydriwvgFkDoQ="}' \
       | jq ".AuthenticationResult.IdToken"  -r )
     echo $id_token
    ``` 
    If it is successful, this command will return the access token, `ID token`, and refresh token. 
    ```bash
     {
       "ChallengeParameters": {},
       "AuthenticationResult": {
          "AccessToken": "eyJraWQiOiJFQ3BSK1h3aGp3ditiMzJuVWgwTWIxT2RHYU5kS1FnQkJjN2hxT3labnY4PSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI2N2E0Y2E3OC00MGExLTcwNDYtMjA5Ny1mOGY5OTI4NTZjNjIiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAuYXAtbm9ydGhlYXN0LTEuYW1hem9uYXdzLmNvbVwvYXAtbm9ydGhlYXN0LTFfcjFjanNYUDU2IiwiY2xpZW50X2lkIjoiNWhuYmNlc2lybWxzaTBkMTh2dXJqcDF0YW8iLCJvcmlnaW5fanRpIjoiNzE1NTExZWUtNzAxZC00OTFiLTk4NjMtMGUwY2M2ZjRhMDI3IiwiZXZlbnRfaWQiOiI3ZjU5YTg2Yi1jMzM2LTRhOGMtOWIwMS05ODFmMzVmOTBhMDciLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6ImF3cy5jb2duaXRvLnNpZ25pbi51c2VyLmFkbWluIiwiYXV0aF90aW1lIjoxNzM1NjU4MDk3LCJleHAiOjE3MzU2NjE2OTcsImlhdCI6MTczNTY1ODA5NywianRpIjoiOWQzMDIwNGMtOTEwMC00NmQzLTkwMjMtNTk1MjA5YmQ5NDE1IiwidXNlcm5hbWUiOiI2N2E0Y2E3OC00MGExLTcwNDYtMjA5Ny1mOGY5OTI4NTZjNjIifQ.N9B01BdtXhgLxtGhKA7ap3wmtRkUbQ9g2DAc2QDxDssfZgv6MaJHQ3QcMjrpEzp5_LBfnoR-zwU0fcXB1AyLVQ8oDd52Nq37zHgpWrR5e_OM4XSGJxSzmXfKFFr68DaHXloy0-S7FjVeQOzpsCp_IYAJ0Ud1T1iqPrrumK9k3FWEO0zYjFJmMrzmEDbrKJspg44Gbtu6rQhbQE99LQWd8nyJBQjYBiHcqRT8UeTJLeH51sSJnx_dMB2CAfJF1EY0HBxCNVl4RaHg3fhNeLosNAI4P7n5VOm6Xy3hVVXTisIb8edemoWXT_qcyDLQ-hTnQ9-eQrlVtyBMN6GprXHPzg",
          "ExpiresIn": 3600,
          "TokenType": "Bearer",
          "RefreshToken": "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ.xPBBDeHnWVDKI0JEwIvFJjEtuX9oT9EMuByTn1weNkyyNZU2P5oNbUXXNyAzE9o-U0WonvTxGCHgnFON_VllDGLBg22-G75fmhz6StmIx8TytlkSWvTYwuhcoueXJoKiGECc_NeH1voVH-Lnmp1QjWQVqU5te8s1fosVc0lXoaOtMUou_nAPk_u0kVKZZ49hj4R7vEx_rorc0gbI6avXSTh33nN8znpR500YN6F3TA838LSuDzFsJotXampYCXS-6rF9hQsu_Bgu3ZidcMLN8hA9vePpyOYZwbF4_tZzaVHThPTPBf5REcLNEXpvWqkKMi9wDRliEe0ApIMgsTRquw.ml-h70nzUYEaUxc3.nFKUMSpHB9XfRRYSOhoEjOqrmGGGFb-uUaKicafzhkauFkRgHb5lC2R2lECYyMxY2zjFwnKZmxl6Sr2onu4NexCVQeI9E4XSx_ith0a0ftAfnkimBXn655J3DRWshwiXuuKpunbdDV56ov0PYkSQhkrzreP6MTHqBQx_A_kfcmBFqh4187mITHtrYvtbENnKFGTZcA5KzB9kx_2A5BHbvOy-lULGChCKvMyYbES_GY8QUJcAAQq7ofOq19Yyj_1RASpwu7AC7y9ga-xKiNr4xBviNe7TFRdMj-1RBtGAKCQygwOuIQjFLHoGil2Mb7OX0lOjMXjWOqJA-kiAFJYitL8gL7daYMlCPLesMmVEUwgBTicPUSaiRvg0CPDbGvFQkrPOOjOIb4NNpT1j6xOP7GnGbRxymCbBoFGQ5N-G_VUc8rWRKJzUfqujZqKNbVnAp9gpdNhjjQrdxHQ61u-ebk_TrF9gpej8tXBuov5FH0ZSqzwv406v9l2TmH_fhrHHuCRKGXOCLzIDGaqfLUXCEn2y21kC79JuH4ncQ6U-7xgD0nF2QfKht58YZc9x3HYzlF5aPmU73brq7VTFtxtyn9LK1LVjQsNE3PzHHQpGfrSiEkSn-HTxLd-HiDy5yH9xkJHj5n_P-F5mchlEROIIuGrMliBwqIyPmo-8LGGs6bI6bbR74ZyCK0uu24fjtSBlglRHZ5rlmjBm0rqUZHXNL3UoX4gHLpalochb2V5oMlfKPdEGZz62hDhHrp4OypPnIag1r3tF9DYAfal57V0giEOx0e9j4YE_9123O5CEbxTu3E38OFYHFpahDoRXYdygg6QyNLeyFMHpHdSb56DaQPHDDOrYJGfFJR72yE4jWKdpRvNeohlzLgEXSM1rmH4yTHKhUmHKoe78lbdEcUrrrcVDvZfA32Pb2jCs5Ei6g7GOeE14MCU_Mq1clBPzKN1DlanP7G3kjnGLhFDj9sPAzcPfWL1cZD-73Fy-j7RzFwYkS4X4vKhm4UEIv3VCgUjREHvRsQclv1BWm0w7U2Mma95kzXsZ4BPvn8P1qNHOVjg0wORh-znvl_jkYcDx6ktBcHAMb_ArXB3cqGJca8TVyFkeKII9ftQ7FmpLSUryYmBnYO8Pk4n0k7Fffx6lnnGCWwnTkU4HKIjA6CegCMNTVkwXZml8zljaY80Jezqipw21d13SgzJoEr8d-clnW4reXI-lDGW0odBydsg9-hYk2Tor913r-qELml3jpWJBa1edKtqE0rOYFFuze874LA4VDSYxWK9as_X7PcrMYFf5C7vGhzYvjYkwzDyp6FHFfWwYE9L4uA_nrPxYYiE1kswtsYfmB1YYMo41hA.NMg1s8ozfZQffGBDUmPi6g",
          "IdToken": "eyJraWQiOiJOWWJ6K3ZvMForMzg3REFPc2FtR3pqNDBvOExsYzRZakRTSE53aDl0bXRvPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI2N2E0Y2E3OC00MGExLTcwNDYtMjA5Ny1mOGY5OTI4NTZjNjIiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAuYXAtbm9ydGhlYXN0LTEuYW1hem9uYXdzLmNvbVwvYXAtbm9ydGhlYXN0LTFfcjFjanNYUDU2IiwiY29nbml0bzp1c2VybmFtZSI6IjY3YTRjYTc4LTQwYTEtNzA0Ni0yMDk3LWY4Zjk5Mjg1NmM2MiIsIm9yaWdpbl9qdGkiOiI3MTU1MTFlZS03MDFkLTQ5MWItOTg2My0wZTBjYzZmNGEwMjciLCJhdWQiOiI1aG5iY2VzaXJtbHNpMGQxOHZ1cmpwMXRhbyIsImV2ZW50X2lkIjoiN2Y1OWE4NmItYzMzNi00YThjLTliMDEtOTgxZjM1ZjkwYTA3IiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE3MzU2NTgwOTcsImV4cCI6MTczNTY2MTY5NywiaWF0IjoxNzM1NjU4MDk3LCJqdGkiOiI4MGU0MGQwMi1hMTdkLTQ4MzYtYWExOS0xYjY3NDlmYjQ3MjkiLCJlbWFpbCI6InJvYmVydC5sZWVAaWlzaWdyb3VwLmNvbSJ9.9LiSOwzbjJhCeaPqcGMZi33CIJn0I0qqVT-iSiSrSgNLB8rYmUpEbJqIqWBvgXKbmA3ep9ojSBuqT_UybulkomV1Du4mHlxEBSC8j8mLpyLi-zZ6iYdnnlmHTcNqM_lT-d0sdZDePnXvXrUJPR7T0q4H3cfwUbJclhNl10j12BRKPBL3o9UbcPzjCBJ74onwlOt_98o7cOUZijrDKhVL31lsy2T_sjqsxnfo98QIyCisjoWz4qemRSS7mTgFUQpO7Izxh-I0j1Tne4LVHfXXQg8I4hzKFpfkdPORWfBFeoyT4dC0QgdgAsRABmnojHi2sTUKjP9Dp9voGaetX-8rKg"
       }
    }

    ```
15. Finally, you can execute the URL by using a REST client, such as Postman. You need to select the authorization type as `Bearer Token` and copy the `ID token value` that you received in the `initiate-auth` request into the token field, as follows:

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
