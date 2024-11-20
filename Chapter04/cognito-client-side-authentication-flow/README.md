# Client-side authentication flow
In the previous recipe, we demonstrated the use of server-side authentication flow, along with the authentication flow type `ADMIN_NO_SRP_AUTH`. In this recipe, we will demonstrate the use of client-side authentication flow, with the authentication flow type `USER_PASSWORD_AUTH`.

## Getting ready
The following are the prerequisites for completing this recipe:

* Make sure that we have created a Cognito user pool, following the recipe `Creating a Cognito user pool`. 
## How to do it...
First, we will create the client, and then, we will execute various client-specific API commands, to demonstrate the client-side authentication flow.
### Creating a Cognito user pool client
We will create a user pool client for client-side authentication flow both using AWS CLI. We will also see the template components to use within a CloudFormation template.

#### Creating a Cognito user pool client with AWS CLI
We use the `cognito-idp create-user-pool-client` sub-command to create a user pool client, as follows:
```bash
aws cognito-idp create-user-pool-client \
    --user-pool-id us-east-1_fYsb1Gyec \
    --client-name my-user-pool-client \
    --explicit-auth-flows USER_PASSWORD_AUTH \
    --profile admin
```
Here, I have specified `USER_PASSWORD_AUTH` as an explicit `auth` flow.
> `ADMIN_NO_SRP_AUTH` is only supported with the admin-initiated authentication used in the server-side authentication flow. Both `ADMIN_NO_SRP_AUTH` and `USER_PASSWORD_AUTH` allow us to pass our username and password without SRP, but `USER_PASSWORD_AUTH` also supports user migration from legacy applications. 


#### Creating a Cognito user pool client with CloudFormation template
We will follow the AWS CLI command option to create the corresponding CloudFormation template, in order to create the app client.

The `Resources` and `Outputs` sections should look as follows:
```yaml
Resources:
  MyUserPoolClient:
    Type: AWS::Cognito::UserPoolClient
    Properties:
      ClientName: 'My Cognito User Pool Client'
      ExplicitAuthFlows:
      -USER_PASSWORD_AUTH
      RefreshTokenValidity: 30
      UserPoolId: !ImportValue MyFirstUserPoolId
Outputs:
  ClientId:
    Description: 'Cognito user pool Client'
    Value: !Ref MyUserPoolClient
```    
We can also add a template format version and a description.

We can now create the CloudFormation stack with the user pool client, by executing the `aws cloudformation create-stack` command. 

### Client-side authentication flow
As we discussed previously, the client-side authentication flow uses non-admin APIs.

> The output for most of the commands will be similar to the ones that we discussed in the recipe Server-side authentication flow, and need not be repeated here. Please refer to that recipe for the screenshots. 

Follow the steps to demonstrate the client-side authentication flow. Remember to replace the `user-pool-id` value with your user pool id.

1. For creating the user, we will still use the admin APIs, as follows:
   ```bash
    aws cognito-idp admin-create-user \
        --user-pool-id us-east-1_fYsb1Gyec \
        --username testuser2 \
        --temporary-password Passw0rd$ \
        --profile admin
   ```
   The default user status will be `FORCE_CHANGE_PASSWORD`, as we saw in the previous recipe.
2. Initiate the authentication flow, as follows:
    ```bash
    aws cognito-idp initiate-auth \
        --client-id 3jiv1fi1rspotsst9m19hktu58 \
        --auth-flow USER_PASSWORD_AUTH \
        --auth-parameters USERNAME=testuser2,PASSWORD=Passw0rd$
    ```
    > As this is a non-admin API, we do not have to specify the admin profile from the command line. The initiate auth command will return a NEW_PASSWORD_REQUIRED challenge and a session ID.
3. Send a response to the auth challenge, as follows:
    ```bash
    aws cognito-idp respond-to-auth-challenge \
        --client-id 3jiv1fi1rspotsst9m19hktu58 \
        --challenge-name NEW_PASSWORD_REQUIRED \
        --challenge-responses USERNAME=testuser2,NEW_PASSWORD=NewPass0123$ \
        --session <session-id>
    ```
    If it is successful, this command will return a response with three tokens: an access token, a refresh token, and an ID token. We can try to run the `initiate auth` command with the new password, and check that it does not ask for the password challenge.
4. From now on, we can also use the refresh token to regenerate the access token and the ID token:
    ```bash
    aws cognito-idp initiate-auth \
        --client-id 3jiv1fi1rspotsst9m19hktu58 \
        --auth-flow REFRESH_TOKEN_AUTH \
        --auth-parameters REFRESH_TOKEN=<refresh token>
    ```
5. To clean up, delete the user pool client, as follows:
    ```bash
    aws cognito-idp delete-user-pool-client \
        --user-pool-id us-east-1_fYsb1Gyec \
        --client-id 3jiv1fi1rspotsst9m19hktu58 \
        --profile admin
    ```
    Delete the user that we created for this recipe, as follows:
    ```bash
    aws cognito-idp admin-delete-user \
        --user-pool-id us-east-1_fYsb1Gyec \
        --username testuser2 \
        --profile admin
    ```     
## How it works...
To summarize, we did the following in this recipe:
1. Created a user
2. Initiated authentication flow as a user
3. Responded to password challenges from Cognito
4. Used the refresh token to regenerate the access token and the ID token

The major differences, as compared to the server-side authentication flow API usage, are as follows:
1. Unlike with the server-side authentication APIs, we did not specify an admin profile while executing the CLI commands.
2. You do not have to specify the user pool ID with client-side authentication flow API calls; only the client ID needs to be specified.
> In real-world applications, you generally choose client-side authentication if you are working with SDKs for client-side platforms (for example, iOS, Android, or JavaScript), and server-side authentication flows if you are working with SDKs for server-side language platforms (for example, Java and Node.js).     

## There's more...
In this recipe, we used a simple authentication flow type, based on a username and password. However, you can also utilize the additional security of Secure Remote Password protocol for additional security. Currently, SRP support is only available for the iOS, Android, and JavaScript SDKs. 

We explored the use of server-side authentication flow and client-side authentication flow in the last two recipes. There are additional flows, such as the custom authentication flow and the user migration authentication flow. You can refer to the link to `Amazon Cognito User Pool Authentication Flow` provided in the `See also` section. 

## See also
https://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-user-pools-authentication-flow.html