# Throughput provisioning examples
Provisioned throughput is the maximum read and write capacity that an application can use within a table or index. 

If we use more than the specified RCU or WCU, DynamoDB can throttle the requests, the requests will fail with a 400 (bad request) error, and DynamoDB throws `ProvisionedThroughputExceededException`. AWS SDKs can do automatic retries in the case of a throughput exception. 

Apart from manual throughput provisioning, DynamoDB also supports features such as auto-scaling and reserved capacity. Having a decent understanding of throughput provisioning can help you configure these alternate options efficiently. We will discuss different throughput provisioning scenarios in this recipe.

## Getting ready
In this recipe, we do not directly discuss writing any commands or code. We will instead discuss examples of how to derive on a RCU or WCU based on a requirement. However, you may try them out when following the commands and code discussed in other recipes. 
## How to do it...
Let's look at some scenarios to understand RCU and WCU better.
### Scenario 1
Your application needs to perform 5 million writes and 5 million eventually consistent reads per day with item sizes of 1 KB:
```bash
Number of writes per day = 5,000,000
Number of writes per second = ceil (5,000,000 / (24 * 60 * 60))  = 58
1 WCU is required for each 1 KB of write. So writes required for each item = ceil(1) = 1
Total WCU required  = 58 x 1 = 1.
Similarly 5,000,000 eventually consistent reads per day  = 58 eventually consistent reads per second.
1 RCU is required for 2 eventually consistency reads. So RCU required for 58 eventually consistent reads  = 29
1 RCU is required for 4KB of read. So reads required for each data item = ceil(1/4) = 1
Total RCU required = 29 x 1 = 29.
```
This example is based on the pricing example given by Amazon. We will see some more scenarios to make the concept clearer. 

Please note the following:
* The ceil function returns the smallest integer greater than the passed value, for example, ceil (1.5) is 2.
* The AWS Free Tier provides 25 RCU and 25 WCU per second for a whole month. Therefore, if you set and use 58 WCU and 29 RCU for a month, you will be billed for only 33 WCU and 4 RCU.
* The AWS Free Tier provides 25 RCU and 25 WCU per second for a whole month. If you are using DynamoDB only for half a month, you can use approximately 50 WCU and 50 RCU per second. Therefore, if you set and use 58 WCU and 29 RCU for only half of a month, you will be billed for only 8 WCU and 0 RCU.

### Scenario 2
Your application needs to perform 5 million writes and 5 million strongly consistent reads per day with item sizes of 5.5 KB:
```bash
Number of writes per day = 5,000,000
Number of writes per second = ceil (5,000,000 / (24 * 60 * 60))  = 58
1 WCU is required for each 1 KB of write. So, writes required for each item = ceil(5.5) = 6
Total WCU required  = 58 x 6 = 348
Similarly 5,000,000 strongly consistent reads per day  = 58 strongly consistent reads per second.
1 RCU is required for 1 strongly consistency read. So RCU required for 58 strongly consistent reads  = 58
1 RCU is required for 4KB of read. So reads required for each data item = ceil(5.5/4) = 2
Total RCU required = 58 x 2 = 116
```
### Scenario 3
Your application needs to perform one write and one eventually consistent read per second with item sizes of 1 KB on a DynamoDB table:
```bash
Number of writes per second = 1
1 WCU is required for each 1 KB of write. So writes required for each item = ceil(1) = 1
Total WCU required  = 1 x 1 = 1;
Similarly, the number of eventually consistent reads per second = 1
1 RCU is required for 1 strongly consistency read. So, the RCU required for 1 eventually consistent read  = .5
1 RCU is required for 4 KB of read. So reads required for each data item = ceil(1/4) = 1
Total RCU required = .5 x 1 = .5.
Even though the required RCU is .5, we still have to set an RCU of 1. This is because you can only set integers greater than 0 for WCU and RCU.
```
## How it works...
Let us go through some of the throughput-provisioning related concepts in more detail. 
### Strongly consistent reads versus eventually consistent reads
DynamoDB replicates data in three different facilities within a region. When you read data from DynamoDB, by default they are eventually consistent reads and hence you may not always see the latest data. You can opt for strongly consistent reads at twice the cost of eventually consistent reads.

One RCU can be used for one strongly consistent read per second or two eventually consistent reads per second, for an item up to 4 KB in size. One WCU can be used for one write per second for an item up to 1 KB in size.

### Limits on throughput updates within a day
There is a limit on the number of times you can decrease your throughput in a day. However, there is no limit on the number of throughput increases. Exact limits may be updated by Amazon. The latest limits are available at https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html.

## There's more...
Apart from manually provisioning throughput, DynamoDB supports the following ways to manage capacity: auto-scaling and reserved capacity. With auto-scaling, you define the upper and lower limits for RCU and WCU along with a target utilization percentage within those limits. DynamoDB auto-scaling will maintain your target utilization as your application workload increases or decreases. Reserve capacity allows you to reserve minimum capacity for longer periods with a one-time upfront payment. Reserving capacity can save cost considerably.

## See also
* https://aws.amazon.com/dynamodb/pricing/
* https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.ProvisionedThroughput.html