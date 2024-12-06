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
    ```bash
    aws iam create-role \
        --role-name lambda_kinesis_write_role \
        --assume-role-policy-document file://iam-role-trust-relationship.txt \
        --profile admin
    {
        "Role": {
            "Path": "/",
            "RoleName": "lambda_kinesis_write_role",
            "RoleId": "AROA5UNKVUCP7CMQZJWYL",
            "Arn": "arn:aws:iam::937197674655:role/lambda_kinesis_write_role",
            "CreateDate": "2024-12-06T02:17:31+00:00",
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
    ```bash
    aws iam create-policy \
        --policy-name lambda_iam_basic_policy \
        --policy-document file://basic-lambda-permissions.txt \
        --profile admin
    {
        "Policy": {
            "PolicyName": "lambda_iam_basic_policy",
            "PolicyId": "ANPA5UNKVUCPRWDP3QBCI",
            "Arn": "arn:aws:iam::937197674655:policy/lambda_iam_basic_policy",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2024-12-06T02:19:11+00:00",
            "UpdateDate": "2024-12-06T02:19:11+00:00"
        }
    }
    aws iam attach-role-policy \
        --role-name lambda_kinesis_write_role \
        --policy-arn arn:aws:iam::937197674655:policy/lambda_iam_basic_policy \
        --profile admin
    ```
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
        ```bash
        aws iam create-policy \
            --policy-name lambda_kinesis_producer_policy \
            --policy-document file://lambda-kinesis-producer-permissions.txt \
            --profile admin
        {
            "Policy": {
                "PolicyName": "lambda_kinesis_producer_policy",
                "PolicyId": "ANPA5UNKVUCPZ2AORYVGK",
                "Arn": "arn:aws:iam::937197674655:policy/lambda_kinesis_producer_policy",
                "Path": "/",
                "DefaultVersionId": "v1",
                "AttachmentCount": 0,
                "PermissionsBoundaryUsageCount": 0,
                "IsAttachable": true,
                "CreateDate": "2024-12-06T02:21:05+00:00",
                "UpdateDate": "2024-12-06T02:21:05+00:00"
            }
        }

        aws iam attach-role-policy \
            --role-name lambda_kinesis_write_role \
            --policy-arn arn:aws:iam::937197674655:policy/lambda_kinesis_producer_policy \
            --profile admin
        ```
6. Create the Lambda function as follows:
    ```bash
    aws lambda create-function \
        --function-name lambda-kinesis-sdk-write \
        --runtime java17 \
        --role arn:aws:iam::<account id>:role/lambda_kinesis_write_role \
        --handler tech.heartin.books.serverlesscookbook.LambdaKinesisSdkWriteHandler::handleRequest \
        --code S3Bucket=dev-for-tw-robert-20241020,S3Key=lambda-kinesis-sdk-write-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
        {
            "FunctionName": "lambda-kinesis-sdk-write",
            "FunctionArn": "arn:aws:lambda:ap-northeast-1:937197674655:function:lambda-kinesis-sdk-write",
            "Runtime": "java17",
            "Role": "arn:aws:iam::937197674655:role/lambda_kinesis_write_role",
            "Handler": "tech.heartin.books.serverlesscookbook.LambdaKinesisSdkWriteHandler::handleRequest",
            "CodeSize": 10788662,
            "Description": "",
            "Timeout": 15,
            "MemorySize": 512,
            "LastModified": "2024-12-06T02:25:24.969+0000",
            "CodeSha256": "2Ew6xPlakTciBHHYlxqW2iSiM2lFYbgAV8memizIve0=",
            "Version": "$LATEST",
            "TracingConfig": {
                "Mode": "PassThrough"
            },
            "RevisionId": "19046b85-b8c3-47f6-9e15-937b57b70c7c",
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
                "LogGroup": "/aws/lambda/lambda-kinesis-sdk-write"
            }
        }
    ```
7. Invoke the Lambda function as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name lambda-kinesis-sdk-write \
        --log-type Tail \
        --cli-binary-format raw-in-base64-out \
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
        ```json
        {
            "ShardIterator": "AAAAAAAAAAGEY3NgtcguI1U1Eo84DJQeNXt7CPVN6TjIpKuNdLnKs6j8IUAZdgzdnabo5cIlI6DjMXB/URVukh1ys+MrKny2wIIi0+/26oyu0p1HiC1VqkSjGwHt2p/EUY20CBxnrROPOiB4F7/NWQ1yJVTCe1XZLwyrwq0xCL3WZttttYKHXIei5O5ASastCW5DAt1zdjTytsmHs4jyEsdfOdEEpV1cYwR0we2xArX2YUHSOiWaHI4U282qLCISWJ87A/O7iA="
        }
        ```
    2. Get the records using the shard iterator, as shown in the following code:
        ```bash    
        aws kinesis get-records \
            --shard-iterator <shard iterator> \
            --region us-east-1 \
            --profile admin
        ```    
        Replace `<shard iterator>` with the shard iterator received in the previous step. This should return the following records:
        ```bash
        $ aws kinesis get-records --shard-iterator "AAAAAAAAAAGEY3NgtcguI1U1Eo84DJQeNXt7CPVN6TjIpKuNdLnKs6j8IUAZdgzdnabo5cIlI6DjMXB/URVukh1ys+MrKny2wIIi0+/26oyu0p1HiC1VqkSjGwHt2p/EUY20CBxnrROPOiB4F7/NWQ+1yJVTCe1XZLwyrwq0xCL3WZttttYKHXIei5O5ASastCW5DAt1zdjTytsmHs4jyEsdfOdEEpV1cYwR0we2xArX2YUHSOiWaHI4U282qLCISWJ87A/O7iA="

        {
            "Records": [
                {
                    "SequenceNumber": "49658376332431426723430748919431374766221064337514037250",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.716000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpMQ==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919432583692040678966688743426",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.719000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpMg==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919433792617860293595863449602",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.719000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpMw==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919435001543679908225038155778",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.719000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpNA==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919436210469499522854212861954",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.719000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpNQ==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919457971134252586179357573122",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.778000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpNg==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919459180060072200808532279298",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.781000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpNw==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919460388985891815437706985474",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.781000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpOA==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919461597911711430066881691650",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.781000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpOQ==",
                    "PartitionKey": "12345"
                },
                {
                    "SequenceNumber": "49658376332431426723430748919462806837531044696056397826",
                    "ApproximateArrivalTimestamp": "2024-12-06T12:32:42.781000+08:00",
                    "Data": "dGVzdHBheWxvYWRmcm9tY2xpMTA=",
                    "PartitionKey": "12345"
                }
            ],
            "NextShardIterator": "AAAAAAAAAAGKy0mRvEWaJvfaONcdUqcibf0V37CmFlMqlJKqnpy38r53j62huzNZuYvmDdaPKtucgBOZrwXAP/S43AUxyoCq1wI5iikbRTzDif5bfRoVG7Wpllg383nn35dAzsH1hTinvZNQ/CsiGaFV2iAR07CHWSNasJs2fff5EErqk/I4+Qr/80T2wdP2H6JSQ/OWtnug+WINXQgvIj1qZzeDnPy/vlXN1pD4xjlkC4iyGHxLB
        ZP3l1vu44s8wF90Np/k934=",
            "MillisBehindLatest": 0
        }
        ```
        I have not shown all the records here, only the first one. At the end, you will also get the next shard iterator, as shown in the following screenshot:
        ```json
           ],
            "NextShardIterator": "AAAAAAAAAAGKy0mRvEWaJvfaONcdUqcibf0V37CmFlMqlJKqnpy38r53j62huzNZuYvmDdaPKtucgBOZrwXAP/S43AUxyoCq1wI5iikbRTzDif5bfRoVG7Wpllg383nn35dAzsH1hTinvZNQ/CsiGaFV2iAR07CHWSNasJs2fff5EErqk/I4+Qr/80T2wdP2H6JSQ/OWtnug+WINXQgvIj1qZzeDnPy/vlXN1pD4xjlkC4iyGHxLB
        ZP3l1vu44s8wF90Np/k934=",
            "MillisBehindLatest": 0
        }
        ```
        You may have to call `get-records` again with the shard iterator received in this step to retrieve further records.

8. Finally, you need to decode the Base64-encoded data using the following code:
    ```bash
    $ echo dGVzdHBheWxvYWRmcm9tY2xpMQ== |base64 --decode
    testpayloadfromcli1
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