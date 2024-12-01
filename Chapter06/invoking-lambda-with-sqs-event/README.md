# Invoking the Lambda with an SQS event (Java)
Until now (in this recipe and recipes from previous chapters), we were invoking a Lambda either directly from the command line or from the API gateway. A Lambda can also be invoked (or triggered) as a response to an event (or trigger) from one of the supported event sources, such as SQS, Kinesis, DynamoDB, and so on. 

In this recipe, we will invoke a Lambda with a trigger from an SQS event source. Similar to the previous recipe, we will then send all the messages as a batch to another SQS output queue. This way, we can easily verify the Lambda was triggered successfully from the other queue. You can also simply check the CloudWatch logs to verify this instead.

## Getting ready
You need to follow the section Getting started in the recipes [Your first AWS Lambda](../../Chapter01/your-first-aws-lambda/README.md) and [Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) from Chapter 1, Getting Started with Serverless Computing on AWS to set up Java, Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/README.md) and AWS CLI, and may also read other notes there including code usage guidelines, S3 bucket creation and notes for the Windows users. 


## How to do it...
We will first create our Java Lambda. Next, we will create two queues as required for this recipe and set up some data to one of them. After that, we will deploy the Lambda and test it from CLI.

### Setting up queues and data
Before we can invoke our Lambda, we need to create an input and an output queue. We will also send messages into the input queue. When we invoke the Lambda, it will retrieve these messages.

Perform the following:
1. Create two SQS queues: an input queue, `my-input-queue`, and an output queue, `my-output-queue`, following the [Your first SQS queue](../your-first-sqs-queue/README.md) recipe
2. Send six to seven messages into the queue from CLI


### Provisioning the Lambda (AWS CLI)
Follow these steps to deploy and invoke the Lambda. You may follow Chapter 1, [Getting Started with Serverless Computing on AWS](../../Chapter01/README.md) and use CloudFormation for Lambda provisioning:

1. Run `mvn clean package` from inside the Lambda project root folder to create the Uber JAR
2. Upload the Uber JAR to `S3`:
    ```bash
    aws s3 cp \
        target/lambda-invoke-sqs-event-0.0.1-SNAPSHOT.jar \
        s3://serverless-cookbook/lambda-invoke-sqs-event-0.0.1-SNAPSHOT.jar \
        --profile admin
    ```    
3. Create a role for the Lambda with an appropriate trust relationship definition:
    ```bash
    aws iam create-role \
        --role-name lambda-invoke-sqs-event-role \
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
4. Create a policy for basic logging permissions and attach it to the role
5. Create a policy for required SQS permissions and attach it to the role
    The policy document with required SQS permissions is shown here:
    ```json
    {
    "Version":"2012-10-17",
    "Statement":[
        {
            "Effect":"Allow",
            "Action":[
                "sqs:GetQueueAttributes",
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:SendMessage",
                "sqs:SendMessageBatch"
            ],
            "Resource":[
                "arn:aws:sqs:*:*:*"
            ]
        }
    ]
    }
    ```
    A Lambda configured to be invoked by an SQS even source should have the following permissions:
    * sqs:GetQueueAttributes
    * sqs:ReceiveMessage
    * sqs:DeleteMessage
    I have also added the send message permissions, as we will be forwarding the messages to another queue

6. Create the Lambda function, as shown here:
    ```bash
    aws lambda create-function \
        --function-name lambda-invoke-sqs-event \
        --runtime java8 \
        --role arn:aws:iam::855923912133:role/lambda-invoke-sqs-event-role \
        --handler tech.heartin.books.serverlesscookbook.LambdaSqsEventHandler::handleRequest \
        --code S3Bucket=serverless-cookbook,S3Key=lambda-invoke-sqs-event-0.0.1-SNAPSHOT.jar \
        --environment Variables={SPC_OUTPUT_QUEUE_URL='https://queue.amazonaws.com/855923912133/my-output-queue'} \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
    ```    
7. Configure an SQS event source for the Lambda:
    ```bash
    aws lambda create-event-source-mapping \
        --event-source-arn arn:aws:sqs:us-east-1:855923912133:my-input-queue \
        --function-name lambda-invoke-sqs-event \
        --batch-size 4 \
        --profile admin
    ```    
    The `batch-size` parameter specifies the maximum number of messages to be retrieved from the queue together

 
### Testing the Lambda (AWS CLI)
1. Send five messages to the input queue.
2. Verify the invocation by retrieving the message from the queue:
    ```bash
    aws sqs receive-message \
        --queue-url https://queue.amazonaws.com/855923912133/my-output-queue \
        --max-number-of-messages 5 \
        --profile admin
    ```    
    If successful, you should get zero to five (maximum) messages in a single `receive-message` call. You may also check CloudWatch logs and verify the logs we printed.    

## How it works...
In summary, we did the following in this recipe:
1. Created a Lambda function to perform the following:
    1. Retrieve multiple messages from an input SQS event
    2. Batch the messages and send them to an output queue
2. Created the input and output queues
3. Created the required policies and attached them to the role
4. Created the Lambda function specifying the role
5. Added data into the input queue
6. Verified data was posted to the queue using the `aws sqs receive-message` command on the output queue    

## There's more...
We created an SQS queue event in this recipe using the `aws lambda create-event-source-mapping` command. We can also use this command to define the following event sources: Kinesis Data Streams and DynamoDB streams.

AWS supports the following triggers for Lambda:
* S3
* DynamoDB
* Kinesis Data Streams
* SNS
* SES
* SQS
* Cognito
* CloudFormation
* CloudWatch Logs
* CloudWatch Events
* CodeCommit
* Scheduled Events powered by CloudWatch Events
* AWS Config
* Alexa
* Lex
* API gateway
* AWS IoT Button
* CloudFront
* Kinesis Data Firehose 

## See also
* https://docs.aws.amazon.com/lambda/latest/dg/invoking-lambda-function.html