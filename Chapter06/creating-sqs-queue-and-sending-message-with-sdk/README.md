# Creating an SQS queue and sending messages with SDK (Java)
In this recipe, we will create Lambda function in Java to create an SQS queue, get the URL of the queue using the queue name, and then send a message to that queue. We will not repeat the commands or steps required to create and invoke the Lambda that was already discussed earlier. Please refer to code files or earlier Lambda recipes for the complete code.  

## Getting ready
You need to follow the section Getting started in the recipes [Your first AWS Lambda and Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) from Chapter 1, [Getting Started with Serverless Computing on AWS to set up Java](../../Chapter01/README.md), Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/) and AWS CLI, and may also read other notes there including code usage guidelines, S3 bucket creation and notes for the Windows users.


## How to do it...
We will first create our Java Lambda and then deploy and test it from the CLI.

## Provisioning and testing the Lambda (AWS CLI)
Follow these steps to deploy and invoke the Lambda. You may follow Chapter 1, [Getting Started with Serverless Computing on AWS](../../Chapter01/) and use CloudFormation for Lambda provisioning:

1. Run `mvn clean package` from inside the Lambda project root folder to create the Uber JAR.
2. Upload the Uber JAR to `S3`:
    ```bash
    aws s3 cp \
        target/lambda-sqs-sdk-create-send-0.0.1-SNAPSHOT.jar \
        s3://dev-for-tw-robert-20241020/lambda-sqs-sdk-create-send-0.0.1-SNAPSHOT.jar \
        --profile admin
    ```    
3. Create a role for the Lambda with an appropriate trust relationship definition:
    ```bash
    aws iam create-role \
        --role-name lambda-sqs-create-send-role \
        --assume-role-policy-document file://iam-role-trust-relationship.txt \
        --profile admin
    ```    
    The trust document, `iam-role-trust-relationship.txt`, is defined as follows:
    ```json
    {
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
    ```
    And its output:
    ```json
    {
        "Role": {
            "Path": "/",
            "RoleName": "lambda-sqs-create-send-role",
            "RoleId": "AROA5UNKVUCP55RU3GFZM",
            "Arn": "arn:aws:iam::937197674655:role/lambda-sqs-create-send-role",
            "CreateDate": "2024-11-30T02:06:23+00:00",
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
4. Create a policy for basic logging permissions and attach it to the role.
    Create the policy document as follows:
    ```json
    {
        "Version":"2012-10-17",
        "Statement":[
            {
                "Effect":"Allow",
                "Action":[
                    "logs:CreateLogGroup",
                    "logs:CreateLogStream",
                    "logs:PutLogEvents"
                ],
                "Resource":[
                    "arn:aws:logs:*:*:*"
                ]
            }
        ]
    }
    ```
    Save this file as `basic-lambda-permissions.txt`.

    Create the policy as follows:
    ```bash
    aws iam create-policy \
        --policy-name lambda-basic-iam-policy \
        --policy-document file://basic-lambda-permissions.txt \
        --profile admin
  
    {
        "Policy": {
            "PolicyName": "lambda-basic-iam-policy",
            "PolicyId": "ANPA5UNKVUCP2YVC743AR",
            "Arn": "arn:aws:iam::937197674655:policy/lambda-basic-iam-policy",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2024-11-30T02:08:20+00:00",
            "UpdateDate": "2024-11-30T02:08:20+00:00"
        }
    }
    ```    
    Attach the policy to the role as follows:
    ```bash
    aws iam attach-role-policy \
    --role-name lambda-sqs-create-send-role \
    --policy-arn arn:aws:iam::937197674655:policy/lambda-basic-iam-policy \
    --profile admin
    ```
5. Create a policy for required SQS permissions, and attach it to the role.
    Create the policy document with the required SQS permissions as follows:
    ```json
    {
        "Version":"2012-10-17",
        "Statement":[
            {
                "Effect":"Allow",
                "Action":[
                    "sqs:CreateQueue",
                    "sqs:GetQueueUrl",
                    "sqs:SendMessage"
                ],
                "Resource":[
                    "arn:aws:sqs:*:*:*"
                ]
            }
        ]
    }
    ```
    Save the file as lambda-sqs-create-send-permissions.txt.

    Create the policy and attach it to the role, as we did in the previous step.
    * Create it
    ```bash
    aws iam create-policy \
        --policy-name lambda-sqs-create-send-policy \
        --policy-document file://lambda-sqs-create-send-permissions.txt \
        --profile admin

    {
        "Policy": {
            "PolicyName": "lambda-sqs-create-send-policy",
            "PolicyId": "ANPA5UNKVUCPY3CHFSLSV",
            "Arn": "arn:aws:iam::937197674655:policy/lambda-sqs-create-send-policy",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2024-11-30T02:15:03+00:00",
            "UpdateDate": "2024-11-30T02:15:03+00:00"
        }
    }
    ```
    * Attach it 
      ```bash
      aws iam attach-role-policy \
        --role-name lambda-sqs-create-send-role \
        --policy-arn arn:aws:iam::937197674655:policy/lambda-sqs-create-send-policy \
        --profile admin
      ```
6. Create the Lambda function as follows:
    ```bash
    aws lambda create-function \
        --function-name lambda-sqs-create-send \
        --runtime java17 \
        --role arn:aws:iam::<account id>:role/lambda-sqs-create-send-role \
        --handler tech.heartin.books.serverlesscookbook.LambdaSqsSdkCreateSendHandler::handleRequest \
        --code S3Bucket=dev-for-tw-robert-20241020,S3Key=lambda-sqs-sdk-create-send-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin

    {
        "FunctionName": "lambda-sqs-create-send",
        "FunctionArn": "arn:aws:lambda:ap-northeast-1:937197674655:function:lambda-sqs-create-send",
        "Runtime": "java17",
        "Role": "arn:aws:iam::937197674655:role/lambda-sqs-create-send-role",
        "Handler": "tech.heartin.books.serverlesscookbook.LambdaSqsSdkCreateSendHandler::handleRequest",
        "CodeSize": 10491287,
        "Description": "",
        "Timeout": 15,
        "MemorySize": 512,
        "LastModified": "2024-11-30T02:21:34.269+0000",
        "CodeSha256": "KmLyGrAA9/M0wNv8BjQfxI7qxJAiTb+0vbcOGqEuloE=",
        "Version": "$LATEST",
        "TracingConfig": {
            "Mode": "PassThrough"
        },
        "RevisionId": "22c268d4-3f17-4c19-99e8-f8ffed0dc922",
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
            "LogGroup": "/aws/lambda/lambda-sqs-create-send"
        }
    }  
    ```    
7. Invoke the Lambda function as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name lambda-sqs-create-send \
        --log-type Tail \
        --cli-binary-format raw-in-base64-out \
        --payload file://payload.json \
        --region us-east-1 \
        --profile admin \
        outputfile.txt

    {
        "StatusCode": 200,
        "LogResult": "U0xGNEo6IEZhaWxlZCB0byBsb2FkIGNsYXNzICJvcmcuc2xmNGouaW1wbC5TdGF0aWNMb2dnZXJCaW5kZXIiLgpTTEY0SjogRGVmYXVsdGluZyB0byBuby1vcGVyYXRpb24gKE5PUCkgbG9nZ2VyIGltcGxlbWVudGF0aW9uClNMRjRKOiBTZWUgaHR0cDovL3d3dy5zbGY0ai5vcmcvY29kZXMuaHRtbCNTdGF0aWNMb2dnZXJCaW5kZXIgZm9yIGZ1cnRoZXIgZGV0YWlscy4KU1RBUlQgUmVxdWVzdElkOiAxNGI2Mjk1MC0zOWY5LTRkYWEtYTA2NC01MGRmMDA3O
    Dc5ODEgVmVyc2lvbjogJExBVEVTVApSZWNlaXZlZCBSZXF1ZXN0OiBSZXF1ZXN0KHF1ZXVlTmFtZT1jcmVhdGUtc2VuZC1kZW1vLXF1ZXVlLCBtZXNzYWdlPXRlc3QgcGF5bG9hZCAxKQpDcmVhdGVkIHF1ZXVlOiBodHRwczovL3Nxcy5hcC1ub3J0aGVhc3QtMS5hbWF6b25hd3MuY29tLzkzNzE5NzY3NDY1NS9jcmVhdGUtc2VuZC1kZW1vLXF1ZXVlCkVORCBSZXF1ZXN0SWQ6IDE0YjYyOTUwLTM5ZjktNGRhYS1hMDY0LTUwZGYwMDc4Nzk4MQpSRVBPUlQgUmVxdWVzdElkOiAxNGI2
    Mjk1MC0zOWY5LTRkYWEtYTA2NC01MGRmMDA3ODc5ODEJRHVyYXRpb246IDI5MTkuNDEgbXMJQmlsbGVkIER1cmF0aW9uOiAyOTIwIG1zCU1lbW9yeSBTaXplOiA1MTIgTUIJTWF4IE1lbW9yeSBVc2VkOiAxNTAgTUIJSW5pdCBEdXJhdGlvbjogMTY5NC40MyBtcwkK",
        "ExecutedVersion": "$LATEST"
    }
    ```    
    The payload file should correspond to our input domain object (Request.java) as follows:
    ```json
    {
        "queueName" : "create-send-demo-queue",
        "message": "test payload 1"
    }
    ```
    If the `aws lambda invoke` command is successful, you should see a success message in the output file, `outputfile.txt` (assuming you return a success message from the Lambda similar to the code files).
    ```json
     {"message":"Successfully sent message to queue: create-send-demo-queue"}
    ```
8. Verify the invocation by retrieving the message from the queue:
    ```bash
    aws sqs receive-message \
        --queue-url https://queue.amazonaws.com/937197674655/create-send-demo-queue \
        --profile admin
    ```    
    If successful, you should get the following message back:
    ```json
    {
        "Messages": [
            {
                "MessageId": "ea3333ba-a71a-4c9a-babc-9f85b418fa98",
                "ReceiptHandle": "AQEBFfisXXZy7AUvohM/1MmBRigObaMRCP9kgnJHE533CLW8yicctUnXeC5J7HQP2xAwujhVCjq91scVMfgwxD/vQLln9usQB3JyTVHqzPdV3poe0Vg7fwQi1VCnirIN/E4DdPQtqgDFjPNaN9uOVnO9acP/Xe9iW8GX0XI1bwq/YYB/d0U4gBZxndDzPp8ABah7N2JGLJ4GFBSI6Uk+7B9cnjFdioQkjZhRBKFt9G3/g4clW/2M0RSdm83IxIkknxNsHk3ceQjzdzpNXgV2F8iQEB6uNvreilowR9EWszLtmOkfnVkYNVIfJwcVuw42Ec4fzRo891PG1
    NxHle5S2BFxbH26V5aK4xl2OnL2T2lm2oeehFdboA22ZR1xG+/eGTOk/ISR60AKAqq08d3r9hutkeqo1xylHrdFFHsuEVFmJes=",
                "MD5OfBody": "0709068de6e40356e7ed36037817bacd",
                "Body": "test payload 1"
            }
        ]
    }
    ```


## How it works...
In summary, we did the following in this recipe:
1. Created a Lambda function to perform the following:
    * Create an SQS queue
    * Get the queue URL from the queue
    * Send the message to the queue
2. Created the required policies and attached them to the role
3. Invoked Lambda with a payload as required by the input handler object (`Request.java`)
4. Verified data was posted to the queue using the `aws sqs receive-message` command

## There's more...
We created a simple SQS queue and sent the message from within a Lambda. You can work on adding more features to the create queue code. Properties available for an SQS queue were listed in the previous recipe. We sent only a single message in this recipe, but you can also send multiple SQS messages together, as we will see in the next recipe.    

## See also
* https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-api-permissions-reference.html