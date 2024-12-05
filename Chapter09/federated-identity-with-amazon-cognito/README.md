# Federated identity with Amazon Cognito
`Federated identity` is a process where a user's identity and attributes are stored across different identity management systems. For example, we can use federated identity to allow users to log in to our system with another identity provider's credentials. In this recipe, we will see how to use federated identity with Cognito user pools. For the purpose of demonstration, we will be using Google Plus as the identity provider and Cognito will act as the identity broker. The general process will be the same for any other providers, such as Facebook or Amazon. 

## Getting ready
The following are the prerequisites for this recipe:
* A working AWS account.
* An S3 static website with a real domain name pointing to it. You may follow the recipes in Chapter 5, [Web Hosting with S3, Route53, and CloudFront](../../Chapter05/README.md) to create a static website, register a domain, and link the domain name to the static website.
* A basic understanding of HTML and JavaScript is good to have.


## How to do it...
We first need to configure the identity provider ~~(Google Plus in our case)~~ to be used for federated identity login.
* https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-identity-federation.html
* https://repost.aws/knowledge-center/cognito-google-social-identity-provider

### Step 1 - Configuring The Identity Provider
Before we can use ~~Google Plus~~ for federated identity logins with Cognito, we need to follows these steps:

1. Navigate to ~~https://console.developers.google.com~~.
2. Click Library on the left sidebar and accept the terms and conditions (of course after reading them) if displayed.
3. Scroll down, Select ~~Google+ API~~, and click Enable on the new page:

4. Click on Credentials on the left sidebar menu, then click Create to create a new credential. If we have not created a project yet, this should show us a new project window:

5. In the New Project window, do the following:
    1. Provide a Project Name
    2. Leave the default value for Location as it is
    3. Click the CREATE button
        Now, click Create credentials in the Credentials popup:


6. From the drop-down menu, select OAuth Client ID:


7. There may be a warning message: To create an OAuth client ID, you must first set a product name on the consent screen:
    1. Click Configure consent screen to the right of it
    2. In the OAuth consent screen tab, do the following:
        1. Give an application name
        2. In the Authorized domains section, add a valid domain name:


        3. Click Save at the bottom of the form to save
8. We will be redirected to the page for creating an OAuth client ID. Set Application Type as Web application and enter our domain under Authorized JavaScript origins:

9. Click Create and we will be provided with a client ID:

### Step 2 - Creating and Configuring an Identity Pool
Let's now create and configure an identity pool from the AWS CLI:
1. Create a provider.json file, with Google as the provider name and the client ID received from the previous step:
    ```json
    { "accounts.google.com" : "55367180174-6brhjc2v6kdllcejabnr1e46957f72te.apps.googleusercontent.com" }
    ```
2. Create an identity pool:
    ```bash
    aws cognito-identity create-identity-pool \
        --identity-pool-name qnatimepool \
        --no-allow-unauthenticated-identities \
        --supported-login-providers file://provider.json
        --profile admin
    ```    
You should get a response similar to this:


3. Create a policy that allows the necessary permissions to the user:
    ```bash
    aws  iam  create-policy  \
        --policy-name  identity-pool-policy  \
        --policy-document file://role_policy.txt \
        --profile  admin
    ```    
4. The `role_policy.txt` file has the following contents:
    ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "mobileanalytics:PutEvents",
                    "cognito-sync:*",
                    "cognito-identity:*"
                ],
                "Resource": [
                    "*"
                ]
            }
        ]
    }
    ```
    There should be a role associated with a user who uses public APIs to log in, so that AWS can issue a token to access AWS services.

5. Create a role named `identity-pool-role` and attach a policy to the role.
6. Create a `roles.json` file with the ARN role as follows:
    ```json
    {"authenticated":"arn:aws:iam::<account id>:role/identity-pool-role"}
    ```
7. Attach the role to the pool:
    ```bash
    aws cognito-identity set-identity-pool-roles \
    --identity-pool-id <your identity pool id> \
    --roles file://roles.json \
    --region us-east-1 \
    --profile admin
    ``` 

### Step 3 - Preparing and uploading code files
We need two HTML files, `index.html` for primary landing page and `error.html` for errors. We will see the important components within the `index.html` here. We will use JavaScript SDK code from within the `index.html` file. A completed i`ndex.html` file with required JavaScript code and `error.html` file are available with code files.

#### Preparing the index.html file
The index.html file should have the following contents:

Start defining the HTML file with a `DOCTYPE` declaration and the `<html>` tag:
```html
<!DOCTYPE html>
<html>
```
The `<head>` section of the HTML file should have contents as follows:
```html
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>QNA Time</title>
  <script src="https://apis.google.com/js/platform.js" async defer></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/aws-sdk/2.390.0/aws-sdk.min.js"></script>
  <meta
    name="google-signin-client_id"
    content="1056864857699-i6ami0u5oevpn9bro2k3r095jtqohdi7.apps.googleusercontent.com"
  />
</head>
```
Start defining a `<script>` section with the following variables:
```html
<script type="text/javascript">
  var id_token;
  var identity;
  var cognitoidentity = new AWS.CognitoIdentity({ region: "us-east-1" });
```  
Define the `getCredentials` method to get the access token from Cognito:
```javascript
function getCredentials() {
    var params1 = {
        IdentityId: identity,
        Logins: {
            "accounts.google.com": id_token
        }
    };
    cognitoidentity.getCredentialsForIdentity(params1, function(err, data) {
        if (err) console.log(err, err.stack);
        else {
            console.log(data);
            console.log(data.Credentials.AccessKeyId);
        }
    });
}
```
Add an `onSignIn` method that will be invoked on the success of Google authentication. This method is specified within the HTML body:
```javascript
  function onSignIn(googleUser) {
    id_token = googleUser.getAuthResponse().id_token;
    console.log("google_id_token:" + id_token);

    var params = {
      IdentityPoolId:
        "us-east-1:f36a0555-fd35-43d6-bafa-187ecdef0f04" /* required */,
      Logins: {
        "accounts.google.com": id_token
      }
    };

    cognitoidentity.getId(params, function(err, data) {
      if (err) console.log(err, err.stack);
      // an error occurred
      else {
        console.log(data);
        identity = data.IdentityId;
        getCredentials();
      }
    });
  }
</script>
```

Add the HTML body:
```html
<body>
<span style="text-align:center;"><h1>Welcome to QNA TIME</h1></span>
<form>
  <div
          style="width:200px;"
          class="g-signin2"
          data-onsuccess="onSignIn"
  ></div>
</form>
</body>
```

#### Deploying and testing the index.html file
Follow these steps to deploy the HTML file:
1. Copy `index.html` and `error.html` to the S3 bucket. Create an `error.html` with dummy contents or follow earlier chapter recipes. It is also available with the code files.
2. Hit the website URL:
3. Click on the Google Sign in button. If not already signed in to our Google account, we will be provided with an option to log in to your Google account. Once logged in, we should see the Signed in message.

## How it works...
A Cognito Federated Identity authentication flow to access AWS services has two forms: classic flow and enhanced flow.

Classic flow can be summarized as follows:
1. The user logs in with an external IDP such as Amazon, Google, or Facebook
2. The IDP returns an OAuth token
3. The client will then make a request to Cognito with the OAuth token
4. Cognito will validate the OAuth token with the IDP and if successful, return a token back
5. The client will then make an `AssumeRoleWithWebIdentity` call to STS, passing this token
6. STS will validate the token and return with temporary credentials (access key ID and secret access key)
7. The client can now use the temporary credentials to access AWS services

Enhanced flow can be summarized as follows:
1. User logs in with an external IDP such as Amazon, Google, or Facebook
2. The IDP returns an OAuth token
3. The client will then make a request to Cognito with the OAuth token
4. Cognito will validate the OAuth token with the IDP and if successful, return a token back
5. The client will then make a `GetCredentialsForIdentity` call with Cognito itself
6. Cognito will validate the token, negotiate with STS, and return temporary credentials (access key ID and secret access key)
7. The client can now use the temporary credentials to access AWS services

We followed the enhanced flow in this recipe.

## There's more...
We created a simple application to demonstrate the Cognito authentication flow. You may follow this recipe and implement the same thing, as per your application needs. 

## See also
* https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-identity.html
* https://docs.aws.amazon.com/cognito/latest/developerguide/authentication-flow.html