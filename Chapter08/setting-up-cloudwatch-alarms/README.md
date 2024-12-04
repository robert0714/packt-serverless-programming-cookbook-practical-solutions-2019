# Setting up CloudWatch alarms (AWS CLI)
CloudWatch alarms enable us to initiate actions based on the state of data within our metrics. These actions may be sent to the **Simple Notification Service (SNS)**. For server-based applications that use autoscaling, CloudWatch alarms can also initiate actions to autoscaling groups. 

## Getting ready
The following are the prerequisites for this recipe:
* A working AWS account
* Access to AWS Management Console
* You should have configured AWS CLI as discussed in the recipe [Your First Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) in Chapter 1, Getting Started with Serverless Computing on AWS
* You should have created a metric with the name `FailedLogins` and namespace `ServerlessProgrammingCookbook` following the previous recipe Your first custom metric created from AWS CLI
* You should have created an SNS topic with an email subscription following the recipe [Your first SNS topic for email and SMS](../../Chapter06/your-first-sns-topic/README.md) in Chapter 6, Messaging and Notifications with SQS and SNS

## How to do it...
Let's create an alarm and simulate the conditions that will trigger it by going through the following steps:
1. We can create an alarm for a metric with the name `FailedLogins` and namespace `ServerlessProgrammingCookbook` as follows:
    ```bash
    aws cloudwatch put-metric-alarm \
        --alarm-name FailedRequestsAlarm \
        --alarm-description 'Alarm for failed login requests' \
        --metric-name 'FailedLogins' \
        --namespace 'ServerlessProgrammingCookbook' \
        --statistic 'Average' \
        --period 60 \
        --threshold 5 \
        --comparison-operator GreaterThanOrEqualToThreshold \
        --evaluation-periods 1 \
        --alarm-actions arn:aws:sns:us-east-1:<account id>:my-first-sns-topic \
        --region us-east-1 \
        --profile admin
    ```    
2. Check the current status of the alarm using the `describe-alarms` command, as follows:
    ```bash
    aws cloudwatch describe-alarms \
        --alarm-names FailedRequestsAlarm \
        --region us-east-1 \
        --profile admin
    ```    
    If we try the `describe-alarms` command immediately after creation or if we have not sent any data for the metric within the specified period (60 seconds, in this case), we get the state `INSUFFICIENT_DATA`, as shown in the following screenshot:
    ```json
    ```

3. Send some data to the metric with matching dimensions (none in this case), using the following code:
    ```bash
    aws cloudwatch put-metric-data \
        --namespace 'ServerlessProgrammingCookbook' \
        --metric-name 'FailedLogins' \
        --value 1 \
        --region us-east-1 \
        --profile admin
    ```    
    We need to wait for at least the period you mentioned (or some more time). The `describe-alarms` command output should contain the status `OK`, as shown in the following screenshot:
    ```json
    ```
4. Send data so that the average crosses the threshold (`5`, in our case) using the `put-metric-data` command. We will send a value of `10`, as shown in the following code:
    ```bash
    aws cloudwatch put-metric-data \
        --namespace 'ServerlessProgrammingCookbook' \
        --metric-name 'FailedLogins' \
        --value 10 \
        --region us-east-1 \
        --profile admin
    ```    
    > Based on the time taken to send after the previous command, you might get an average of 5.5 (an average of 10 and 1) or just 10 (if sent after 1 minute). In either case, the alarm should be triggered and the describe-alarms command output should contain the status ALARM.
    ```json
    ```
    If the SNS topic was configured correctly and we have subscribed to an email address successfully, we should get a message similar to the following: 

    =
    The email will also contain the alarm details and state change action details, as shown in the following screenshot:

    =
    The change actions details are as follows:


    When the average goes below the threshold, the alarm automatically goes back to the OK state. 

## How it works...
In this recipe, we created an alarm for a metric using the `aws cloudwatch put-metric-alarm` command. The metric may not be available at the time of the alarm's creation, in which case the alarm will remain in the state `INSUFFICIENT_DATA`.

We used the following properties of the `put-metric-alarm` sub-command:
* `metric-name` is the name of the metric with which we want to associate this alarm.
* `namespace` is the namespace of the metric.
* `statistic` is the statistic operation for the metric and can have one of the following values: SampleCount, Average, Sum, Minimum, or Maximum. To find the percentile, we need to use the `extended-statistic` property instead.
* `period` is the length, in seconds, of each time that the specified metric is evaluated. Valid values are `10`, `30`, and any multiple of `60`. We specified `60`.
* `threshold` is the value that the comparison operator option value uses for calculating whether an ALARM state has been reached. We specified a value of 5.
* `comparison-operator` specifies the comparison operator to use. We used `GreaterThanOrEqualToThreshold`.
* `evaluation-periods` is the number of periods over which the data is compared to the threshold. For example, we can set an alarm that triggers when five consecutive data points are breached. We specified a value of `1` for the alarm to be triggered when only one data point is breached.
* `alarm-actions` is the ARN of actions to execute when this alarm transitions to the `ALARM` state from any other state. 

The alarm may belong to one of the following states:
* `OK` - Denotes that the alarm has not been triggered.
* `ALARM` - Denotes that the alarm was triggered.
* `NOT_SUFFICIENT` - Denotes that there is not enough data to determine the alarm state—for example, there is no data within the time period specified by the alarm. An alarm just created will also be in this state for a little bit of time.

When an alarm goes to the `INSUFFICIENT_DATA` state immediately after the alarm creation, it will give a `StateReason` of `Unchecked: Initial alarm creation`. Once the alarm is in an `OK` or `ALARM` state and then goes to the `INSUFFICIENT_DATA` state because there is not enough data within the evaluation period, it gives a `StateReason` as `Insufficient Data: 1 datapoint was unknown`, as follows:
```json
```

## There's more...
We created a simple alarm and learned how to trigger it. Some of the important things to remember regarding CloudWatch alarms include the following:
* The alarm period should be equal to or greater than the metric frequency.
* The state of the alarm should change (for example, OK to ALARM) for the alarm to trigger an action. 
* The alarm and its actions must be in the same region.
* We can create an alarm before we create the metric. The alarm stays in the INSUFFICIENT_DATA state until the metric is available with data.

In this recipe, we created an alarm from only AWS CLI. However, if you have fully understood this recipe, then you can do the same easily with Management Console.     

## See also
https://docs.aws.amazon.com/cli/latest/reference/cloudwatch/index.html