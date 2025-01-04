# Creating a Cognito user pool
Amazon Cognito can be used as an identity provider, as well as an identity broker. In this chapter, we will create a simple Cognito user pool, and we'll explore the use of Cognito as an identity provider. In later recipes, we will look at some more customization for the Cognito user pool. 

## How to do it...
The initial setup, such as the user pool creation, is generally done using CloudFormation templates; authentication and authorization are generally done with the SDK. 

### Creating Cognito user pool with AWS CLI
In this section, we will create a user pool by using CLI commands:
1. Generate the input JSON template, using the generate-cli-skeleton option:
    ```bash
    aws cognito-idp create-user-pool \
        --pool-name my-pool-from-cli \
        --generate-cli-skeleton
    ```
    This command will return a template with all of the supported properties for the create-user-pool sub-command, in the correct JSON format. 
2. Fill in the properties that are required within the JSON file, and remove the properties that are not required:
   1. Start the JSON file, specifying a name by using the PoolName property:
        ```json        
        {
        "PoolName": "MyFirstUserPool",
        ```
   2. Under the Policies section, we will define the password policy, using the PasswordPolicy sub-property:
        ```json   
        "Policies": {
        "PasswordPolicy": {
            "MinimumLength": 8,
            "RequireUppercase": true,
            "RequireLowercase": true,
            "RequireNumbers": true,
            "RequireSymbols": true
        }
        },
        ```
   3. Define AutoVerifiedAttributes and AliasAttributes, as follows:
        ```json        
        "AutoVerifiedAttributes": [
        "email"
        ],
        "AliasAttributes": [
        "email"
        ],
        ```
   4. Refer to the How it works... section for more details.
        ```bash
        Define an email verification message and an email verification subject:
        "EmailVerificationMessage": "Your verification code from MyApp is {####}.",
        "EmailVerificationSubject": "Your verification code from MyApp",
        ```
        > In this recipe, we will only demonstrate email verification. In a later recipe, we will look at how to do SMS verification.
   5. Define a tag by using the `UserPoolTags` property, as follows:
        ```json
        "UserPoolTags": {
        "Team": "Dev"
        },
        ```
   6. Define the `AdminCreateUserConfig` property, as follows:
        ```json
        "AdminCreateUserConfig": {
        "AllowAdminCreateUserOnly": false,
        "UnusedAccountValidityDays": 7,
        "InviteMessageTemplate": {
            "EmailMessage": "Your username for MyApp is {username} and password is {####}.",
            "EmailSubject": "Your temporary password for MyApp"
        }
        }
        ```
        The `AllowAdminCreateOnly` property, if set to `true`, restricts creating accounts to administrators. We will set it to `false`, as we will be doing user sign-up with this user pool in a later recipe. The complete JSON file is available in the code files.
3. Execute the `aws congnito-idp create-user-pool command`, specifying this JSON file:
    ```bash
    aws cognito-idp create-user-pool \
        --cli-input-json file://resources/create-user-pool-cli-input.json \
        --profile admin
    ```    
    Note the `user-pool-id`, for use in future commands.
    ```json
    {
        "UserPool": {
           "Id": "ap-northeast-1_vk0YGvCZD",
           "Name": "MyFirstUserPool",
         (ommitted...)
    ```
    We can verify `user-pool-created` by using the `describe-user-pool` sub-command:
    ```bash
    aws cognito-idp describe-user-pool \
    --user-pool-id ap-northeast-1_vk0YGvCZD \
    --profile admin
    ```
    We can verify `user-pool-created` by using the `Token signing key URL` :
    ```
    https://cognito-idp.${AWS::Region}.amazonaws.com/${AWS::user-pool-id}/.well-known/jwks.json
    
    # examples
    https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_vk0YGvCZD/.well-known/jwks.json
    ```
    We can verify `user-pool-created` by using the `Metadata endpoint URL` :
    ```
    https://cognito-idp.${AWS::Region}.amazonaws.com/${AWS::user-pool-id}/.well-known/openid-configuration

    # examples
    https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_vk0YGvCZD/.well-known/openid-configuration
    ```
    Remember to replace the `user-pool-id` value with our `user-pool-id` from the previous command. The `describe-user-pool` sub-command returns the current properties of the `user-pool`.

### Creating Cognito user pool with CloudFormation template
Various sections of the CloudFormation template correspond to the CLI commands that we saw in the previous section. The complete template YAML file is available in the code files:
1. Start the template with the template format version and a description (optional):
    ```yaml
    ---
    AWSTemplateFormatVersion: '2010-09-09'
    Description: 'My First Cognito User Pool'
    ```
2. Start to define the user pool resource with the type, AWS::Cognito::UserPool:
    ```yaml
    Resources:
    MyFirstUserPool:
        Type: AWS::Cognito::UserPool
    ```    
3. Under Properties, first, define a Policies property with a PasswordPolicy, as follows:
    ```yaml
    Properties:
    Policies:
        PasswordPolicy:
        MinimumLength: 8
        RequireLowercase: true
        RequireNumbers: true
        RequireSymbols: true
        RequireUppercase: true
    ```      
4. Define AutoVerifiedAttributes and AliasAttributes, as follows:
    ```yaml
    AutoVerifiedAttributes:
    - email
    AliasAttributes:
    - email  
    ```
5. Define an email verification message and an email verification subject, as follows:
    ```yaml
    EmailVerificationMessage: 'Your verification code from MyApp is {####}.'
    EmailVerificationSubject: 'Your verification code from MyApp'
    ```
6. Define the AdminCreateUserConfig property, as follows:
    ```yaml
    AdminCreateUserConfig:
    AllowAdminCreateUserOnly: false
    InviteMessageTemplate:
        EmailMessage: 'Your username for MyApp is {username} and password is {####}.'
        EmailSubject: 'Your temporary password for MyApp'
    UnusedAccountValidityDays: 7
    ```  
    `AllowAdminCreateOnly` restricts creating accounts to administrators.
7. Provide a name and add a tag for this user pool (this is optional):
    ```yaml
    UserPoolName: 'MyApp User Pool'
    UserPoolTags:
    Team: Dev
    ```  
8. In the Outputs section, return the user-pool-id. Also, export the user pool, so that we can reuse it in later recipes:
    ```yaml
    Outputs:
    UserPoolId:
        Description: 'Cognito user pool'
        Value: !Ref MyFirstUserPool
        Export:
        Name: MyFirstUserPoolId
    ```      
    Save the file as `cognito-user-pool-cf-template.yml`.

9. Execute the CloudFormation template by using `aws cloudformation create-stack`, in order to create a CloudFormation stack.
We can run the `aws cloudformation describe-stacks` command to find the status and get the `user-pool-id`.
We can also use the `describe-user-pool` sub-command, with the ID returned by the `describe-stacks` sub-command, to verify the new Cognito user pool:
    ```bash
    aws cognito-idp describe-user-pool \
        --user-pool-id ap-northeast-1_rXSW6qnjL \
        --profile admin
    ```
    If it is successful, this command will return the current state of the newly created user pool. The initial part of the response contains the `id`, `name`, `policies`, an empty `LambdaConfig`, the last modified date, and the creation date:
    ```json
    {
        "UserPool": {
            "Id": "ap-northeast-1_rXSW6qnjL",
            "Name": "MyApp User Pool",
            "Policies": {
                "PasswordPolicy": {
                    "MinimumLength": 8,
                    "RequireUppercase": true,
                    "RequireLowercase": true,
                    "RequireNumbers": true,
                    "RequireSymbols": true,
                    "TemporaryPasswordValidityDays": 7
                }
            },
            "DeletionProtection": "INACTIVE",
            "LambdaConfig": {},
            "LastModifiedDate": "2024-11-22T14:31:09.795000+08:00",
            "CreationDate": "2024-11-22T14:31:09.795000+08:00",
            (ommitted..)
        }
    }       
    ``` 
    The `SchemaAttributes` section will contain the definitions for all of the attributes (including the default attributes), in the following format:
    ```json
    "SchemaAttributes": [
            {
                "Name": "profile",
                "AttributeDataType": "String",
                "DeveloperOnlyAttribute": false,
                "Mutable": true,
                "Required": false,
                "StringAttributeConstraints": {
                    "MinLength": "0",
                    "MaxLength": "2048"
                }
            },    
    ``` 
    Other attributes contained within the `SchemaAttributes` section include the `name`, `given_name`, `family_name`, `middle_name`, `nick_name`, `preferred_username`, `profile`, `picture`, `website`, `email`, `email_verified`, `gender`, `birthdate`, `zoneinfo`, `locale`, `phone_number`, `phone_number_verified`, `address`, and `updated_at`.

    The remainder of the response is as follows:
    ```json
    "AutoVerifiedAttributes": [
            "email"
        ],
        "AliasAttributes": [
            "email"
        ],
        "EmailVerificationMessage": "Your verification code from MyApp is {####}.",
        "EmailVerificationSubject": "Your verification code from MyApp",
        "VerificationMessageTemplate": {
            "EmailMessage": "Your verification code from MyApp is {####}.",
            "EmailSubject": "Your verification code from MyApp",
            "DefaultEmailOption": "CONFIRM_WITH_CODE"
            "DefaultEmailOption": "CONFIRM_WITH_CODE"
        },
        "UserAttributeUpdateSettings": {
            "AttributesRequireVerificationBeforeUpdate": []
        },
        "MfaConfiguration": "OFF",
        "EstimatedNumberOfUsers": 0,
        "EmailConfiguration": {
            "EmailSendingAccount": "COGNITO_DEFAULT"
        },
        "UserPoolTags": {
            "Team": "Dev"
        },
        },
        "UserAttributeUpdateSettings": {
            "AttributesRequireVerificationBeforeUpdate": []
        },
        "MfaConfiguration": "OFF",
        "EstimatedNumberOfUsers": 0,
        "EmailConfiguration": {
            "EmailSendingAccount": "COGNITO_DEFAULT"
        },
        "UserPoolTags": {
            "Team": "Dev"
        },
        "UserAttributeUpdateSettings": {
            "AttributesRequireVerificationBeforeUpdate": []
        },
        "MfaConfiguration": "OFF",
        "EstimatedNumberOfUsers": 0,
        "EmailConfiguration": {
            "EmailSendingAccount": "COGNITO_DEFAULT"
        },
        "UserPoolTags": {
            "Team": "Dev"
        },
        "EstimatedNumberOfUsers": 0,
        "EmailConfiguration": {
            "EmailSendingAccount": "COGNITO_DEFAULT"
        },
        "UserPoolTags": {
            "Team": "Dev"
        },
            "Team": "Dev"
        },
        "AdminCreateUserConfig": {
        "AdminCreateUserConfig": {
            "AllowAdminCreateUserOnly": false,
            "UnusedAccountValidityDays": 7,
            "InviteMessageTemplate": {
                "EmailMessage": "Your username for MyApp is {username} and password is {####}.",
                "EmailSubject": "Your temporary password for MyApp"
            }
        },
        "Arn": "arn:aws:cognito-idp:ap-northeast-1:937197674655:userpool/ap-northeast-1_rXSW6qnjL",  
    ```
    We can verify `user-pool-created` by using the `Token signing key URL` :
    ```
    https://cognito-idp.${AWS::Region}.amazonaws.com/${AWS::user-pool-id}/.well-known/jwks.json
    
    # examples
    https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_rXSW6qnjL/.well-known/jwks.json
    ```
    We can verify `user-pool-created` by using the `Metadata endpoint URL` :
    ```
    https://cognito-idp.${AWS::Region}.amazonaws.com/${AWS::user-pool-id}/.well-known/openid-configuration

    # examples
    https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_rXSW6qnjL/.well-known/openid-configuration
    ```

10. To clean up, we can delete the user pool by deleting the stack, or keep the stack and reuse it in the next recipe.

## How it works...
Cognito is the primary service in AWS that can be used as an identity provider, for securing applications with authentication, authorization, and access control. The important features of Cognito are as follows:
* User sign-up
* User sign-in
* User creation by an administrator
* A set of predefined attributes, as well as support for creating custom attributes
* **Multi-factor authentication (MFA)**
* User profile management
* Email and SMS verification
* Forgot password
* Forcing a change of password after first login (in the case of admin user creation)
* Support for guest users
* Prevention of man-in-the-middle attacks through **Secure Remote Password (SRP)** protocols
* Enabling or disabling of user accounts by an administrator
* Deleting user accounts
* Support for customization, using Lambdas invoked through predefined triggers
* Support for authentication from other identity providers, such as Google, Facebook, Twitter, and so on


### Generating and using JSON templates with CLI commands
Most AWS CLI commands come with options either to specify the input parameters directly on the command line, or input them through a JSON file specified by the `cli-input-json` property. A template for this JSON file can be generated by using the `generate-cli-skeleton` property option. 

For the `create-user-pool` sub-command, we used the `cli-input-json` property, specifying a JSON file created using the `generate-cli-skeleton` property option. The `create-user-pool` sub-command has many properties, and some of them have sub-properties. It would be easy (and less error-prone) to get the template generated in the right format.


### AliasAttributes versus UsernameAttributes
The Cognito `create user pool` sub-command supports two properties that allow for additional properties, such as the username and email to be used for logging in. `AliasAttributes` defines the supported attributes to be used as an alias for this user pool. The possible values for AliasAttributes are the `phone_number`, email, or Preferred_username. `UsernameAttributes` defines the supported attributes that can be specified as usernames when a user signs up. The possible values for `UsernameAttributes` are the `phone_number` or `email`.

While the `AliasAttributes` property allows us to use additional attributes as aliases for our original username for `login`, the `UsernameAttributes` property allows us to use the specified attributes as usernames, instead of another username. We cannot specify both AliasAttributes and `UsernameAttributes` in a single configuration, or we will get an error (`InvalidParameterException`) stating that only one of the aliasAttributes or `usernameAttributes` can be set in a user pool.

The `AliasAttributes` or `UsernameAttributes` that we use has to be unique across our user pool.

### There's more...
We created a Cognito user pool in this recipe. To start using the Cognito user pool, we also need to create an app client. An app client is an entity that has permission to call APIs as unauthenticated users; such API functions include register, sign-in, and forgot password. We will look at how to create a Cognito user pool, and then how to use it to perform unauthenticated calls, in the next recipe.

We explored the use of Cognito as an identity provider. Identity providers provide user pool management on their own. Cognito can also be used as an identity broker, where an external provider will maintain the user pool, and Cognito will just provide temporary credentials, after that provider verifies the user credentials. However, most of these external providers will need an actual domain name that we own, for security reasons.

### See also
* https://aws.amazon.com/compliance/shared-responsibility-model/
* https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-cost-allocation-tagging.html
* https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-client-apps.html
* https://docs.aws.amazon.com/cli/latest/userguide/generate-cli-skeleton.html
