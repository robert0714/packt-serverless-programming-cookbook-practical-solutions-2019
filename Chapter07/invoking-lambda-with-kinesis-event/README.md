# Invoking Lambda with Kinesis events (Java)
There are different ways to manually read data from a KDS, such as using the SDK and KCL. We can also configure AWS to invoke a Lambda when records are put into a Kinesis stream. In this recipe, we will learn how to configure a lambda to be invoked when records are added to a Kinesis stream.

## Getting ready
You will need the following prerequisites to complete this recipe:
* You need an active AWS account. You need to follow the section Getting started in the recipes [Your first AWS Lambda](../../Chapter01/your-first-aws-lambda/README.md) and [Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) from Chapter 1, Getting Started with Serverless Computing on AWS to set up Java, Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/README.md)  and AWS CLI, and may also read other notes there including code usage guidelines, S3 bucket creation and notes for the Windows users.
* Follow the recipe [Your first Kinesis data stream](../your-first-kinesis-stream/README.md) and create a Kinesis stream named `kinesis-stream-for-event`.

## How to do it...
We will now learn how we can implement a Java lambda function that will be invoked when records are added to a Kinesis stream. I will not show all the details of provisioning the lambda. You can refer to earlier recipes (mentioned in the Getting ready section). 

### Step 1 - Creating a Lambda project (Java)
In the previous recipes, we used a service interface and its implementation. In this recipe, we will create a Lambda function without a service class. As discussed in the previous chapter, you can follow any of the approaches that you prefer (or that your project prefers), but the underlying code will be the same.

> In this section, I will be discussing only the core application logic, and will not be discussing supporting code, such as imports, error handling, and Java doc comments; however, the complete working code will be provided along with the code files. 

### Step 2 - Provisioning and testing Lambda (AWS CLI)
Go through the following steps to deploy and invoke the lambda. You may also follow [Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) recipe of Chapter 1, [Getting Started with Serverless Computing on AWS](../../Chapter01/README.md) and use CloudFormation for Lambda provisioning:
1. Run `mvn clean package` from inside the Lambda project root folder to create the `Uber JAR`.
2. Upload the `Uber JAR` to S3.
3. Create a role named `lambda-invoke-kinesis-event-role` for the lambda with an appropriate trust relationship definition.
4. Create and attach a policy for basic logging permissions and attach it to the role.
5. Create a policy for the required Kinesis permissions using the following policy document and attach it to the role:
    ```json
    {
    "Version":"2012-10-17",
    "Statement":[
        {
            "Effect":"Allow",
            "Action":[
                "kinesis:GetRecords",
                "kinesis:GetShardIterator",
                "kinesis:DescribeStream",
                "kinesis:ListStreams"
            ],
            "Resource":[
                "arn:aws:kinesis:*:*:*"
            ]
        }
    ]
    }
    ```
    Save the file as `lambda-kinesis-producer-permissions.txt`, create a policy using this file, and attach it to the role.

6. Create the lambda function as follows:
    ```bash
    aws lambda create-function \
        --function-name lambda-invoke-kinesis-event \
        --runtime java8 \
        --role arn:aws:iam::<account id>:role/lambda-invoke-kinesis-event-role \
        --handler tech.heartin.books.serverlesscookbook.LambdaKinesisEventHandler::handleRequest \
        --code S3Bucket=serverless-cookbook,S3Key=lambda-invoke-kinesis-event-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
    ```    
7. Create an event source mapping for invoking the lambda function, as follows:
    ```bash
    aws lambda create-event-source-mapping \
        --event-source-arn arn:aws:kinesis:us-east-1:<account id>:stream/kinesis-stream-for-event \
        --function-name lambda-invoke-kinesis-event \
        --starting-position LATEST \
        --batch-size 3 \
        --region us-east-1 \
        --profile admin
    ```    
8. Verify the invocation by sending messages to the stream. You can do this by going through the following steps:
    1. Send messages with different payload text, following this:
        ```bash        
        aws kinesis put-record \
            --stream-name kinesis-stream-for-event \
            --partition-key 12345 \
            --data sampledata01 \
            --region us-east-1 \
            --profile admin
        ```    
    2. Check the CloudWatch logs.   
        We can check CloudWatch logs from Management console as follows:
        1. Log in to the Management console and go to the Lambda service.
        2. Click on your Lambda to see its configuration.
        3. Click on the Monitoring tab.
        4. Click on View logs in CloudWatch.Click on a Log Stream and check the logs.
        5. You should see logs similar to those shown in the following screenshot:

## How it works...
In summary, we did the following in this recipe:
    1. Created a lambda that can be invoked when records are added to a Kinesis stream.
    2. Added an event source mapping for invoking the lambda when records are added to a Kinesis stream.
    3. Checked the `CloudWatch` logs. We will learn more about CloudWatch in the next chapter.

Event source mapping creates a mapping between an event source and an AWS lambda function. The lambda's handler function is then triggered by events on the event source. In our case, we created an event source mapping for our Lambda with a Kinesis event type.

The following are the three event source types that are currently supported:
    * Amazon Kinesis
    * Amazon SQS
    * Amazon DynamoDB

We create all lambda triggers in a similar way to how we did it from UI. However, from CLI, we do this differently for different services. For example, Kinesis, SQS, and DynamoDB triggers are added using event source mapping, but for services such as API Gateway and Alexa Skills, triggers are defined when we use the `add-permission` subcommand of the lambda CLI command to add a permission policy, and for SNS, the lambda function is subscribed to the SNS topic using the `aws sns subscribe` command.

## There's more...
We invoked the lambda function using triggers in this recipe. You can also use the AWS Kinesis SDK or the KCL to read from a Kinesis stream. However, a lambda trigger is the most common way to read from a stream in serverless applications. Refer to the See also section to read more about SDK and KCL approaches.

## See also
You may read more about developing consumers using SDK at https://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-sdk.html.

You may read more about developing consumers with KCL at https://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-kcl.html. 