# Your first custom metric (AWS CLI)
In the previous recipe, we learned how to check the automatically created CloudWatch metrics. We also learned some theory behind CloudWatch metrics. In this recipe, we will see how we can create a custom metric. We will create a simple functional scenario where we post a number of failed logins to the system. The sender may aggregate more requests within a set time, but we will not look at this in this recipe.

## Getting ready
The following are the prerequisites for this recipe:
* A working AWS account.
* Access to AWS Management Console.
* You should have configured AWS CLI as discussed in the recipe [Your First Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) in Chapter 1, Getting Started with Serverless Computing on AWS.

## How to do it...
We can use the `aws cloudwatch put-metric-data` command to send metric data to CloudWatch. If the metric does not already exist, then this command will also create that metric. 

### Step 1–Create a simple metric without dimensions 
Using the following code, we will first create a metric without specifying dimensions and then we will post data with dimensions. We can combine both Step 1 and Step 2 for real-world use cases:
```bash
aws cloudwatch put-metric-data \
    --namespace 'ServerlessProgrammingCookbook' \
    --metric-name 'FailedLogins' \
    --value 1 \
    --region us-east-1 \
    --profile admin
```    
We can verify our metric from AWS Management Console as follows:
1. Log in to Management Console and go to the CloudWatch dashboard.
2. Click on the Metrics link from the sidebar. We should now see a new custom namespace called `ServerlessProgrammingCookbook`, along with the AWS namespaces, as shown in the following screenshot:


3. Click on the custom namespace `ServerlessProgrammingCookbook`. This will display a link called Metric with no dimensions, as shown in the following screenshot:


    This is because we have not defined any dimensions for our metric.

4. Click on the Metric with no dimensions link. This will show us our new metric. We can now click on the dropdown next to the metric, as shown in the following screenshot, to perform all of the operations that we can perform on an automatically generated metric:

### Step 2—Add dimensions to metric data
We can specify dimensions with the put-metric-data sub-command using its dimensions property. Add a dimension with two different values using the following code:
```bash
aws cloudwatch put-metric-data \
    --namespace 'ServerlessProgrammingCookbook' \
    --metric-name 'FailedLogins' \
    --value 1 \
    --dimensions 'Device=Mobile' \
    --region us-east-1 \
    --profile admin

aws cloudwatch put-metric-data \
    --namespace 'ServerlessProgrammingCookbook' \
    --metric-name 'FailedLogins' \
    --value 1 \
    --dimensions 'Device=Laptop' \
    --region us-east-1 \
    --profile admin

aws cloudwatch put-metric-data \
    --namespace 'ServerlessProgrammingCookbook' \
    --metric-name 'FailedLogins' \
    --value 1 \
    --dimensions 'Device=Laptop' \
    --region us-east-1 \
    --profile admin
```    
We can verify our metric from AWS Management Console as follows:
1. When we click on our custom namespace, `ServerlessProgrammingCookbook`, we should see a link for our new dimension along with a link for the ones without a dimension, as shown in the following screenshot:


2. Click on the Device link. This will show our metrics for each value of the Device dimension, as shown in the following screenshot:


3. We can now click on the dropdown next to any of these dimension values (as shown in the following screenshot) to perform all operations that we can perform on an automatically generated metric:

## How it works...
In this recipe, we posted data to a custom metric that we wanted to create using the `aws cloudwatch put-metric-data` command and the metric was created for us. We first created the metric without specifying any dimension and then posted data to the metric specifying the dimensions. We also verified both cases. You can also refer to the previous recipe for the basic theory behind CloudWatch metrics, if you have not already done this.

The minimum granularity to which CloudWatch can aggregate metric data is 1 minute. So even if we post data to a CloudWatch metric in shorter intervals than 1 minute, CloudWatch will only aggregate with a minimum granularity of 1 minute. 

## There's more...
In this recipe, we created a custom metric using the `aws cloudwatch put-metric-data` command and specified the value and dimension using their respective properties. We can also use the `metric-data` property, which accepts a list of up to 20 metrics per call. Refer to the link on `put-metric-data` in the See also section for more details.

## See also
* https://docs.aws.amazon.com/cli/latest/reference/cloudwatch/put-metric-data.html