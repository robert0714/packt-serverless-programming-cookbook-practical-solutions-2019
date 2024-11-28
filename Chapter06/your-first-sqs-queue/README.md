# Your first SQS queue (AWS CLI + CloudFormation)
Amazon **Simple Queue Service (SQS)** is a fully managed messaging queue service in AWS that can be used with serverless as well as non-serverless microservices and distributed systems. In this recipe, we will create an SQS queue and use the queue to transfer data.

## Getting ready
There are no additional prerequisites for completing this recipe, other than the common requirements specified in the chapter introduction.

## How to do it...
We will first create the SQS queue and later test it using AWS CLI commands.

### Creating an SQS queue
We will create an SQS queue first using CLI commands, and then using a CloudFormation template.

#### AWS CLI commands
You can create a simple SQS queue with defaults from AWS CLI as follows:
```bash
aws sqs create-queue \
 --queue-name 'my-first-queue' \
 --profile admin
```
If successful, this will give the output shown here:
```json
{
    "QueueUrl": "https://sqs.ap-northeast-1.amazonaws.com/937197674655/my-first-queue"
}

```
#### The CloudFormation template
You can create a CloudFormation template file with the following Resources and Output sections to create a simple SQS queue with defaults:
```yaml
Resources:
  SQSQueue:
    Type: AWS::SQS::Queue
    Properties:
      QueueName: my-first-sqs-queue-cf

Output:
  SQSQueueURL:
    Value: !Ref SQSQueue
    Export:
      Name: "SQSQueueURL"
  SQSQueueArn:
    Value: !GetAtt SQSQueue.Arn
    Export:
      Name: "SQSQueueArn"
```      
You may also add a template version and description.

If stack creation (run using `aws cloudformation create-stack`) is successful, the `describe` command (run using `aws cloudformation describe-stacks`) will return a response with the `Output` section, as follows:
```json
            "Outputs": [
                {
                    "OutputKey": "SQSQueueArn",
                    "OutputValue": "arn:aws:sqs:ap-northeast-1:937197674655:my-first-sqs-queue-cf",
                    "ExportName": "SQSQueueArn"
                },
                {
                    "OutputKey": "SQSQueueURL",
                    "OutputValue": "https://sqs.ap-northeast-1.amazonaws.com/937197674655/my-first-sqs-queue-cf",
                    "ExportName": "SQSQueueURL"
                }
            ],

```
### Sending and receiving data (AWS CLI)
1. We can send data to an AWS queue from the command line, as follows:
    ```bash
    aws sqs send-message \
        --queue-url https://queue.amazonaws.com/<account id>/my-first-queue \
        --message-body 'This is a test message' \
        --profile admin
    ```    
    This command will return a response, as follows:
    ```json
    {
        "MD5OfMessageBody": "fafb00f5732ab283681e124bf8747ed1",
        "MessageId": "8504a6f2-e700-406e-855e-86c975c79ad9"
    }
    ```

2. We can get data from an AWS queue from the command line, as follows:
    ```bash
    aws sqs receive-message \
        --queue-url https://queue.amazonaws.com/<account id>/my-first-queue \
        --profile admin
    ```    
    This command will return a response, as follows:
    ```json
    {
        "Messages": [
            {
                "MessageId": "8504a6f2-e700-406e-855e-86c975c79ad9",
                "ReceiptHandle": "AQEBo0UYfE969gVRpPBpEZrMBtAqv9obpTrnXldrkufdCYJhfwy4O4CT9k5VQVvv4XiiJs4IEl6M8ZYevs/2I4JVBTFbXrh5/7f10O12MEoFFpsg1x0z+zwJ0fyXDBkicTmUdg0H5K5y0brIkSlq+eFSVuniNJUBmYUBm7Vxi/+N4AFnwXiFItfXnJByCheknn5LcjTVLG1In1a2UaE1TSPKYxEiZKwG2XW6zx+jqVUIOkWtwC4HG4lGgZZMfPC1LCsmKuKx70GpnBPo6eijDezOLWWm4U4hXH/2DsfdnGv+V+3xmnG+xATn0enQfjbkYMcb+ejtpVhV6
    nQkQ070nFTIJl5d25j7/e8Jh4TVcwCKKlpSCEA2O7XOD/V5vz2dlFa6l8zyjDyJFfmvVRtoSXCIAA==",
                "MD5OfBody": "fafb00f5732ab283681e124bf8747ed1",
                "Body": "This is a test message"
            }
        ]
    }
    ```

## How it works...
In summary, we did the following in this recipe:
1. We created a queue using an AWS CLI command
2. We created a queue using a CloudFormation template
3. We sent a message to the queue from the AWS CLI
4. We retrieved a message from the queue from the AWS CLI

Both `send` and `receive` message commands returned the following properties:
* `Message id`: Message ID of the message
* `MD5ofBody`: This is the MD5 digest that can be used to verify when SQS received the message correctly

The `receive` message command also returned the following properties:
* `Body`: Body of the message.
* `MD5ofBody`: MD5 digest of the body.
* `ReceiptHandle`: There is a `ReceiptHandle` value associated with each instance of receiving a message. Every time you receive a message, the `ReceiptHandle` value will be different. To delete a message from the queue, you need to provide the latest `ReceiptHandle` value received.

## There's more...
In this recipe, we created a simple queue with defaults. It has the following properties:
* `ContentBasedDeduplication`: Boolean
* `DelaySeconds`: Integer
* `FifoQueue`: Boolean
* `KmsMasterKeyId`: String
* `KmsDataKeyReusePeriodSeconds`: Integer
* `MaximumMessageSize`: Integer
* `MessageRetentionPeriod`: Integer
* `QueueName`: String
* `ReceiveMessageWaitTimeSeconds`: Integer
* `RedrivePolicy`: RedrivePolicy object
* `Tags`: Resource tag
* `VisibilityTimeout`: Integer

## See also
https://docs.aws.amazon.com/cli/latest/reference/sqs/index.html#cli-aws-sqs
https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-sqs-queues.html
http://www.faqs.org/rfcs/rfc1321.html