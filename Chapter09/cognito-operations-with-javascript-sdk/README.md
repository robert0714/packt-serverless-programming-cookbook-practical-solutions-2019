# Implementing and testing Cognito operations with the JavaScript SDK
We saw Cognito operations with AWS CLI commands in Chapter 4, Application Security with Amazon Cognito. With web applications, Cognito is generally used with the JavaScript SDK from the frontend. Hence, we will see various uses of Cognito using the JavaScript SDK and then test it using CodePen. We may use CodePen or any other tool, or use it within our application (for example, an Angular app) and then test it. 

## Getting ready
The prerequisites for this recipe are as follows:

1. A working AWS account
2. The Node.js and npm installed on your machine
3. Download the `amazon-cognito-identity.min.js` file
4. Create an `S3` bucket and upload the `amazon-cognito-identity.min.js` file
5. Create a `Cognito` user pool and a client

We also need to complete the following setup before we follow the steps outlined in the How to do it... section:

### Download the amazon-cognito-identity.min.js file
Create a `temp` folder and go inside the folder.

Run this: 
```bash
npm i amazon-cognito-identity-js
```

We should see a response similar to this:


If we do an `ls`, we should see the following two folders created:


If we go inside `node_modules/amazon-cognito-identity-js/dist`, we should see these files:

### Create an S3 bucket and upload the amazon-cognito-identity.min.js file
Create an `S3` bucket as follows:
```bash
aws s3api create-bucket \
    --bucket serverlesscookbook-cognito-files \
    --profile admin
```    
Upload the `amazon-cognito-identity.min.js` file:
```bash
aws s3 cp amazon-cognito-identity.min.js s3://serverlesscookbook-cognito-files \
    --profile admin
```    
Execute the bucket policy that allows public read access to the bucket:
```bash
aws s3api put-bucket-policy \
    --bucket serverlesscookbook-cognito-files \
    --policy file://s3-website-policy.json \
    --profile admin
```    
The s3`-website-policy.json` file should have these contents:
```json
{  "Version":"2012-10-17",
    "Statement":[
        {    "Sid":"PublicReadGetObjectAccess",
            "Effect":"Allow",
            "Principal": "*",
            "Action":["s3:GetObject"],
            "Resource":["arn:aws:s3:::serverlesscookbook-cognito-files/*"]
        }  ]
}
```

### Creating a Cognito user pool and client
We can create a Cognito user pool as follows:
```bash
aws cognito-idp create-user-pool \
    --cli-input-json file://create-user-pool-cli-input.json \
    --region us-east-1 \
    --profile admin
```    
The `create-user-pool-cli-input.json` file has the following contents:
```json
{
    "PoolName": "javscript_pool",
    "Policies": {
        "PasswordPolicy": {
            "MinimumLength": 8,
            "RequireUppercase": true,
            "RequireLowercase": true,
            "RequireNumbers": true,
            "RequireSymbols": true
        }
    },
    "AutoVerifiedAttributes": [
        "email"
    ],
    "AliasAttributes": [
        "email"
    ],
    "EmailVerificationMessage": "Your verification code from MyApp is {####}",
    "EmailVerificationSubject": "Your verification code from MyAp",
    "UserPoolTags": {
        "Team": "Dev"
    }
}
```
This is the same as what we have seen in Chapter 4, Application Security with Amazon Cognito, which we can refer to for more details and explanations.

Create a user pool client:
```bash
aws cognito-idp create-user-pool-client \
    --user-pool-id us-east-1_P8srRzYqn \
    --client-name javscript-pool-client \
    --explicit-auth-flows USER_PASSWORD_AUTH \
    --region us-east-1 \
    --profile admin
```    
Replace the user pool ID value with the ID of the user pool you created in the previous step.

## How to do it...
We will use CodePen to execute the JavaScript SDK code for Cognito operations, following these steps:
1. Open CodePen and add locations to the required files.
2. Go to  https://codepen.io/.
3. Click the Create tab and then select the Pen option.
4. In the new window, click the Settings menu, select the Behaviour tab, and uncheck the Enabled option under Auto-Updating Preview.
5. In the Settings menu, select the JavaScript tab and do the following:
    1. Search for `aws sdk` and select the appropriate SDK:
    CodePen will populate the SDK URL (as we will see in the next screenshot).
6. Add the URL of our `amazon-cognito-identity.min.js` file (for example, https://s3.amazonaws.com/cognito-min-bucket/amazon-cognito-identity.min.js):
7. Click Close. We can now run JavaScript code from within the Cognito JS tab, as we did in the previous recipe:
8. We can sign up/register the user with the following code:
    ```JavaScript
    var poolData = {
        UserPoolId: '<user pool id>',
        ClientId: '<client id>'
    };
    var userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);
    var attributeList: CognitoUserAttribute[] = [];
    var emailAttribute = {

    Name : 'email',
    Value : '<user email>'
    };

    attributeList.push(new AmazonCognitoIdentity.CognitoUserAttribute(emailAttribute));

    userPool.signUp('heartin', 'Passw0rd$1', attributeList, null, function(err, result){
        if (err) {
            console.log(JSON.stringify(err));
            alert(err.message || JSON.stringify(err));
            return;
        }
        var cognitoUser = result.user;
        console.log('user name is ' + cognitoUser.getUsername());
    });
    ```
9. Update the code with correct userpool ID, client ID, username, and password, and click Run.
   We can view the log messages in the developer console as follows:

10. Confirm the registered user by using the code received in the email provided during registration:
    ```JavaScript
    var poolData = {
    UserPoolId: '<user pool id>',
    ClientId: '<client id>'
    };
    var userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);
    var userData = {
            Username : 'heartin',
            Pool : userPool
        };

    var cognitoUser = new AmazonCognitoIdentity.CognitoUser(userData);
        cognitoUser.confirmRegistration('698099', true, function(err, result) {
            if (err) {
                alert(err.message || JSON.stringify(err));
                return;
            }
            console.log('call result: ' + result);
        });
    ```    
    Replace `698099` with the code you received. Run the script from CodePen and we should receive a response similar to this in the developer logs:

11. Sign in to the application using the registered email ID and password:
    ```JavaScript
    var authenticationData = {
            Username : 'heartin',
            Password : 'Passw0rd$1',
        };
        
        var authenticationDetails = new AmazonCognitoIdentity.AuthenticationDetails(authenticationData);
        var poolData = {
            UserPoolId: '<user pool id>',
            ClientId: '<client id>' 
        };
        
        var userPool = new AmazonCognitoIdentity.CognitoUserPool(poolData);
        var userData = {
            Username : 'heartin',
            Pool : userPool
        };
        
        var cognitoUser = new AmazonCognitoIdentity.CognitoUser(userData);
        cognitoUser.authenticateUser(authenticationDetails, {
            onSuccess: function (result) {
                var accessToken = result.getAccessToken().getJwtToken();
                console.log('access token is:' + accessToken);
            }, 
            onFailure: function(err) {
            console.log(JSON.stringify(err));
                alert(err.message || JSON.stringify(err));
            },
    
        });
    ```    
    If successful, we should receive the access token in the response and we can verify it from the browser developer console:



We can now use this access token for further operations.

## How it works...
We used CodePen to execute a basic user signup and login flow using the JavaScript SDK for Cognito. The APIs used correspond to the AWS CLI APIs used in Chapter 4, Application Security with Amazon Cognito, which we can refer to for more details and explanations.

## There's more...
We have implemented the JavaScript SDK-based code for Cognito and executed it from CodePen. We can use the code with any JavaScript application, or a framework such as Angular. We implemented only one login flow. You may follow this recipe and the recipes in Chapter 4, Application Security with Amazon Cognito, and do the same for all the other flows discussed.

## See also
https://docs.aws.amazon.com/cognito/latest/developerguide/using-amazon-cognito-user-identity-pools-javascript-examples.html