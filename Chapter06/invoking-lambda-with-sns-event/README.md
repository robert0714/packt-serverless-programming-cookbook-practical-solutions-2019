# Invoking a Lambda with SNS events (Java)
In a previous recipe, we invoked a Lambda with a trigger from an SQS event source, and we configured SQS as an event source for the Lambda. With SNS, instead of defining an event source, Lambda has to subscribe to an SNS topic. Lambda will write the message received from the SNS topic into another queue, and we will verify the output queue after publishing messages to the topic. 

## Getting ready
You need to follow the section Getting started in the recipes [Your first AWS Lambda and Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/) from Chapter 1, Getting Started with Serverless Computing on AWS to set up Java, Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/README.md) and AWS CLI, and may also read other notes there including code usage guidelines, S3 bucket creation and notes for the Windows users. 


## How to do it...
We will first create our Java Lambda. We will deploy the Lambda, subscribe the Lambda to the SNS topic, and test it from CLI.

### Provisioning the Lambda (AWS CLI)
Follow these steps to deploy and invoke the Lambda:
1. Run `mvn clean package` from inside the Lambda project root folder to create the Uber JAR.
2. Upload the Uber JAR to `S3`:
    ```bash
    aws s3 cp \
        target/lambda-invoke-sns-event-0.0.1-SNAPSHOT.jar \
        s3://serverless-cookbook/lambda-invoke-sns-event-0.0.1-SNAPSHOT.jar \
        --profile admin
    ```    
3. Create a role for the Lambda with an appropriate trust relationship definition:
    ```bash
    aws iam create-role \
        --role-name lambda-invoke-sns-event-role \
        --assume-role-policy-document file://iam-role-trust-relationship.txt \
        --profile admin
    ```    
    The trust document, `iam-role-trust-relationship.txt`, is defined in previous recipes. You can also refer to the code files.

4. Create a policy for basic logging permissions and attach it to the role.
5. Create a policy for required SQS permissions and attach it to the role.
    The policy document with required SQS permissions is shown here:
    ```json
    {
    "Version":"2012-10-17",
    "Statement":[
            {
                "Effect":"Allow",
                "Action":[
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
    These permissions are required since we are writing the messages received to the queue again, however if you are not using a queue, you will not need it.

6. Create the Lambda function as shown here:
    ```bash
    aws lambda create-function \
        --function-name lambda-invoke-sns-event \
        --runtime java8 \
        --role arn:aws:iam::<account id>:role/lambda-invoke-sns-event-role \
        --handler tech.heartin.books.serverlesscookbook.LambdaSnsEventHandler::handleRequest \
        --code S3Bucket=serverless-cookbook,S3Key=lambda-invoke-sns-event-0.0.1-SNAPSHOT.jar \
        --environment Variables={SPC_OUTPUT_QUEUE_URL='https://queue.amazonaws.com/855923912133/my-output-queue'} \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
    ```    
7. Subscribe the Lambda to the queue:
    ```bash
    aws sns subscribe --topic-arn arn:aws:sns:us-east-1:<account id>:lambda-invoke-sns-topic \
    --protocol lambda \
    --notification-endpoint arn:aws:lambda:us-east-1:<account id>:function:lambda-invoke-sns-event \
    --profile admin
    ``` 
### Testing the Lambda (AWS CLI)
We will now test the Lambda created in the previous section:
1. Send a messages to the topic.
2. Verify the invocation by retrieving the message from the output queue:
    ```bash
    aws sqs receive-message \
        --queue-url https://queue.amazonaws.com/<account id>/my-output-queue \
        --max-number-of-messages 5 \
        --profile admin
    ```    
    If successful, you should get the message that you posted to the topic. You can also verify the invocation details from CloudWatch logs.    

## How it works...
In summary, we did the following in this recipe:
1. Created a Lambda function to perform the following:
    1. Retrieve messages from an input SNS event
    2. Batch the messages and send them to an output queue
2. Created the required policies and attached them to the role
3. Created the Lambda function, specifying the role
4. Added data into the topic
5. Verified that data was posted to the queue using the aws sqs receive-message command on the output queue    

## There's more...
We read a message from the topic and wrote it to an SQS queue. We can also configure an SQS queue with an SNS topic. One pattern that uses this combination is usually referred to as the fanout pattern. SNS can fanout messages to various SQS queues for various reasons, including parallel processing.

## See also
* https://aws.amazon.com/blogs/compute/messaging-fanout-pattern-for-serverless-architectures-using-amazon-sns/