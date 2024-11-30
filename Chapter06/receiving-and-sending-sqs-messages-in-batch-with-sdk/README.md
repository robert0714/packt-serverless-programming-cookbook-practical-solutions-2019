# Receiving and sending SQS messages in batches with SDK (Java)
In this recipe, we will create a Lambda function in Java to receive messages from an existing input SQS queue and send all the messages as a batch to another SQS output queue. We will also delete the messages from the input SQS queue. 

## Getting ready
You need to follow the section Getting started in the recipes [Your first AWS Lambda and Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) from Chapter 1, Getting Started with Serverless Computing on AWS to set up Java, Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/) and AWS CLI, and may also read other notes there including code usage guidelines, S3 bucket creation and notes for the Windows users. 

## How to do it...
We will first create our Java Lambda. Next, we will create two queues as required for this recipe and place some data into one of them. After that, we will deploy the Lambda and test it from CLI.

## Setting up queues and data
Before we can invoke our Lambda, we need to create an input and an output queue. We will also send messages to the input queue. When we invoke the Lambda, it will retrieve these messages.

Perform the following steps:
1. Create two SQS queues: an input queue, `my-input-queue`, and an output queue, `my-output-queue`, following the *Your first Simple Queue Service* (SQS) recipe.
    ```bash
    aws sqs create-queue \
        --queue-name 'my-input-queue' \
        --profile admin
    {
        "QueueUrl": "https://sqs.ap-northeast-1.amazonaws.com/937197674655/my-input-queue"
    }


    aws sqs create-queue \
        --queue-name 'my-output-queue' \
        --profile admin
    {
        "QueueUrl": "https://sqs.ap-northeast-1.amazonaws.com/937197674655/my-output-queue"
    }
    ```
2. Send six to seven messages to the queue from CLI.
    ```bash
    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-input-queue \
        --message-body 'This is test message 1' \
        --profile admin
    {
        "MD5OfMessageBody": "246ff47f61c75f9aaf1d07bcd6c0d2d4",
        "MessageId": "7ee7299d-c3e6-4a06-8279-1fc90ebb167a"
    }
    

    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-input-queue \
        --message-body 'This is test message 2' \
        --profile admin
    {
        "MD5OfMessageBody": "29ceb3b2564689646d593962604ff1e3",
        "MessageId": "8922e111-3969-4635-8b97-8f93eb156711"
    }


    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-input-queue \
        --message-body 'This is test message 3' \
        --profile admin
    {
        "MD5OfMessageBody": "a74919a3c03768a752425d32ad7beaa6",
        "MessageId": "7610c265-eff2-434d-a5d4-c3da2ca2fc75"
    }

    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-input-queue \
        --message-body 'This is test message 4' \
        --profile admin
    {
        "MD5OfMessageBody": "9e8297d7c4d5e90158bcacb7645870d6",
        "MessageId": "86eab4be-9043-48b5-b9c4-8214c1be776e"
    }

    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-input-queue \
        --message-body 'This is test message 5' \
        --profile admin
    {
        "MD5OfMessageBody": "12b71e7ae4d41fb52089474ece7684ff",
        "MessageId": "680d7ad7-e32a-4392-b2f1-016912e934aa"
    }

    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-input-queue \
        --message-body 'This is test message 6' \
        --profile admin
    {
        "MD5OfMessageBody": "721d1b00ea8d915bfaab3c0607c2c8e7",
        "MessageId": "b45f4a47-c9a4-4834-a797-59c0cad1af20"
    }

    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-input-queue \
        --message-body 'This is test message 7' \
        --profile admin
    {
        "MD5OfMessageBody": "f4803193337c4beea27cddf1ecfe9d54",
        "MessageId": "a2788035-035b-446a-81e3-0ffd293aa222"
    }
    ```

## Provisioning and testing the Lambda (AWS CLI)
Follow this steps to deploy and invoke the Lambda. You may follow Chapter 1, Getting Started with Serverless Computing on AWS and use CloudFormation for Lambda provisioning:

1. Run `mvn clean package` or `mvn clean package -Dcheckstyle.skip` from inside the Lambda project root folder to create the Uber JAR.
2. Upload the Uber JAR to `S3`:
    ```bash
    aws s3 cp \
        target/lambda-sqs-sdk-receive-send-batch-0.0.1-SNAPSHOT.jar \
        s3://dev-for-tw-robert-20241020/lambda-sqs-sdk-receive-send-batch-0.0.1-SNAPSHOT.jar \
        --profile admin
    ```    
3. Create a role for the Lambda with an appropriate trust relationship definition:
    ```bash
    aws iam create-role \
        --role-name lambda-sqs-sdk-receive-send-batch-role \
        --assume-role-policy-document file://iam-role-trust-relationship.txt \
        --profile admin
    {
        "Role": {
            "Path": "/",
            "RoleName": "lambda-sqs-sdk-receive-send-batch-role",
            "RoleId": "AROA5UNKVUCPWS5UKXLJP",
            "Arn": "arn:aws:iam::937197674655:role/lambda-sqs-sdk-receive-send-batch-role",
            "CreateDate": "2024-11-30T13:29:12+00:00",
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
4. Create a policy for basic logging permissions and attach it to the role.
    ```bash
    aws iam create-policy \
        --policy-name lambda-basic-iam-policy \
        --policy-document file://basic-lambda-permissions.txt \
        --profile admin
    {
        "Policy": {
            "PolicyName": "lambda-basic-iam-policy",
            "PolicyId": "ANPA5UNKVUCPX5Q2ITSBF",
            "Arn": "arn:aws:iam::937197674655:policy/lambda-basic-iam-policy",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2024-11-30T13:32:05+00:00",
            "UpdateDate": "2024-11-30T13:32:05+00:00"
        }
    }

    aws iam attach-role-policy \
        --role-name lambda-sqs-sdk-receive-send-batch-role \
        --policy-arn arn:aws:iam::937197674655:policy/lambda-basic-iam-policy \
        --profile admin

    aws iam create-policy \
        --policy-name lambda-sqs-sdk-receive-send-delete-policy \
        --policy-document file://lambda-sqs-receive-send-batch-permissions.txt \
        --profile admin
    {
        "Policy": {
            "PolicyName": "lambda-sqs-sdk-receive-send-delete-policy",
            "PolicyId": "ANPA5UNKVUCPQYOX7D6IE",
            "Arn": "arn:aws:iam::937197674655:policy/lambda-sqs-sdk-receive-send-delete-policy",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2024-11-30T13:32:53+00:00",
            "UpdateDate": "2024-11-30T13:32:53+00:00"
        }
    }

    aws iam attach-role-policy \
        --role-name lambda-sqs-sdk-receive-send-batch-role \
        --policy-arn arn:aws:iam::937197674655:policy/lambda-sqs-sdk-receive-send-delete-policy \
        --profile admin
    ```
5. Create a policy for required SQS permissions and attach it to the role.
    The policy document with required SQS permissions is shown as follows:
    ```json
    {
    "Version":"2012-10-17",
    "Statement":[
        {
            "Effect":"Allow",
            "Action":[
                "sqs:ReceiveMessage",
				"sqs:SendMessage",
				"sqs:SendMessage",
				"sqs:DeleteQueue",
				"sqs:DeleteMessage"
            ],
            "Resource":[
                "arn:aws:sqs:*:*:*"
            ]
        }
    ]
    }
    ```
6. Create the Lambda function, as follows:
    ```bash
    aws lambda create-function \
        --function-name lambda-sqs-sdk-receive-send-batch \
        --runtime java17 \
        --role arn:aws:iam::<account id>:role/lambda-sqs-sdk-receive-send-batch-role \
        --handler tech.heartin.books.serverlesscookbook.LambdaSqsSdkReceiveSendBatchHandler::handleRequest \
        --code S3Bucket=dev-for-tw-robert-20241020,S3Key=lambda-sqs-sdk-receive-send-batch-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin

    {
        "FunctionName": "lambda-sqs-sdk-receive-send-batch",
        "FunctionArn": "arn:aws:lambda:ap-northeast-1:937197674655:function:lambda-sqs-sdk-receive-send-batch",
        "Runtime": "java17",
        "Role": "arn:aws:iam::937197674655:role/lambda-sqs-sdk-receive-send-batch-role",
        "Handler": "tech.heartin.books.serverlesscookbook.LambdaSqsSdkReceiveSendBatchHandler::handleRequest",
        "CodeSize": 10491878,
        "Description": "",
        "Timeout": 15,
        "MemorySize": 512,
        "LastModified": "2024-11-30T14:33:10.621+0000",
        "CodeSha256": "Ep5QSqa5sRmIGj05VCWGk1boYdyDQ4rqoIvg63+SX7k=",
        "Version": "$LATEST",
        "TracingConfig": {
            "Mode": "PassThrough"
        },
        "RevisionId": "a36fa8f3-f63a-442b-85a0-26bb447c9903",
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
            "LogGroup": "/aws/lambda/lambda-sqs-sdk-receive-send-batch"
        }
    }
 
    ``` 
7. Invoke the Lambda function, as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name lambda-sqs-sdk-receive-send-batch \
        --log-type Tail \
        --cli-binary-format raw-in-base64-out \
        --payload file://payload.json \
        --region us-east-1 \
        --profile admin \
        outputfile.txt

        {
        "StatusCode": 200,
        "LogResult": "U0xGNEo6IEZhaWxlZCB0byBsb2FkIGNsYXNzICJvcmcuc2xmNGouaW1wbC5TdGF0aWNMb2dnZXJCaW5kZXIiLgpTTEY0SjogRGVmYXVsdGluZyB0byBuby1vcGVyYXRpb24gKE5PUCkgbG9nZ2VyIGltcGxlbWVudGF0aW9uClNMRjRKOiBTZWUgaHR0cDovL3d3dy5zbGY0ai5vcmcvY29kZXMuaHRtbCNTdGF0aWNMb2dnZXJCaW5kZXIgZm9yIGZ1cnRoZXIgZGV0YWlscy4KU1RBUlQgUmVxdWVzdElkOiA0NjhkZGJlYi0wMzA4LTQyZDctYjNmYy05MjEzMjk4N
    WJjNWMgVmVyc2lvbjogJExBVEVTVApSZWNlaXZlZCBSZXF1ZXN0OiBSZXF1ZXN0KGlucHV0UXVldWVVUkw9aHR0cHM6Ly9xdWV1ZS5hbWF6b25hd3MuY29tLzEyMzQ1Njc4OTAxMi9teS1pbnB1dC1xdWV1ZSwgb3V0cHV0UXVldWVVUkw9aHR0cHM6Ly9xdWV1ZS5hbWF6b25hd3MuY29tLzEyMzQ1Njc4OTAxMi9teS1vdXRwdXQtcXVldWUsIG1heE1lc3NhZ2VzVG9SZWNlaXZlPTUsIGRlbGF5PTEwKQpFcnJvciBvY2N1cnJlZDogVGhlIGFkZHJlc3MgaHR0cHM6Ly9zcXMuYXAtbm9y
    dGhlYXN0LTEuYW1hem9uYXdzLmNvbS8gaXMgbm90IHZhbGlkIGZvciB0aGlzIGVuZHBvaW50LiAoU2VydmljZTogU3FzLCBTdGF0dXMgQ29kZTogNDA0LCBSZXF1ZXN0IElEOiBkODk3NDEyMy04Zjk5LTVmZTgtOGE1Ny0xZWU2OTg4NmUxZmQpCkVORCBSZXF1ZXN0SWQ6IDQ2OGRkYmViLTAzMDgtNDJkNy1iM2ZjLTkyMTMyOTg1YmM1YwpSRVBPUlQgUmVxdWVzdElkOiA0NjhkZGJlYi0wMzA4LTQyZDctYjNmYy05MjEzMjk4NWJjNWMJRHVyYXRpb246IDI2NzQuNTQgbXMJQmlsbGV
    kIER1cmF0aW9uOiAyNjc1IG1zCU1lbW9yeSBTaXplOiA1MTIgTUIJTWF4IE1lbW9yeSBVc2VkOiAxNDkgTUIJSW5pdCBEdXJhdGlvbjogMTU2OC4yMiBtcwkK",
        "ExecutedVersion": "$LATEST"
    }
    ```    
    The payload file should correspond to our input domain object (Request.java) as follows:
    ```json
    {
        "inputQueueURL" : "https://queue.amazonaws.com/937197674655/my-input-queue",
        "outputQueueURL" : "https://queue.amazonaws.com/937197674655/my-output-queue",
        "maxMessagesToReceive" : 5,
        "delay": 10
    }
    ```
    If the `aws lambda invoke command` is successful, you should see a success message in the output file, `outputfile.txt` (assuming you return a success message from the Lambda similar to the code files).

8. Verify the invocation by retrieving the message from the queue:
    ```bash
    aws sqs receive-message \
        --queue-url https://queue.amazonaws.com/937197674655/my-output-queue \
        --max-number-of-messages 7 \
        --profile admin
    ```    
    If successful, you should get between zero and seven (maximum) messages in a single `receive-message` call. Even if you have more messages than this value in the queue, the exact number of messages returned is not guaranteed, but the maximum returned will be as per the value of the `max-number-of-messages` property.

 
## How it works...
In summary, we did the following in this recipe:
1. Created a Lambda function to perform the following:
    1. Retrieve multiple messages from an input queue
    2. Batch the messages and send it to an output queue
2. Created the input and output queues
3. Added data into the input queue
4. Created required policies and attached them to the role
5. Created the Lambda function specifying the role
6. Invoked the Lambda with a payload as required by the input handler object (`Request.java`)
7. Verified data was posted to the queue using the `aws sqs receive-message` command on the output queue    

## There's more...
We created a simple SQS queue, and messages were retrieved randomly. You can however configure the queue to act as a strict **first in, first out (FIFO)** queue using the Fifo queue property while creating the queue.

## See also
* https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-api-permissions-reference.html
* https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sqs/model/ReceiveMessageRequest.html#getMaxNumberOfMessages--