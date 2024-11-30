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
2. Send six to seven messages to the queue from CLI.

## Provisioning and testing the Lambda (AWS CLI)
Follow this steps to deploy and invoke the Lambda. You may follow Chapter 1, Getting Started with Serverless Computing on AWS and use CloudFormation for Lambda provisioning:

1. Run `mvn clean package` from inside the Lambda project root folder to create the Uber JAR.
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
                "sqs:SendMessageBatch",
                "sqs:DeleteQueue"
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
    --runtime java8 \
    --role arn:aws:iam::<account id>:role/lambda-sqs-sdk-receive-send-batch-role \
    --handler tech.heartin.books.serverlesscookbook.LambdaSqsSdkReceiveSendBatchHandler::handleRequest \
    --code S3Bucket=serverless-cookbook,S3Key=lambda-sqs-sdk-receive-send-batch-0.0.1-SNAPSHOT.jar \
    --timeout 15 \
    --memory-size 512 \
    --region us-east-1 \
    --profile admin
    ``` 
7. Invoke the Lambda function, as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name lambda-sqs-sdk-receive-send-batch \
        --log-type Tail \
        --payload file://payload.json \
        --region us-east-1 \
        --profile admin \
        outputfile.txt
    ```    
    The payload file should correspond to our input domain object (Request.java) as follows:
    ```json
    {
        "inputQueueURL" : "https://queue.amazonaws.com/855923912133/my-input-queue",
        "outputQueueURL" : "https://queue.amazonaws.com/855923912133/my-output-queue",
        "maxMessagesToReceive" : 5,
        "delay": 10
    }
    ```
    If the `aws lambda invoke command` is successful, you should see a success message in the output file, `outputfile.txt` (assuming you return a success message from the Lambda similar to the code files).

8. Verify the invocation by retrieving the message from the queue:
    ```bash
    aws sqs receive-message \
        --queue-url https://queue.amazonaws.com/855923912133/my-output-queue \
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