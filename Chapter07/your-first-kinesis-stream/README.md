# Your first Kinesis data stream (AWS CLI)
In the previous chapter, we learned how we can use SQS for messaging. SQS is good for standard data transfer (messaging) within serverless microservice applications; however, applications that work on big data and data analytics demand more. KDS is a highly scalable data streaming service that is used for such use cases.

KDS consists of an ordered sequence of data records. A stream is composed of multiple shards with different unique sequences of data records. A partition key is used to group data into shards. In the following recipe, we will create a simple KDS, put data into the stream, and retrieve data from the stream, all using AWS CLI.

## Getting ready
There are no additional prerequisites for completing this recipe other than the common requirements specified in this chapter's introduction.

## How to do it...
We will first create the KDS and later test it using AWS CLI commands.

### Step 1 - Creating a Kinesis data stream
We will create the KDS using both AWS CLI commands and the CloudFormation template.

#### Using AWS CLI
We can create a KDS from the AWS CLI as follows:
```bash
aws kinesis create-stream \
    --stream-name my-first-kinesis-stream \
    --shard-count 1 \
    --profile admin
```    
This command will not return anything. You may use the aws kinesis describe-stream command to get the details of the stream: 
```bash
aws kinesis describe-stream \
    --stream-name my-first-kinesis-stream \
    --profile admin
``` 
If stream creation happened successfully, you should see the `StreamStatus` as `ACTIVE`, as shown in the following screenshot:
```json
{
    "StreamDescription": {
        "Shards": [
            {
                "ShardId": "shardId-000000000000",
                "HashKeyRange": {
                    "StartingHashKey": "0",
                    "EndingHashKey": "340282366920938463463374607431768211455"
                },
                "SequenceNumberRange": {
                    "StartingSequenceNumber": "49658358137521028184677199569407376593868925235651149826"
                }
            }
        ],
        "StreamARN": "arn:aws:kinesis:ap-northeast-1:937197674655:stream/my-first-kinesis-stream",
        "StreamName": "my-first-kinesis-stream",
        "StreamStatus": "ACTIVE",
        "RetentionPeriodHours": 24,
        "EnhancedMonitoring": [
            {
                "ShardLevelMetrics": []
            }
        ],
        "EncryptionType": "NONE",
        "KeyId": null,
        "StreamCreationTimestamp": "2024-12-05T22:22:09+08:00"
    }
}
```
You can also list the streams available using `aws kinesis list-streams`, as shown in the following code:
```bash
aws kinesis list-streams \
    --profile admin
```

This should return the following response in our case (assuming you have only one stream):
```json
{
    "StreamNames": [
        "my-first-kinesis-stream"
    ],
    "StreamSummaries": [
        {
            "StreamName": "my-first-kinesis-stream",
            "StreamARN": "arn:aws:kinesis:ap-northeast-1:937197674655:stream/my-first-kinesis-stream",
            "StreamStatus": "ACTIVE",
            "StreamModeDetails": {
                "StreamMode": "PROVISIONED"
            },
            "StreamCreationTimestamp": "2024-12-05T22:22:09+08:00"
        }
    ]
}
```
#### Using the CloudFormation template
You can create a CloudFormation template file with the following `Resource` and `Outputs` sections to create a simple KDS:
```yaml
Resources:
  KinesisStream:
    Type: AWS::Kinesis::Stream
    Properties:
      Name: my-first-kinesis-stream
      RetentionPeriodHours: 24
      ShardCount: 1

Outputs:
  KinesisStreamId:
    Value: !Ref KinesisStream
    Export:
      Name: "KinesisStreamId"
  KinesisStreamArn:
    Value: !GetAtt KinesisStream.Arn
    Export:
      Name: "KinesisStreamArn"
```      
You may also add a template version and description to the top of the template file and then execute the stack using the `aws cloudformation create-stack` command. The complete commands and the template are available with the code files.
```bash
aws cloudformation create-stack \
    --stack-name kinesisfirststack \
    --template-body file://kinesis-stream-cf-template.yml \
    --region us-east-1 \
    --profile admin
{
    "StackId": "arn:aws:cloudformation:ap-northeast-1:937197674655:stack/kinesisfirststack/fd158ae0-b314-11ef-b6c5-06d9cdf7ed8f"
}
```
If successful, the describe-stacks subcommand should return with an `Outputs` section, as shown in the following screenshot:
```bash
aws cloudformation describe-stacks \
    --stack-name kinesisfirststack \
    --region us-east-1 \
    --profile admin
{
    "Stacks": [
        {
            "StackId": "arn:aws:cloudformation:ap-northeast-1:937197674655:stack/kinesisfirststack/fd158ae0-b314-11ef-b6c5-06d9cdf7ed8f",
            "StackName": "kinesisfirststack",
            "Description": "My first Kinesis stream",
            "CreationTime": "2024-12-05T14:26:59.908000+00:00",
            "RollbackConfiguration": {},
            "StackStatus": "CREATE_COMPLETE",
            "DisableRollback": false,
            "NotificationARNs": [],
            "Outputs": [
                {
                    "OutputKey": "KinesisStreamId",
                    "OutputValue": "my-first-kinesis-stream",
                    "ExportName": "KinesisStreamId"
                },
                {
                    "OutputKey": "KinesisStreamArn",
                    "OutputValue": "arn:aws:kinesis:ap-northeast-1:937197674655:stream/my-first-kinesis-stream",
                    "ExportName": "KinesisStreamArn"
                }
            ],
            "Tags": [],
            "EnableTerminationProtection": false,
            "DriftInformation": {
                "StackDriftStatus": "NOT_CHECKED"
            }
        }
    ]
}

```
### Step 2 - Adding and retrieving data
You can add data to a KDS from the CLI using the `aws kinesis put-record` command, as follows:
```bash
aws kinesis put-record \
    --stream-name my-first-kinesis-stream \
    --partition-key 12345 \
    --data sampledata01 \
    --profile admin
```

This will return the shard ID and the sequence number of the record, as shown in the following screenshot:
```json
    {
        "ShardId": "shardId-000000000000",
        "SequenceNumber": "49658358242066921675388760897122398432411279367184842754"
    }
```


Similarly, you can add one more data item with a payload of `sampledata02`.

Retrieving data from a KDS is a two-step process:
1. Get the shard iterator:
    ```bash
    aws kinesis get-shard-iterator \
        --shard-id shardId-000000000000 \
        --shard-iterator-type TRIM_HORIZON \
        --stream-name my-first-kinesis-stream \
        --profile admin
    ```    
    This will return the following response:
    ```json
    {
        "ShardIterator": "AAAAAAAAAAFSw6L98G6JUb0ggcflg0lG1TWa5i7snldYK9ZO1EPuaINVKLIWNgc34QD5Wwb387X6TKM8Ei3e+cSaGRKIWXguXSMOEEafOoayVHRc0f/h667stBycOZdeNU305uQKCpV/qnLpecysRQGPr4ymBc/RENUJMPWM0zpINIO+9xenQDk1tIu/o3ya/9GgDtb44lm6W6GIyjxBS2ennAjA2SfYxsHQio58wqHb8q6iRDq7kxJwI9wmFQZBeUrT4tjqowI="
    }
    ```
2. Invoke the `aws kinesis get-records` command to pass the shard iterator, as shown in the following code:
    ```bash
    aws kinesis get-records \
        --shard-iterator <shard-iterator-value> \
        --profile admin
    ```
    Use the shard iterator value from the previous step. This should give the following response:
    ```json
    {
        "Records": [
            {
                "SequenceNumber": "49658358242066921675388760897122398432411279367184842754",
                "ApproximateArrivalTimestamp": "2024-12-05T22:29:22.897000+08:00",
                "Data": "sampledata01",
                "PartitionKey": "12345"
            },
            {
                "SequenceNumber": "49658358242066921675388760897159875132819372522738810882",
                "ApproximateArrivalTimestamp": "2024-12-05T22:39:00.571000+08:00",
                "Data": "sampledata02",
                "PartitionKey": "12345"
            }
        ],
        "NextShardIterator": "AAAAAAAAAAGCn790gNvQLDoAc1WFx4WMHmtbnqd6bIPXoEWEIk5jSmmPjen9ptewcq/TQBBwItU00oN8o2aOJbAUf4mHSKtPjBsVDsgCVVxOYozIGz5h1FwHtu8F968WwgAcKUcAP2N6jTIWurlEP2hh
    Kw6NPIu3yu7Lnp9moVy+2VVphD/SCo/NX1zkmOI+sE+ZcuqDuTR9QPPifr5PcTncynh5pBhpB95aYyOGkp8QZE3V+TBchc7e2jedmgmh+nN0mNtfIbA=",
        "MillisBehindLatest": 0
    }
    ```
    The `TRIM_HORIZON` option return records from the oldest record. If you try to use the `get-records` command with the next shard iterator returned by this command, you will not get any records as it has retrieved all of the records already.

    The data in the response is Base64 encoded, and so needs to be decoded. You can do a quick Google search to find an online decoder, or if you are using a Mac or a similar OS, you can also use the following command to decode the Base64-encoded string:
    ```bash
    $ echo c2FtcGxlZGF0YTAx | base64 --decode
    sampledata01

    $ echo c2FtcGxlZGF0YTAy | base64 --decode
    sampledata02
    ```
## How it works...
In summary, we did the following in this recipe:
1. Created a KDS using AWS CLI commands and the CloudFormation template
2. Added two data records into the stream
3. Retrieved the stream iterator with the shard iterator type as `TRIM_HORIZON`
4. Retrieved the data records, passing the shard iterator value

You can add data to a KDS from the CLI using the `aws kinesis put-record` command, specifying the stream name and a partition key. The partition key determines which shard a given data record belongs to. A stream is composed of many shards and each shard has a fixed capacity. Based on the data rate capacity requirements, you can increase or decrease the number of shards.

The following are some of the limitations of a Kinesis shard:
* Five transactions per second for reads 
* Total data read rate of 2 MBps
* 1,000 records per second for writes
* Data write rate of 1 MBps

Unlike adding records, retrieving records from a Kinesis stream is a two-step process:
1. You first need to retrieve the shard iterator, passing the stream name, the shard ID, and the shard iterator type.
2. Then you need to retrieve data records using the shard iterator. The shard iterator type determines how the shard iterator is used to start reading data records from the shard.

Let's look at the different shard types in detail.

### Kinesis shard iterator types
You can specify one of the following shard iterator type values while retrieving the shard iterator value:
* `AT_SEQUENCE_NUMBER`: Use this to read from the position specified by the sequence number, as follows:
    ```bash
    aws kinesis get-shard-iterator \
        --shard-id shardId-000000000000 \
        --shard-iterator-type AT_SEQUENCE_NUMBER \
        --starting-sequence-number 49658358242066921675388760897122398432411279367184842754 \
        --stream-name my-first-kinesis-stream \
        --profile admin

    {
        "ShardIterator": "AAAAAAAAAAE1x/74jOzomdLwP7anqeX4ysNfat0B3RXTDFqE/Tmm2vSArmNUL33na/2QhUPsACWq88bGX452vRYBRLigkIo2N3evxHZ6qjURQGKNkFkZ+1DXoIK2g3p2uMciBXnKvikbajHLZphGSaTr34FV
    y2NBEVVynssbAxcQJ3iFmvmqfYcc53jlOAaJV1pLRzz3ttTruwjjcP0y0/YKQk9eS8I8KrmMKVMcEskom/mLn7ivdKzQ2tIFzXhbux/bQaBUKeI="
    }    
    ```
    I have specified the sequence number of record 1. Here, the `get-records` command will return both records 1 and 2.
* `AFTER_SEQUENCE_NUMBER`: Use this to read after the position specified by the sequence number, as follows:
    ```bash
    aws kinesis get-shard-iterator \
        --shard-id shardId-000000000000 \
        --shard-iterator-type AFTER_SEQUENCE_NUMBER \
        --starting-sequence-number 49658358242066921675388760897122398432411279367184842754 \
        --stream-name my-first-kinesis-stream \
        --profile admin

    {
        "ShardIterator": "AAAAAAAAAAGhfBVLLuOhWWX4ygGaePme3QqXT/4HYIYS9ynwDvwsZF59AhOafowM594SKCeFl18kk7+gy+RBKw4tqcKQSEv+ADiz4+GmW8UCG3SYp9T0aoN9nQoDBdu+WZOyZdE9tSeW8VwR6eFtlENhmJvV
    KctuzaW/xgfOuiLHPlsUboiDYu1sTKCdHkjH0g1uVw2MT91a+dp014CjrFTNq1PDCEF0uufvJZtbkrp/C0e5DB6FQDhwt1cb1UexnRzaPe4SMuY="
    }    
    ```    
    I have again specified the sequence number of record 1, however, here the `get-records` command will return only record 2.
* `AT_TIMESTAMP`: Use this to read from the specified timestamp, as follows:
    ```bash
    aws kinesis get-shard-iterator \
        --shard-id shardId-000000000000 \
        --shard-iterator-type AT_TIMESTAMP \
        --timestamp 1544692680.484 \
        --stream-name my-first-kinesis-stream \
        --profile admin

    {
        "ShardIterator": "AAAAAAAAAAHtZx9b/1mvohCw144WwT28K9kFte+wpbKzs4Li0GjNDV455yDL6u6iGiJKe0qfQ8dcxYeK3EFCcuQ9RaiJg5cxPTUslStHNDhyXErlKy5fRgkxMMS1C5BHUzi2qoKtAqqmn/vqu/g7YN3Qvava
    Ze18Rlj4Q+/o/cGnidGVic5pVfV4E/6k9duO20kMPRkytwiFa0ReCAAkLFnCMkZJBt75l64dDVyKTKTRTYOC1LaGYrgeTEBGyilT8ZB0OhrsfXuYVyeDJtfHFPPcLBPTYs9b"
    }    
    ```    
    Provided the timestamp matches the first record, the `get-records` command will return both the records.

* `TRIM_HORIZON`: Use this to return records from the oldest record after the last commit. We already looked at how to use this in this recipe.
* `LATEST`: Use this to return the latest records that were added after the shard iterator was generated.

Let's look at how to use these types:
1. We will first get the shard iterator that specifies the shard iterator type as `LATEST`:
    ```bash
    aws kinesis get-shard-iterator \
        --shard-id shardId-000000000000 \
        --shard-iterator-type LATEST \
        --stream-name my-first-kinesis-stream \
        --profile admin
    {
        "ShardIterator": "AAAAAAAAAAE5Y2vx2xQa1qaP3xKLb6Aqn/oSYd4kCFl+tGjz0H4/fBA8jGHmy0SMxQjWlKK/H3+zn4EfSyjyegDZFS/273EpUIWUUI5//OtCrrUOS1XmRFFACgJr3kgv+uht4kGECFNGmcsVztX0XIVe1WBhctgIhW9NEUjcIOuKxRTcv4EdNYw8fuim2EDobHR9GZXKhIitXXcZSuQD0SoRf9Gvw3NkscQBCULYi9Aftel4TlR5HyLqMSsFozFwkXlDran3YE="
    }

    ```    
2. Note down the iterator value and add a new record, as shown in the following code:
    ```bash
    aws kinesis put-record \
        --stream-name my-first-kinesis-stream \
        --partition-key 12345 \
        --data sampledata03 \
        --profile admin
    {
        "ShardId": "shardId-000000000000",
        "SequenceNumber": "49658358242066921675388760897165919761917476935974256642"
    }
    ```    
3. Invoke the `aws kinesis get-records` command, passing the shard iterator received in step 1:
    ```bash
    aws kinesis get-records \
        --shard-iterator <shard-iterator-value> \
        --profile admin
    ```    
    This will return only the latest record that was added after the shard iterator was created, which is `sampledata03` (but encoded as before).

For more details, refer to the `get-shard-iterator` documentation reference link provided in the [See also](#see-also) section.

## There's more...
You can also add encryption to the stream by using the AWS **Key Management Service (KMS)**. 

If you exceed the limits when calling the `GetShardIterator` requests, it will throw a `ProvisionedThroughputExceededException`. For KDS limits, refer to the s`ervice-sizes-and-limits` documentation reference link limits in the [See also](#see-also) section.

## See also
* https://docs.aws.amazon.com/streams/latest/dev/key-concepts.html
* https://docs.aws.amazon.com/cli/latest/reference/kinesis/get-shard-iterator.html
* https://docs.aws.amazon.com/streams/latest/dev/service-sizes-and-limits.html
* https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-kinesis-stream-streamencryption.html