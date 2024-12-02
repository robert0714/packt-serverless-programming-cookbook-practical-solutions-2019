# Publishing to an SNS topic with SDK (Java)
In the previous recipe, we saw how to create an SNS topic, subscribe to that topic, and publish messages from AWS CLI. In this recipe, we will see how to publish a message to an SNS topic from Java Lambda code using the AWS Java SDK.  

## Getting ready
The following are the prerequisites for this recipe:
* You need to follow the section Getting started in the recipes [Your first AWS Lambda and Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) from Chapter 1, Getting Started with Serverless Computing on AWS to set up Java, Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/README.md) and AWS CLI, and may also read other notes there including code usage guidelines, S3 bucket creation and notes for the Windows users. 
* You should have already created the SNS topic and subscriptions as discussed in the [Your first SNS topic for email and SMS](../your-first-sns-topic/README.md) recipe.

## How to do it...
We will first create our Java Lambda. We will then provision it and test it from AWS CLI.

### Provisioning and testing the Lambda (AWS CLI)
Follow these steps to deploy and invoke the Lambda. You may follow Chapter 1, Getting Started with Serverless Computing on AWS and use CloudFormation for Lambda provisioning:

1. Run `mvn clean package` from inside the Lambda project root folder to create the Uber JAR.
2. Upload the Uber JAR to `S3`:
    ```bash
    aws s3 cp \
        target/lambda-sns-publish-with-sdk-0.0.1-SNAPSHOT.jar \
        s3://dev-for-tw-robert-20241020/lambda-sns-publish-with-sdk-0.0.1-SNAPSHOT.jar \
        --profile admin
    ```    
3. Create a role for the Lambda with an appropriate trust relationship definition:
    ```bash
    aws iam create-role \
        --role-name lambda-sns-publish-with-sdk-role \
        --assume-role-policy-document file://iam-role-trust-relationship.txt \
        --profile admin
    {
        "Role": {
            "Path": "/",
            "RoleName": "lambda-sns-publish-with-sdk-role",
            "RoleId": "AROA5UNKVUCP7MBEFDSUG",
            "Arn": "arn:aws:iam::937197674655:role/lambda-sns-publish-with-sdk-role",
            "CreateDate": "2024-12-02T13:45:19+00:00",
            "AssumeRolePolicyDocument": {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {
                            "Service": "lambda.amazonaws.com"
                        },
                        "Action": "sts:AssumeRole"
                    }
                ]
            }
        }
    }
    ```    
    Refer to the previous recipes or the code files for the trust relationship document file, `iam-role-trust-relationship.txt`.

4. Create a policy for basic logging permissions and attach it to the role.
    ```bash
    aws iam create-policy \
        --policy-name lambda-basic-iam-policy \
        --policy-document file://basic-lambda-permissions.txt \
        --profile admin
    {
        "Policy": {
            "PolicyName": "lambda-basic-iam-policy",
            "PolicyId": "ANPA5UNKVUCPZAITLWGNT",
            "Arn": "arn:aws:iam::937197674655:policy/lambda-basic-iam-policy",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2024-12-02T13:47:29+00:00",
            "UpdateDate": "2024-12-02T13:47:29+00:00"
        }
    }

    aws iam attach-role-policy \
        --role-name lambda-sns-publish-with-sdk-role \
        --policy-arn arn:aws:iam::937197674655:policy/lambda-basic-iam-policy \
        --profile admin
    ```
5. Create a policy for required SNS permissions and attach it to the role.
    The policy document with required SNS permissions is shown as follows:
    ```bash
    {
    "Version":"2012-10-17",
    "Statement":[
        {
            "Effect":"Allow",
            "Action":[
                "sns:Publish"
            ],
            "Resource":[
                "arn:aws:sns:*:*:*"
            ]
        }
    ]
    }
    ```
    cli
    ```bash
    aws iam create-policy \
        --policy-name lambda-sns-publish-policy \
        --policy-document file://lambda-sns-publish-permissions.txt \
        --profile admin
    {
        "Policy": {
            "PolicyName": "lambda-sns-publish-policy",
            "PolicyId": "ANPA5UNKVUCP34Z3LQ4BS",
            "Arn": "arn:aws:iam::937197674655:policy/lambda-sns-publish-policy",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2024-12-02T13:49:32+00:00",
            "UpdateDate": "2024-12-02T13:49:32+00:00"
        }
    }


    aws iam attach-role-policy \
        --role-name lambda-sns-publish-with-sdk-role \
        --policy-arn arn:aws:iam::937197674655:policy/lambda-sns-publish-policy \
        --profile admin
    ```
6. Create the Lambda function, as shown here:
    ```bash
    aws lambda create-function \
        --function-name lambda-sns-publish-with-sdk \
        --runtime java17 \
        --role arn:aws:iam::937197674655:role/lambda-sns-publish-with-sdk-role \
        --handler tech.heartin.books.serverlesscookbook.LambdaSnsPublishHandler::handleRequest \
        --code S3Bucket=dev-for-tw-robert-20241020,S3Key=lambda-sns-publish-with-sdk-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
    {
        "FunctionName": "lambda-sns-publish-with-sdk",
        "FunctionArn": "arn:aws:lambda:ap-northeast-1:937197674655:function:lambda-sns-publish-with-sdk",
        "Runtime": "java17",
        "Role": "arn:aws:iam::937197674655:role/lambda-sns-publish-with-sdk-role",
        "Handler": "tech.heartin.books.serverlesscookbook.LambdaSnsPublishHandler::handleRequest",
        "CodeSize": 10673040,
        "Description": "",
        "Timeout": 15,
        "MemorySize": 512,
        "LastModified": "2024-12-02T13:51:22.977+0000",
        "CodeSha256": "kX5YvNpM/yGdAyUBQeZSgaKlmR04LPKODsAXH6huv1s=",
        "Version": "$LATEST",
        "TracingConfig": {
            "Mode": "PassThrough"
        },
        "RevisionId": "d1aa2027-c398-49ad-bfb6-367f0b867516",
        "State": "Pending",
        "StateReason": "The function is being created.",
        "StateReasonCode": "Creating",
        "PackageType": "Zip",
        "Architectures": [
            "x86_64"
        ],
        "EphemeralStorage": {
            "Size": 512
        },
        "SnapStart": {
            "ApplyOn": "None",
            "OptimizationStatus": "Off"
        },
        "RuntimeVersionConfig": {
            "RuntimeVersionArn": "arn:aws:lambda:ap-northeast-1::runtime:5b9b2cfd05dd0cba22f79278aa11976651792d65fdc56d6bbda8271221739ad8"
        },
        "LoggingConfig": {
            "LogFormat": "Text",
            "LogGroup": "/aws/lambda/lambda-sns-publish-with-sdk"
        }
    } 
    ```    
7. You can invoke the Lambda as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name lambda-sns-publish-with-sdk \
        --log-type Tail \
        --cli-binary-format raw-in-base64-out \
        --payload file://payload.json \
        --region us-east-1 \
        --profile admin \
        outputfile.txt
    ```    
    The `payload.json` file has the following contents:
    ```json
    {
        "topicArn" : "arn:aws:sns:us-east-1:<account id>:my-first-sns-topic",
        "message": "test payload 1"
    }
    ```
    If successful, you will get notifications to the configured email and SMS.

## How it works...
In summary, we did the following in this recipe:
1. Created a Java Lambda to publish messages to a topic
2. Provisioned it and tested it from AWS CLI

## There's more...
This was a small recipe to demonstrate the use of SNS Java SDK within a Lambda. You can extend it with additional functionality as per your requirements. 

## See also
* https://docs.aws.amazon.com/sns/latest/dg/using-awssdkjava.html


