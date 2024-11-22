# User sign-up with SMS verification and MFA
In the previous recipes, we did user sign-up with a text username and email verification. In this recipe, we will create a user pool to support SMS and MFA verification, and then do sign-up using a login with SMS and MFA verification.

We will also support user sign-up with a phone number as the username. However, you do not need to specify a phone number as the username to support SMS and MFA verification. Instead, you can specify `phone_number` as an auto-verified attribute, similar to how we specified email in the previous recipes.

## Getting ready
The following are the prerequisites for completing this recipe:
* You will need a working phone number that can receive SMS to complete the steps within this recipe.

## How to do it...
We will first create a user pool that supports SMS verification, and then, we will do user sign-up with SMS verification. 

> I will not explain the theory behind the CLI commands or CloudFormation template parameters that we have already discussed in the previous recipes within this chapter. If you are not following the recipes in order, please refer to the previous recipes whenever you need extra details for those CLI commands or CloudFormation template parameters.

### Creating the user pool
First, we will look at how to create a user pool that supports using a phone number as a username, with SMS and MFA verification. 

#### Creating a Cognito user pool client with AWS CLI
We will now create a user pool using CLI commands. In the next section, we will create the user pool using a CloudFormation template:
1. Create a role that allows Cognito to send SMS messages:
   1. Create a policy JSON file with an action, sns:publish, as follows:
        ```json
        {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Effect": "Allow",
                    "Action": [
                        "sns:publish"
                    ],
                    "Resource": [
                        "*"
                    ]
                }
            ]
        }
        ```
        Save this as `sns-publish-policy.txt`.
   2. Create the policy, as follows:
        ```bash
        aws iam create-policy \
            --policy-name cognito_sns_iam_policy \
            --policy-document file://resources/sns-publish-policy.txt \
            --profile admin
        ```
   3. Create a trust relationship document for the role, as follows:
        ```json   
        {
        "Version": "2012-10-17",
        "Statement": [
            {
            "Effect": "Allow",
            "Principal": {
                "Service": "cognito-idp.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
            }
        ]
        }
        ```
        Save this as `assume-role-trust-relationship-policy-document.txt`.
   4. Create the role, as follows:
        ```bash   
        aws iam create-role \
            --role-name cognito_sns_iam_role \
            --assume-role-policy-document file://resources/assume-role-trust-relationship-policy-document.txt \
            --profile admin
        ```    
        Note the role ARN.
   5. Attach the policy to the role, as follows:
        ```bash
        aws iam attach-role-policy \
        --role-name cognito_sns_iam_role \
        --policy-arn arn:aws:iam::<account_id>:policy/cognito_sns_iam_policy \
        --profile admin
        ``` 
2. Generate the input JSON template by using the `generate-cli-skeleton` option, and fill in the properties that are required within the JSON file (remove the properties that are not required). 
   1. We will start the JSON file by specifying a name, using the `PoolName` property:
        ```json   
        {
        "PoolName": "QnaTime.com User Pool",
        ```  
      `QnaTime.com` is a domain that is bought in Chapter 5, [Web Hosting with S3, Route 53, and CloudFront](../../Chapter05/) .
   2. Under the `Policies` section, we will define the password policy, using the `PasswordPolicy` sub-property:
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
   3. Next, define AutoVerifiedAttributes and UsernameAttributes:
        ```json   
        "AutoVerifiedAttributes": [
        "phone_number"
        ],
        "UsernameAttributes": [
        "phone_number"
        ],
        ```
   4. Define an SMS verification message and email verification subject, as follows:
      ```json
      "EmailVerificationMessage": "Your verification code from qnatime.com is {####}.",
      ```
   5. Enable MFA, as follows:
      ```json      
      "MfaConfiguration": "ON",
      ```
      You can set `MfaConfiguration` to `"OFF"` to disable MFA, or to `"OPTIONAL"` to make MFA optional for users.
   6. Define the SMS configuration, as follows:
        ```json   
        "SmsConfiguration": {
        "SnsCallerArn": "arn:aws:iam::855923912133:role/cognito_sns_iam_role",
        "ExternalId": "some-unique-external-id-preferably-a-uuid"
        },
        ```
        Here, `SnsCallerArn` is the ARN of the role that you created in the previous step. The external ID is a unique external ID. If you are creating the user pool from the Management Console, AWS will generate a UUID value for this field.
   7. Define any tags, if they are needed (optional).
   8. Define the `AdminCreateUserConfig` property:
        ```json
        "AdminCreateUserConfig": {
            "AllowAdminCreateUserOnly": false,
            "UnusedAccountValidityDays": 7,
            "InviteMessageTemplate": {
                "SMSMessage": "Your username for qnatime.com is {username} and password is {####}."
            }
        }
        ```
        We are not using `InviteMessageTemplate` in this recipe, as we are doing user sign-up, but it is shown for reference. Save this file as `create-user-pool-cli-input.json`. The complete JSON file is available in the code files. 
3. Execute the `create-user-pool` sub-command, specifying this JSON file, as follows:
    ```bash
    aws cognito-idp create-user-pool \
    --cli-input-json file://resources/create-user-pool-cli-input.json \
    --profile admin
    ``` 
    Note the `user-pool-id`, for use in further commands.

4. You can verify `user-pool-created` by using the `describe-user-pool` sub-command.

#### Creating a Cognito user pool client with CloudFormation template
Creating Cognito user pools is more commonly done using CloudFormation templates. Various sections of the CloudFormation template correspond to the CLI commands that you saw in the previous section:

1. You can start the template with a description and a name. We will call our template file `cognito-user-pool-cf-template.yml`. You can find the file under the resources folder for this recipe:
    ```yaml
    ---
    AWSTemplateFormatVersion: '2010-09-09'
    Description: Cognito User Pool with SMS and MFA Verification
    ```
2. Start to define the role for our resource with an inline policy definition, as follows:
    ```yaml
    Resources:
    SNSRole:
        Type: "AWS::IAM::Role"
        Properties:
        AssumeRolePolicyDocument:
            Version: "2012-10-17"
            Statement:
            - Effect: "Allow"
            Principal:
                Service:
                - "cognito-idp.amazonaws.com"
            Action:
            - "sts:AssumeRole"
        Policies:
        - PolicyName: "CognitoSNSPolicy"
            PolicyDocument:
            Version: "2012-10-17"
            Statement:
            - Effect: "Allow"
                Action: "sns:publish"
                Resource: "*"
    ```            
3. Define the user pool resource with the type `AWS::Cognito::UserPool`:
    ```yaml
    UserPool:
    Type: "AWS::Cognito::UserPool"
    ```  
4. Under `Properties`, define `AutoVerifiedAttributes` and `AliasAttributes`:
    ```yaml
    Properties:
    AutoVerifiedAttributes:
    - phone_number
    UsernameAttributes:
    - phone_number
    ```  
5. Define an SMS verification message and an email verification subject:
    ```yaml
    SmsVerificationMessage: 'Your verification code from qnatime.com is {####}.'
    ```
6. Define MFA and SMS configuration, as follows:
    ```yaml
    MfaConfiguration: "ON"
    SmsConfiguration:
    ExternalId: 'some-unique-external-id-preferably-a-uuid'
    SnsCallerArn: !GetAtt SNSRole.Arn
    ```  
7. We will define the AdminCreateUserConfig property, as follows:
    ```yaml
    AdminCreateUserConfig:
    AllowAdminCreateUserOnly: false
    InviteMessageTemplate:
        SMSMessage: 'Your username for qnatime.com is {username} and password is {####}.'
    UnusedAccountValidityDays: 7
    ```  
    We are not using `InviteMessageTemplate` in this recipe, as we are performing user creation by admin, but it is given for reference. 
8. Although it is not required, we will provide a name and add a tag for this user pool:
    ```yaml
    UserPoolName: 'Qnatime.com User Pool'
    UserPoolTags:
    Team: Dev
    ```  
9. In the Outputs section, we will return the user pool ID and the client ID, as follows:
    ```yaml
    Outputs:
    UserPoolId:
        Value: !Ref UserPool
        Export:
        Name: "UserPool::Id"
    UserPoolClientId:
        Value: !Ref UserPoolClient
        Export:
        Name: "UserPoolClient::Id"
    ```      
    The complete CloudFormation template is available in the code files.
10. Execute the CloudFormation template to create a CloudFormation stack.
11. You can run the `describe-stacks` sub-command to get the status and the `user-pool-id`. You can also use the `describe-user-pool` sub-command with the ID returned by the `describe-stacks` sub-command, in order to verify the new Cognito user pool.
12. To clean up, you can delete the user pool by deleting the stack, or you can keep the stack.


### User sign-up with SMS and MFA verification
First, we will set up a user pool client for SMS verification; then, we will do user sign-up with SMS verification:
1. Create a user pool client, as follows:
   ```bash
    aws cognito-idp create-user-pool-client \
        --user-pool-id us-east-1_n5USdCHNf \
        --explicit-auth-flows USER_PASSWORD_AUTH \
        --client-name user-pool-client-signup \
        --profile admin
   ```
    You can use `describe-user-pool-client` to get the details of the user pool client.

2. Do user sign-up with a phone number as the username, as follows:
    ```bash
    aws cognito-idp sign-up \
        --client-id 6amm4ins1md8fo5tvhtmel183h \
        --username +917411174114 \
        --password Passw0rd$
    ```    
    You will need to start the phone number with a `+`, followed by the country code (for example, `+44` for the United Kingdom and `+91` for India).

    If this is successful, you should get the following response:
    ```json
    {
        "UserConfirmed": false,
        "CodeDeliveryDetails": {
            "Destination": "+********4114",
            "DeliveryMedium": "SMS",
            "AttributeName": "phone_number"
        },
        "UserSub": "f8f7f918-23dc-43da-a88b-4a7364c78072"
    }
    ```
    You will now get a confirmation code SMS at the phone number you specified.

    If you do not receive a confirmation authentication code after waiting for some time, or if the one that you received expires, you can use the `resend-confirmation-code` command, as follows:
    ```bash
    aws cognito-idp resend-confirmation-code \
        --client-id 6amm4ins1md8fo5tvhtmel183h \
        --username +917411174114
    ```    
3. Confirm the user sign-up with the confirmation authentication code that was received in the previous step:
    ```bash
    aws cognito-idp confirm-sign-up \
        --client-id 6amm4ins1md8fo5tvhtmel183h \
        --username +917411174114 \
        --confirmation-code 432348
    ```    
4. Initiate the authentication flow, as follows:
    ```bash
    aws cognito-idp initiate-auth \
        --client-id 6amm4ins1md8fo5tvhtmel183h \
        --auth-flow USER_PASSWORD_AUTH \
        --auth-parameters USERNAME=+917411174114,PASSWORD=Passw0rd$
    ```    
    As we have enabled MFA, you should get back an authentication challenge in the response, as shown in the following screenshot:
    ```json
    {
        "ChallengeName": "SMS_MFA",
        "Session": "Ud3vrfFW0Xfx04tPDzE8rtGass4A1XnQIGrac-VcmSKVUDoC9FjiAbsAr3fyxwb577t4vCHBwMCadSbcm6n_4-ypA0hxMfYTEfn4TjEdfs9jVkg40egdvmXZ100lm9WFMVSb6f_CeYxxth1FsNiZiwxE0K2iLUIK74nzB0RAoh
        t4QKisydnFHwka82RvnewSwJF5vf6VnEuZ00b-qatdUN-B9kUWtYK8ImwDZuXZobxQwDcGtBGeyTzQynIGCxqB3xbRAv6Q1wp2RbRZNaiee2koHj9DybybVe0jL_kCmyjVNtPy061HcSpV3AM4D007COM-khuRLHFnKDIgkFT190Dfu9KpQPLUKKΧΗlcfG5RKktU_6i6ulH9VDW2T3tR0FXyxZRhGWzJ7Q5w69G45toUU0Fb_CYmGN9EKkD6HJ5SB8NneWj-sGN7dM7usALi080VAQCF0Lc8K0xYaWx9g2VNPHeosMFUW1-R0yE4HkJ0Q2YBsBg3BtVADadcVT8zsv1StPHhUKVyw4LGytq7oK32WQwh8GSue32vWIXdTFzdN4MnqW8Ye0idNW-4AxkBP6KC3ovlL9kGL60Q5ki7m-_5phFjexNzWVYUpqEEf1sIZP6h6Hbm2d30mC29MwmqPSng0ot33UKyjE6_YE-FYYZpD4igZmt83cvjRlhCX_L_i7ZJltBe2xHlj7pq63Mn9IM0d0GAbkWw-ug-IKcku3hU",
        "ChallengeParameters": {
            "CODE_DELIVERY_DELIVERY_MEDIUM": "SMS",
            "CODE_DELIVERY_DESTINATION": "+********4114",
            "USER_ID_FOR_SRP": "f8f7f918-23dc-43da-a88b-4a7364c78072"
        }
    }
    ```
    You will now receive an SMS with an authentication code.
5. Respond to the authentication challenge with the authentication code that you received in an SMS and the session value that you received in the previous step:
    ```bash
    aws cognito-idp respond-to-auth-challenge \
        --client-id 6amm4ins1md8fo5tvhtmel183h \
        --challenge-name SMS_MFA \
        --challenge-responses USERNAME=+917411174114,SMS_MFA_CODE=650598 \
        --session <session>
    ```    
    If this is successful, you should get a response with the `AccessToken`, `RefreshToken`, and `IdToken`. You can use these for further operations, including deleting the user.

## How it works...
To summarize, we did the following in this recipe:
1. Created a role with an inline policy that allowed Cognito to use SMS to send (publish) messages
2. Created a Cognito user pool to support using a `phone_number` as the username, SMS verification, and MFA
3. Performed user sign-up with a `phone_number` as the username
4. SMS verification
5. Multi-factor authentication (MFA)

Multi-factor authentication (MFA) is an authentication done in addition to the standard authentication. In our case MFA is done by sending a code through SMS and we send back that code in the response. In this recipe, I used both SMS verification and MFA; however, within the code files, I have also provided the CLI commands for scenarios where we perform sign-up and sign-in without MFA. You can disable MFA support while creating the user pool, by setting the `MfaConfiguration` parameter to `false`.    

## There's more...
Even though we only discussed using a phone number as a username with SMS verification for signing up, you could also use email, or a combination of email and SMS verification.

## See also
You can read more about email and phone verification at: https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-email-phone-verification.html.