# Server-side authentication flow
Cognito supports different authentication flow use cases, such as server-side authentication flow, client-side authentication flow, and custom authentication flow. We will look at server-side authentication flow in this recipe. Server-side authentication is mostly used with SDKs, for server-side languages like Java, Node.js, and so on.

To work with a Cognito user pool, we also need an app client for the user pool. In this recipe, we will first define an app client that supports username and password authentication for admins. After that, we will execute admin-specific API commands from the CLI, in order to demonstrate the server-side authentication flow.

## Getting ready
The following are the prerequisites for completing this recipe:

A Cognito user pool that was created via the recipe, *Creating a Cognito user pool*

## How to do it...
We will first create the app client, and then, we'll execute the admin-specific API commands, to demonstrate the server-side authentication flow.

### Creating Cognito user pool client
We will now look at how to create a Cognito user pool client, using both AWS CLI commands and CloudFormation templates.

#### Creating a Cognito user pool client with AWS CLI
Use the `cognito-idp create-user-pool-client` sub-command to create a user pool client, as follows:
```bash
    aws cognito-idp create-user-pool-client \
        --user-pool-id us-east-1_fYsb1Gyec \
        --client-name my-user-pool-client \
        --explicit-auth-flows ADMIN_NO_SRP_AUTH \
        --profile admin
```    
Here, I have specified `ADMIN_NO_SRP_AUTH` as an explicit auth flow. This will allow us to pass our username and password without SRP. Other options that are allowed include `CUSTOM_AUTH_FLOW_ONLY` and `USER_PASSWORD_AUTH`. A few other authentication flows, including `USER_SRP_AUTH` and `REFRESH_TOKEN_AUTH`, are supported by default. We will see `REFRESH_TOKEN_AUTH` within this recipe, and `USER_SRP_AUTH` within a different recipe.


#### Creating a Cognito user pool client with CloudFormation template
With AWS CLI commands we had to hardcode the user pool ID, however with CloudFormation template we will reference it from the user pool CloudFormation template from the previous recipe.

We may add a template format version and a description similar to what we did in previous recipes.

The `Resources` and `Outputs` sections should look as follows:
```yaml
Resources:
  MyUserPoolClient:
    Type: AWS::Cognito::UserPoolClient
    Properties:
      ClientName: 'My Cognito User Pool Client'
      ExplicitAuthFlows:
      - ADMIN_NO_SRP_AUTH
      RefreshTokenValidity: 30
      UserPoolId: !ImportValue MyFirstUserPoolId
Outputs:
  ClientId:
    Description: 'Cognito user pool Client'
    Value: !Ref MyUserPoolClient
```    
Create the CloudFormation stack by executing the `aws cloudformation create-stack` command. 


### Server-side authentication flow
The server-side authentication flow is used with admin APIs, as follows:
1. Use admin APIs to create a user, as follows:
    ```bash
    aws cognito-idp admin-create-user \
        --user-pool-id us-east-1_fYsb1Gyec \
        --username testuser \
        --temporary-password Passw0rd$ \
        --profile admin
    ```     
    Remember to replace the user-pool-id value with your user pool id. If it is successful, this command will provide the following [output](https://docs.aws.amazon.com/cli/latest/reference/cognito-idp/admin-create-user.html#examples):
    ```json
    {
        "User": {
            "Username": "diego",
            "Attributes": [
                {
                    "Name": "sub",
                    "Value": "7325c1de-b05b-4f84-b321-9adc6e61f4a2"
                },
                {
                    "Name": "phone_number",
                    "Value": "+15555551212"
                },
                {
                    "Name": "email",
                    "Value": "diego@example.com"
                }
            ],
            "UserCreateDate": 1548099495.428,
            "UserLastModifiedDate": 1548099495.428,
            "Enabled": true,
            "UserStatus": "FORCE_CHANGE_PASSWORD"
        }
    }
    ``` 
    Note that the default user status is `FORCE_CHANGE_PASSWORD`. The user will be provided with a NEW_PASSWORD_REQUIRED challenge after the first login.
2. Initiate the authentication flow as an admin, as follows:
    ```bash
    aws cognito-idp admin-initiate-auth \
        --user-pool-id us-east-1_fYsb1Gyec \
        --client-id 4o1kgtd4sj39nr36ouak5mhblt \
        --auth-flow ADMIN_NO_SRP_AUTH \
        --auth-parameters USERNAME=testuser,PASSWORD=Passw0rd$ \
        --profile admin
    ```
    > Note that we have specified ADMIN_NO_SRP_AUTH. This call will fail if we do not configure this option within explicit auth flows during the client creation. Also, remember to replace the value for client-id with our client ID from the previous step.

    The `initiate auth` command will return a [NEW_PASSWORD_REQUIRED](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_InitiateAuth.html) challenge and a session ID:
    ```json
    {
        "ChallengeName": "NEW_PASSWORD_REQUIRED",
        "ChallengeParameters": {
            "USER_ID_FOR_SRP": "testuser",
            "requiredAttributes": "[]",
            "userAttributes": "{}"
        },
        "Session": "AYABeC1-y8qooiuysEv0uM4wAqQAHQABAAdTZXJ2aWNlABBDb2duaXRvVXNlclBvb2xzAAEAB2F3cy1rbXMAS2Fybjphd3M6a21zOnVzLXd..."
    }
    ```
3. We will then send a response to the `auth` challenge, as follows:
    ```bash
    aws cognito-idp admin-respond-to-auth-challenge \
        --user-pool-id us-east-1_fYsb1Gyec \
        --client-id 5hh4v7nveu22vea74h8stt9238 \
        --challenge-name NEW_PASSWORD_REQUIRED \
        --challenge-responses USERNAME=testuser,NEW_PASSWORD=NewPass0123$ \
        --session <session-id> \
        --profile admin
    ```
    If it is successful, this command will return a response with three tokens (an access token, a refresh token, and an ID token):
    ```json
    {
    "ChallengeParameters": {},
    "AuthenticationResult": {
        "AccessToken": "eyJraWQiOiI0VTY1ZHNqWlgyRHZoUDNwVFhnaTVkNU4zNkhmUkd00FFVZHFvd1pmTXVnPSIsImFsZyIjA2MGMyMGRkLWVjZjktMTFl0C040Tg1LWNkMzIwOGIxODA5NSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ2aXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX2ZZc2IxR3llYyIsImV4CCI6MTU0MjCONTA5MiwiaWF0IjOiI1aGg0djdudmV1MjJ2ZWE3NGg4c3R00TIzOCIsInVzZXJuYW1lIjoidGVzdHVzZXIifQ.Eg3Z1kAJyJ-NEXmWmbavMV325_Uh-UTecuFeXtYLKHjc_rD7gj8vp50NfDQN5m_u8fP8Q8JRFTsLPaGR6C3qX6u0F_HR6BN_YWpHYtBudsShz2qGlryxcvqSzfpzbws8rMli5xZNIxmwna0c0CycbED8buKQ49Mj_g",
        "ExpiresIn": 3600,
        "TokenType": "Bearer",
        "RefreshToken": "eyJjdHki0iJKV1QiLCJlbmMi0iJBMjU2RONNIiwiYWxnIjoiUlSBLU9BRVAifQ.BnFlQ7rap5v7g4aapMvMJPUejJIwTkqnPpAYjRNJsECnIlKgVYa8gmfkVLmPaGT8p2NmuIicHDZoPhY60VwAHrtVBWzq9xXX4g-k4PKIerEaMK4vZGMcLlMHcUNex7usnE0xvu0ryXrSniWAi3Sq940xjfEFgpfM2g.4WtMGmzXd8KEzU1P.g7zcSVH6RXguLWAeTa0ALcJghunwYB7Z5gSAEfbdCTXsWaAfddTlNyGweh0e6S34q4t4egQtgTZWjcUdBuCRkvcCUU_V3YC38SxENfNmxw9AzVfRg7PQKM4M5Pt2vU-CZx8Hklat31fojErd-3YBOLzgYIq8_0qMNhVWoeJCA3AjsB0vQ2R7z_qyaTXqbQBBpR0QfjaQD0psT404xRJ_Blqxs_PEm2Ego7mXsjo6SoILgVRX5q gZ0KjqXci91M-65MJB7HbLOUXYouOYPLazE_J3P0npEEAUUc9hx2RCHbfh8EUyPFiHv890mNukhcuyfNlh5N8EPLyy5Gmxf8MGgfe0jqPtnA4J5f380vD5mXF2Dx_iF1-1MNdcZnQzUG-1Z0yG9rTchnrPIk6JLMCXhUQFu9791plCSKRf1oLiZTSg0e0PB_h2lGTJaU2ULEklYj6qpxKaryd-ysY7C1YDPf-ee_w0-MN5maUjwXuzpKrboiwEBsjfrGSnwd4M58GHHQtqUZMUbVQn6hoVElyYNvhgXdXByuVTxGKmHdmBu28hbuhYt7Y1h409AqhBWAhqUFez2BqBeGYT_tsv3FELK1-s7qPrNvkwLQYPaXDooLgKNVMqjnVbpIsbLU4DW4nAHLWNx9d165saUwcaMUuw.Wa_lkFBRerl1zeoKjE32XA",
        "IdToken": "eyJraWQi0iJlVWh6bWYzR28wNDcrVW01b3dybDdReHZuamdvYjFlbk9ZV3NnV1FvZEc0PSIsImFsZyI6I1dmV1MjJ2ZWE3NGg4c3R00TIzOCIsImV2ZW50X2lkIjoiMDYwYzIwZGQtZWNm0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMj"
       }
    }
    ```
    We can try to run the `initiate auth` command with the new password; we will see that it does not ask for the password challenge. Instead, it returns the tokens.
4. From now on, we can use the refresh token to regenerate the access token and the ID token:
    ```bash
    aws cognito-idp admin-initiate-auth \
        --user-pool-id us-east-1_fYsb1Gyec \
        --client-id 5hh4v7nveu22vea74h8stt9238 \
        --auth-flow REFRESH_TOKEN_AUTH \
        --auth-parameters REFRESH_TOKEN=<refresh-token> \
        --profile admin
    ```

5. To clean up, delete the user pool client, as follows:
    ```bash
    aws cognito-idp delete-user-pool-client \
        --user-pool-id us-east-1_fYsb1Gyec \
        --client-id 5hh4v7nveu22vea74h8stt9238 \
        --profile admin
    ```
    Delete the user that we created for this recipe, as follows:
    ```bash
    aws cognito-idp admin-delete-user \
    --user-pool-id us-east-1_fYsb1Gyec \
    --username testuser \
    --profile admin
    ``` 
## How it works...
To summarize, we did the following in this recipe:
1. Created a user
2. Initiated an authentication flow as an admin
3. Responded to password challenges from Cognito
4. Used the refresh token to regenerate the access token and the ID token    

### Server-side, client-side, and custom authentication flows
Server-side authentication is mostly used with SDKs, for server-side languages such as Java, Node.js, Ruby, and so on. Server-side authentication uses admin APIs, and can also be referred to as `admin authentication flow`. 

Client-side SDKs, such as iOS, Android, and JavaScript, use client-side authentication flow.

Custom authentication flow uses custom Lambdas that get triggered during various life cycle stages of the authentication. 

### Secure Remote Password protocol
The **Secure Remote Password (SRP)** protocol tries to protect the password from being sent insecurely over the network, through alternate means like clients letting the server know that they have the correct password, without actually sending it over the network.

SRP is currently only supported in client-side SDKs for iOS, Android, and JavaScript. Backend SDKs, such as the ones for Java, Node.js, Ruby, and others, do not support SRP. Server-side authentication flows usually happen on secure backend servers; hence, SRP protocol calculations may not be required. 

### The access token, refresh token, and ID token
An **identity token (ID token)** is used to authenticate requests to the backend (for example, the API gateway). For example, to send a request to an API gateway API with Cognito Authorizer, we use the authorization type **Bearer Token** and pass the ID token. This will be demonstrated later, in the recipe on `Integrating Cognito with the API gateway`. The ID token will also contain additional information, such as the user ID and any other user attributes that we provide while generating it. We will demonstrate this in a later recipe.

The access token is used within Cognito APIs, in order to authorize updates to the users' parameters. The Cognito API commands that accept access tokens include `associate-software-token`, `change-password`, `confirm-device`, `delete-user`, `delete-user-attributes`, `forget-device`, `get-device`, `get-user`, `get-user-attribute-verification-code`, `global-sign-out`, `list-devices`, `set-user-mfa-preference`, `set-user-settings`, `update-device-status`, `update-user-attributes`, `verify-software-token`, and `verify-user-attribute`. 

The **refresh token** is used to get new identity and access tokens. For example, the `initiate auth` sub-command can specify the auth flow as `REFRESH_TOKEN_AUTH`, and can pass a refresh token to get back the access token and the ID token. We can configure the refresh token expiration (in days) when creating the user pool.

### ADMIN_NO_SRP_AUTH versus USER_PASSWORD_AUTH
Cognito authentication APIs support various authentication flow types, including `ADMIN_NO_SRP_AUTH` and `USER_PASSWORD_AUTH`. Both `ADMIN_NO_SRP_AUTH` and `USER_PASSWORD_AUTH` support sending the username and the password from the client to the IDP, without SRP protocol. 

`USER_PASSWORD_AUTH` also supports user migration from a legacy application, without actually requiring them to reset their passwords. However, AWS documentation suggests that we should update our auth flow type to a more secure once (for example, using SRP) after the migration is complete.

`ADMIN_NO_SRP_AUTH` is only supported for server-side authentication using `admin-initiate-auth` and `admin-respond-to-auth-challenge`, and is not supported for client-side authentication using `initiate-auth` and `respond-to-auth-challenge`.

## There's more...
In this recipe, we saw server-side authentication. There are other authentication flow use cases, including server-side authentication flow, client-side authentication flow, and custom authentication flow. We will look at some of these in later recipes. 

In the real world, the admin APIs that we used for authentication in this recipe are mostly used along with SDKs, for server-side languages like Java, Node.js, and so on. We can refer to the respective SDK documentation and follow the API usages in this recipe to implement them using the SDK.

## See also
* https://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-user-pools-authentication-flow.html
