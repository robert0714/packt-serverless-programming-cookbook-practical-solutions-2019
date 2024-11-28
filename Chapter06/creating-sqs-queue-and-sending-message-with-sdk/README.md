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
        s3://serverless-cookbook/lambda-sqs-sdk-create-send-0.0.1-SNAPSHOT.jar \
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
    ```    
    Attach the policy to the role as follows:
    ```bash
    aws iam attach-role-policy \
    --role-name lambda-sqs-create-send-role \
    --policy-arn arn:aws:iam::855923912133:policy/lambda-basic-iam-policy \
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
                    sqs:CreateQueue,
                    sqs:GetQueueUrl,
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

6. Create the Lambda function as follows:
    ```bash
    aws lambda create-function \
        --function-name lambda-sqs-create-send \
        --runtime java8 \
        --role arn:aws:iam::<account id>:role/lambda-sqs-create-send-role \
        --handler tech.heartin.books.serverlesscookbook.LambdaSqsSdkCreateSendHandler::handleRequest \
        --code S3Bucket=serverless-cookbook,S3Key=lambda-sqs-sdk-create-send-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
    ```    
7. Invoke the Lambda function as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name lambda-sqs-create-send \
        --log-type Tail \
        --payload file://payload.json \
        --region us-east-1 \
        --profile admin \
        outputfile.txt
    ```    
    The payload file should correspond to our input domain object (Request.java) as follows:
    ```json
    {
        "queueName" : "create-send-demo-queue",
        "message": "test payload 1"
    }
    ```
    If the `aws lambda invoke` command is successful, you should see a success message in the output file, `outputfile.txt` (assuming you return a success message from the Lambda similar to the code files).
8. Verify the invocation by retrieving the message from the queue:
    ```bash
    aws sqs receive-message \
        --queue-url https://queue.amazonaws.com/855923912133/create-send-demo-queue \
        --profile admin
    ```    
    If successful, you should get the following message back:


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