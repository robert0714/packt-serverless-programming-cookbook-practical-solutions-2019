# CloudWatch alarms with dimensions (AWS CLI)
In the previous recipe, we created a CloudWatch alarm for a metric without any dimensions. In this small recipe, we will learn how to create a CloudWatch alarm for a metric with dimensions, and we will then verify the alarm by sending some data with dimensions.

## Getting ready
The following are the prerequisites for this recipe:
* A working AWS account.
* Access to AWS Management Console.
* You should have configured AWS CLI as discussed in the recipe Your First Lambda with AWS CLI in Chapter 1, Getting Started with Serverless Computing on AWS.
* You should have created a metric with the name `FailedLogins` and namespace ServerlessPr`ogrammingCookbook following the previous recipe Your first custom metric created from AWS CLI.
* You should have created an SNS topic with an email subscription following the recipe Your first SNS topic for email and SMS in Chapter 6, Messaging and Notifications with SQS and SNS.

## How to do it...
Let's create an alarm and simulate conditions that will trigger it:
1. We can create an alarm for a metric with the name `FailedLogins` and namespace `ServerlessProgrammingCookbook` as follows:
    ```bash
    aws cloudwatch put-metric-alarm \
        --alarm-name FailedRequestsAlarmWithDimensions \
        --alarm-description 'Alarm for failed login requests' \
        --metric-name 'FailedLogins' \
        --namespace 'ServerlessProgrammingCookbook' \
        --statistic 'Average' \
        --period 60 \
        --threshold 5 \
        --comparison-operator GreaterThanOrEqualToThreshold \
        --evaluation-periods 1 \
        --dimensions Name=Device,Value=Laptop \
        --alarm-actions arn:aws:sns:us-east-1:<account id>:my-first-sns-topic \
        --region us-east-1 \
        --profile admin
    ```    
2. Check the current status of the alarm using the `describe-alarms` command as follows:
    ```bash
    aws cloudwatch describe-alarms \
        --alarm-names FailedRequestsAlarm \
        --region us-east-1 \
        --profile admin
    ```    
    If we try the `describe-alarms` command immediately after the alarm's creation or if we have not sent any data for the metric within the specified period (60 seconds, in this case), we get the state `INSUFFICIENT_DATA` within the response.

    Send the data with the dimension using the following code: 
    ```bash
    aws cloudwatch put-metric-data \
        --namespace 'ServerlessProgrammingCookbook' \
        --metric-name 'FailedLogins' \
        --value 1 \
        --dimensions Device=Laptop \
        --region us-east-1 \
        --profile admin
    ```    
    If we check with `describe-alarms` after some time, we can see that the state has changed to `OK`. We can now post data with a higher value (for example, `10`) so that the average is more than the threshold, as shown in the following screenshot. Based on the interval we take to send data, and based on when the average is calculated, we may get a `10` or `0.5` average:
    ```json
    ```
    We will also receive a mail notification similar to the one we received in the previous recipe, Setting up CloudWatch alarms (AWS CLI).

## How it works...
In this recipe, we created an alarm for a metric with dimensions using the `dimensions` property of the `put-metric-alarm` command. We then used the `dimensions` property of the `put-metric-data` command to send the data. We also used a period of 10 seconds, smaller than we used in the previous recipe. The period has to be 10, 30, or a multiple of 60.    

## There's more...
If we mistype the dimension details either while creating the alarm or while sending data, it will not throw any error. Instead, the data will go to a separate dimension and the alarm will stay in the state `INSUFFICIENT_DATA`. The metric name and the dimension names and values are case sensitive. 

## See also
For more theory and explanation, please refer to the previous recipe, Setting up CloudWatch alarms (AWS CLI).