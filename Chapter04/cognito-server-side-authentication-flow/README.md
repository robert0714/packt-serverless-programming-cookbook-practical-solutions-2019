# Server-side authentication flow
Cognito supports different authentication flow use cases, such as server-side authentication flow, client-side authentication flow, and custom authentication flow. We will look at server-side authentication flow in this recipe. Server-side authentication is mostly used with SDKs, for server-side languages like Java, Node.js, and so on.

To work with a Cognito user pool, we also need an app client for the user pool. In this recipe, we will first define an app client that supports username and password authentication for admins. After that, we will execute admin-specific API commands from the CLI, in order to demonstrate the server-side authentication flow.

## Getting ready
The following are the prerequisites for completing this recipe:

A Cognito user pool that was created via the recipe, [Creating a Cognito user pool](../creating-a-cognito-user-pool/README.md). 

## How to do it...
We will first create the app client, and then, we'll execute the admin-specific API commands, to demonstrate the server-side authentication flow.

### Creating Cognito user pool client
We will now look at how to create a Cognito user pool client, using both AWS CLI commands and CloudFormation templates.

#### Creating a Cognito user pool client with AWS CLI
Use the `cognito-idp create-user-pool-client` sub-command to create a user pool client, as follows:
```bash
    aws cognito-idp create-user-pool-client \
        --user-pool-id ap-northeast-1_NbDfIkPxm \
        --client-name my-user-pool-client \
        --explicit-auth-flows ADMIN_NO_SRP_AUTH \
        --profile admin

{
    "UserPoolClient": {
        "UserPoolId": "ap-northeast-1_NbDfIkPxm",
        "ClientName": "my-user-pool-client",
        "ClientId": "bd726017pgl4utca13kinbbmh",
        "LastModifiedDate": "2024-11-22T16:07:44.464000+08:00",
        "CreationDate": "2024-11-22T16:07:44.464000+08:00",
        "RefreshTokenValidity": 30,
        "TokenValidityUnits": {},
        "ExplicitAuthFlows": [
            "ADMIN_NO_SRP_AUTH"
        ],
        "AllowedOAuthFlowsUserPoolClient": false,
        "EnableTokenRevocation": true,
        "EnablePropagateAdditionalUserContextData": false,
        "AuthSessionValidity": 3
    }
}        
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
            "Username": "testuser",
            "Attributes": [
                {
                    "Name": "sub",
                    "Value": "77641ad8-6071-707f-0f39-70b70939d731"
                }
            ],
            "UserCreateDate": "2024-11-22T16:10:17.724000+08:00",
            "UserLastModifiedDate": "2024-11-22T16:10:17.724000+08:00",
            "Enabled": true,
            "UserStatus": "FORCE_CHANGE_PASSWORD"
        }
    }
    ``` 
    Note that the default user status is `FORCE_CHANGE_PASSWORD`. The user will be provided with a NEW_PASSWORD_REQUIRED challenge after the first login.
2. Initiate the authentication flow as an admin, as follows:
    ```bash
    aws cognito-idp admin-initiate-auth \
        --user-pool-id ap-northeast-1_NbDfIkPxm \
        --client-id bd726017pgl4utca13kinbbmh \
        --auth-flow ADMIN_NO_SRP_AUTH \
        --auth-parameters USERNAME=testuser,PASSWORD=Passw0rd$ \
        --profile admin


    ```
    > Note that we have specified ADMIN_NO_SRP_AUTH. This call will fail if we do not configure this option within explicit auth flows during the client creation. Also, remember to replace the value for client-id with our client ID from the previous step.

    The `initiate auth` command will return a [NEW_PASSWORD_REQUIRED](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_InitiateAuth.html) challenge and a session ID:
    ```json
    {
        "ChallengeName": "NEW_PASSWORD_REQUIRED",
        "Session": "<session-id>",
        "ChallengeParameters": {
            "USER_ID_FOR_SRP": "testuser",
            "requiredAttributes": "[]",
            "userAttributes": "{}"
        }
    }
    ```
3. We will then send a response to the `auth` challenge, as follows:
    ```bash
    aws cognito-idp admin-respond-to-auth-challenge \
        --user-pool-id ap-northeast-1_NbDfIkPxm \
        --client-id bd726017pgl4utca13kinbbmh \
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
            "AccessToken": "eyJraWQiOiJvbjJMb2FxeGhhbTlSTUhGXC84R01cLytOQU9JdWt6SGI1RStRVnFoeHFLb3c9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI3NzY0MWFkOC02MDcxLTcwN2YtMGYzOS03MGI3MDkzOWQ3MzEiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAuYXAtbm9ydGhlYXN0LTEuYW1hem9uYXdzLmNvbVwvYXAtbm9ydGhlYXN0LTFfTmJEZklrUHhtIiwiY2xpZW50X2lkIjoiYmQ3MjYwMTdwZ2w0dXRjYTEza2luYmJtaCIsIm9yaWdpbl9qdGkiOiJmMmNiYzRhYi0yYzgwLTRmNTUtODQyMy04OWQ5ZjQ5ZjRkOGIiLCJldmVudF9pZCI6IjUxZmU3NWIyLTc2MTUtNGMzNC1iMTFlLTkyYmFmZDQzZTIyMCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG8uc2lnbmluLnVzZXIuYWRtaW4iLCJhdXRoX3RpbWUiOjE3MzIyNjMyMzQsImV4cCI6MTczMjI2NjgzNCwiaWF0IjoxNzMyMjYzMjM0LCJqdGkiOiIyZjMxYjNiZi0zNGU0LTRkNzQtYTZkOC04NTUzOTIxZTEyNmQiLCJ1c2VybmFtZSI6InRlc3R1c2VyIn0.fpAy-dqqdiD3IJWHMS4ijzbpiR2qekaWj7KTDTeCqI_2y-oIApFx-6rytTtVhpXfWqvI7TX47_nNg-QhYXNLwzxrR31wjN2NCsZod5z8LlLds3ifdM0gwwDYcvMAIHDxmYEnoDbidAbrIVXK6fhCYMn6Taci4bnZ2FMeMDu6QffqN1n_LKmD-8Tk7ZsiFksIy6KMuqjYAISYVL42QnffxDMNeIAulReNh3mLspIooRhd91iTrlbJiV1-jtOPZKfmLbc-ML4fIXle3mma4kXvI1NGLTjB_dgRw4Z288vfxXkplVZzZapREqSvsjuYAxVusOLg8U-5m2zktiOt9e86GQ",
            "ExpiresIn": 3600,
            "TokenType": "Bearer",
            "RefreshToken": "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ.PRyqUzY_Pl8XMyhG8FHgmLXQYOOW_LGp9F8JZHqh9MI4UCYrU9O2fFTJQCavGISrFsxK9uesmRbWnCVMeLWk8dB3juWvgad50FQPzn5xSQ015wKoH1-VyKlmRiFvR-I1BVBsNiWb8IhDG2hR51s05BzPsz4G0JSo5p3K-rdDxUJy5M4LdnoPcr_0aVQLDf97RQfcNIi2z6WKq00NgRhC6IaG1emNo2BDcYu1qhx5AIKLUUDeDiaopnCVPzjCIvTe711DjRy61nsN5GTJDZIgb1zfJaeT0y09b1r2ms-FTTZdXsFAq7nooAZ3-rPlceceIwIDoGZQbxRSR9idf1IOug.xK5VAzMSKPQoO9Qy.0VCd4Q_7v_i1i5u2-bei_UI5NutN4Ml3PBRhHWq98ItxAljAsaVHY8PZNifevN0lRADRA5OxQCrVQE2pLPmDmgFmxyJAiUATHMkbYrhLUCNy9rRio8pmvm1ecVSqUj1s8BEVt8YBdqhX3zboEipFoUPZoV1oF72tqaUbAypZhs1a2hFmLHM7Cwlrnr5LdP5rgKzMPI-N3n5sbUUxAcBvST8jctN2c9y3dSCsp-1pRLUqOR5ZFr9UYPB_SA3dU6XHQD_C-PE620Y6h98TUC6TmrV3x0i-KvhiNB1wFLEK5Eo8K26mnqOdHCahdYqorbZoWo9xyTV3PvP5XU0jQxjF7JBZSe2pengBGBvCo4AIH4K35LQstsNOj0EiOnxu9qSeSfh1iLq4QMOM7xr50cFlNQEKhRtwgLW5VMkIDBxpMo4IbfhdQqxQ9Tr6Oc2OM-MgX8SU2TokHcRVVsY-HyHHhDEENFdjZ-qCTohyWrEzTKT9fzgXELfIlGHIWuCV4uJhUjYRfkuyHc49E-5I1YeLIf937kNRIAwHFfR1Xixw43aBh-Z96klFMeIwbKcX3EMr-odCCT29qGlxFnL3JNPWQFryyjVLTXHvHC647EOHqDQX-TJdRVnKsPempaUmsg1NI5fw-VdVwKomevyC1_TsWzAkvBTmd-iOoc47BxJAhoS--smPEs_3TZ3R-5FGGTDDt4gs5Sc9cPG9AHvbzLPrdJdCg_1NHqikhdCYNFjy5_2_b3-UliNlHzwWVscu8GtKfue5WWjBFLbX2gNtjUfiEdacBpdDMTx3Z-s_mRePE8FVKiPlIkTFxJagy-W9jHkGeGi38aRqFixoLJcZrHUnIJqFRM4TiLzLsYJwCbQXDuw3UoScQBbMkzHQEt7PD-G8q078tX82LTNRCNvWTCKgyelsBXJ5oFLfohxUaYzSyeUaiIRh89-xEIES4jP7plzTgAI_V5RlSxXZ1fYS9EilxlnJLLydP9JYIicMRp_Px8QGuZNa4Qzcn-FrQCcQY16gAlWfjTm-ybeypb2G5QTIHn5iZJsSlsZr6uiePAE-SKVIKrN7kHps9DQNXTDGa3qOB_MhcvghuM4RNZwr4qzOivv9VxdfuLeEYgYm61XefO11zgyNmY6eShYqoZxcHMy1hZmo19MDpLsDIh0m2YX2mL6ZwxUou5h7KesLG-V1_VV7edyVFFXot_J7VkOO5WAFDYEa4E_go7_JZMME0f4lyFvAHg_uZ0CqTrlF6gCR1iHDfQfIHyi-DhNnR9BZCx4qvQ-dHOsU_nqTzQvbrsaOpSVP.-OrjU8lfIomBcRr5r6W0dA",
            "IdToken": "eyJraWQiOiJROHJoS3ZzSEVYSG5HaVVHVGQ2M3pnbE9IOE55QVYzdlRGUWdGdmhsc3ZRPSIsImFsZyI6IlJTMjU2In0.eyJvcmlnaW5fanRpIjoiZjJjYmM0YWItMmM4MC00ZjU1LTg0MjMtODlkOWY0OWY0ZDhiIiwic3ViIjoiNzc2NDFhZDgtNjA3MS03MDdmLTBmMzktNzBiNzA5MzlkNzMxIiwiYXVkIjoiYmQ3MjYwMTdwZ2w0dXRjYTEza2luYmJtaCIsImV2ZW50X2lkIjoiNTFmZTc1YjItNzYxNS00YzM0LWIxMWUtOTJiYWZkNDNlMjIwIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE3MzIyNjMyMzQsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5hcC1ub3J0aGVhc3QtMS5hbWF6b25hd3MuY29tXC9hcC1ub3J0aGVhc3QtMV9OYkRmSWtQeG0iLCJjb2duaXRvOnVzZXJuYW1lIjoidGVzdHVzZXIiLCJleHAiOjE3MzIyNjY4MzQsImlhdCI6MTczMjI2MzIzNCwianRpIjoiNjFkMWJmYWUtMWVjOC00NTljLTlmYWEtYTA2MzE4ZTAzMjUxIn0.Im0LyUC55hmovVd1Xe-ypeYVr162rWVZiKO9MnC3Gr3yDYXRyCbxKI6zp62rs6LzXcN114VpuSA-LKcaTCEipDDCu6pLyqrMTjB6fRg8ngfQuPAdGy-PW0CIii2fBCaInfdsMzAkR4PYzzPRD1OnqrJpkhhGk-mlVgx14qw_v7gIXpD5Rq7UCKTSR-xBHCLImdH0uyZc1RoWgoI1A38AGaef1HbMiUS3WFWL1F_KFIDqeQ-fWgLY4W0n6hDBOhXv9y6T-vaLE_ZeT_kzD1rGFmCht-Y1T1LB3q_ZwuMuZMFmhNml0FChyE7hxZmhkwO9amAbd_zBNMLkjU0zWjeHZg"
        }
    }
    ```
    We can try to run the `initiate auth` command with the new password; we will see that it does not ask for the password challenge. Instead, it returns the tokens.
4. From now on, we can use the refresh token to regenerate the access token and the ID token:
    ```bash
    aws cognito-idp admin-initiate-auth \
        --user-pool-id ap-northeast-1_NbDfIkPxm \
        --client-id bd726017pgl4utca13kinbbmh \
        --auth-flow REFRESH_TOKEN_AUTH \
        --auth-parameters REFRESH_TOKEN=<refresh-token> \
        --profile admin
    ```

5. To clean up, delete the user pool client, as follows:
    ```bash
    aws cognito-idp delete-user-pool-client \
        --user-pool-id ap-northeast-1_NbDfIkPxm \
        --client-id bd726017pgl4utca13kinbbmh \
        --profile admin
    ```
    Delete the user that we created for this recipe, as follows:
    ```bash
    aws cognito-idp admin-delete-user \
        --user-pool-id ap-northeast-1_NbDfIkPxm  \
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
