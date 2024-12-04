# Writing data into Kinesis Stream with SDK (Java)
In this recipe, we will develop an AWS Lambda function that write to KDS using AWS Java SDK for Kinesis. Kinesis producers may also be developed using the *Kinesis Producer Library (KPL)*; this is the more common option for non-serverless applications. However, with AWS Lambda, SDK-based code is generally preferred as we will be using less libraries.

## Getting ready
You will need to have the following prerequisites for this recipe:
* You need an active AWS account. You need to follow the Getting started section in the recipes [Your first AWS Lambda](../../Chapter01/your-first-aws-lambda/README.md) and [Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) from Chapter 1, Getting Started with Serverless Computing on AWS to set up Java, Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/README.md), and AWS CLI, and may also read other notes there, including code usage guidelines, S3 bucket creation, and notes for Windows users.
* Follow the recipe [Your first Kinesis data stream](../your-first-kinesis-stream/README.md) and create a Kinesis stream named `my-first-kinesis-stream`.


## How to do it...
We will learn how we can implement a Java Lambda function to write data into a Kinesis stream using AWS Java SDK for Kinesis. I will not show all the details of provisioning the Lambda; for these, you can refer to earlier recipes (as given in the Getting ready section).

### Step 1 - Creating the Lambda project (Java)
We will create a Lambda that gets triggered from AWS CLI using `aws lambda invoke` command  and send messages to an SQS queue as a batch. 

> In this section, I will be discussing only the core application logic, and will not be discussing supporting code, such as imports, error handling, and Javadoc comments; however, the complete working code is provided in this book along with the code files. 


ommitted...


### Step 2 - Provisioning and testing Lambda (AWS CLI)
You can also go through the [Your first AWS Lambda](../../Chapter01/your-first-aws-lambda/README.md) recipe in Chapter 1, Getting Started with Serverless Computing on AWS, and use CloudFormation for Lambda provisioning. Go through the following steps to deploy and invoke the `Lambda` function:
1. Run `mvn clean package` from inside the `Lambda` project root folder to create the `Uber JAR`.
2. Upload the `Uber JAR` to S3.
3. Create a role for the Lambda with an appropriate trust relationship definition.
4. Create a policy for basic logging permissions and attach it to the role.
5. Create a policy for the required Kinesis permissions and attach it to the role by going through the following steps:
    1. Create the policy document with the required Kinesis permissions using the following code:
        ```json
        {
        "Version":"2012-10-17",
        "Statement":[
            {
                "Effect":"Allow",
                "Action":[
                    "kinesis:DescribeStream",
                    "kinesis:PutRecord",
                    "kinesis:PutRecords"
                ],
                "Resource":[
                    "arn:aws:kinesis:*:*:*"
                ]
            }
        ]
        }
        ```
    2. Save the file as `lambda-kinesis-producer-permissions.txt`.
    3. Create the policy and attach it to the role.
6. Create the Lambda function as follows:
    ```bash
    aws lambda create-function \
        --function-name lambda-kinesis-sdk-write \
        --runtime java8 \
        --role arn:aws:iam::<account id>:role/lambda_kinesis_write_role \
        --handler tech.heartin.books.serverlesscookbook.LambdaKinesisSdkWriteHandler::handleRequest \
        --code S3Bucket=serverless-cookbook,S3Key=lambda-kinesis-sdk-write-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
    ```
7. Invoke the Lambda function as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name lambda-kinesis-sdk-write \
        --log-type Tail \
        --payload file://resources/payload.json \
        --region us-east-1 \
        --profile admin \
        outputfile.txt
    ```    
    The payload file should correspond to our input domain object (Request.java), as shown in the following code:
    ```json
    {
        "streamName" : "my-first-kinesis-stream",
        "partitionKey": "12345",
        "payload": "testpayloadfromcli",
        "count": 10,
        "batchSize" : 5
    }
    ```
    If the `aws lambda invoke` command is successful, you should see a success message in the output file, `outputfile.txt` (assuming you return a success message from the Lambda similar to the code files).

    Verify the invocation by retrieving the messages from the stream using the following steps:
    1. First, retrieve the iterator, as shown in the following code:
    ```bash    
    aws kinesis get-shard-iterator \
        --shard-id shardId-000000000000 \
        --shard-iterator-type TRIM_HORIZON \
        --stream-name my-first-kinesis-stream \
        --region us-east-1 \
        --profile admin
    ```    
    If successful, you should get the following message back:



    2. Get the records using the shard iterator, as shown in the following code:
        ```bash    
        aws kinesis get-records \
            --shard-iterator <shard iterator> \
            --region us-east-1 \
            --profile admin
        ```    
        Replace `<shard iterator>` with the shard iterator received in the previous step. This should return the following records:
```json
```
        I have not shown all the records here, only the first one. At the end, you will also get the next shard iterator, as shown in the following screenshot:
```json

```
        You may have to call `get-records` again with the shard iterator received in this step to retrieve further records.

8. Finally, you need to decode the Base64-encoded data using the following code:
```bash
```


## How it works...
In summary, we did the following in this recipe:
1. Created a Lambda function with basic Lambda permissions and Kinesis-specific permissions
2. Invoked Lambda with a payload as required by the input handler object (Request.java)
3. Verified that data was posted to the stream

From the Kinesis client, we retrieved `DescribeStreamResult` and from the `DescribeStreamResult`, we retrieved `StreamDescription`. The `StreamDescription` contains current status of the stream, the stream ARN, an array of shard objects of the stream, and information on whether there are more shards available. This was an optional step just to see the stream status.

The Kinesis client's `putRecords` method accepts a `PutRecordsRequest` object and the `PutRecordsRequest` object accepts a list of `PutRecordsRequestEntry` objects. We generated PutRecordsRequestEntry objects in a for loop and added them into a list. Once the list size crossed our defined batch size, we invoked the `putRecords` method of the Kinesis client and passed a `PutRecordsRequest` object with our list of `PutRecordsRequestEntry` objects.

## There's more...
In this recipe, we used AWS Java SDK for Kinesis. We can also create producers using the **Kinesis Producer Library (KPL)**. The KPL simplifies Kinesis producer application development and helps us to achieve high write throughput to a Kinesis data stream by aggregating smaller records into larger records, up to 1 MB in size. While the **Kinesis Client Library (KCL)** for Java can deaggregate records aggregated by KPL for regular applications, we need to use a special module to deaggregate records when using AWS Lambda as the consumer. 

## See also
* https://docs.aws.amazon.com/streams/latest/dev/developing-producers-with-kpl.html