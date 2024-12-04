# Using CloudWatch metric log filters
We can use log filters to define search patterns within the logs. We can use log filters to turn logs into metrics and then use those metrics to trigger alarms. 

##Getting ready
The following are the prerequisites for this recipe:
* A working AWS account
* Access to AWS Management Console
* You should have configured AWS CLI and created the lambda demo-lambda-with-cli as discussed in the recipe Your First Lambda with AWS CLI in Chapter 1, Getting Started with Serverless Computing on AWS

## How to do it...
Let's see how we can create metric log filters.

### Creating metric filters from AWS CLI
Let's go through the following steps to create metric filters using AWS CLI:

1. We first need to prepare our metric transformations, as follows:
    ```json
    [
    {
        "metricName": "HelloCountMetric",
        "metricNamespace": "ServerlessProgrammingCookbook",
        "metricValue": "1"
    }
    ]
    ```
    Save this into the `metric-transformations.json` file.

2. Use the `put-metric-filter` command to create a metric filter using the `metric-transformations.json` file, as follows:
    ```bash
    aws logs put-metric-filter \
        --log-group-name /aws/lambda/demo-lambda-with-cli \
        --filter-name 'HelloCountFilter' \
        --filter-pattern 'Hello' \
        --metric-transformations file://metric-transformations.json \
        --region us-east-1 \
        --profile admin
    ```    
3. Execute the Lambda a few times, either from the console or from AWS CLI, as follows:
    ```bash
    aws lambda invoke \
        --invocation-type RequestResponse \
        --function-name demo-lambda-with-cli \
        --log-type Tail \
        --payload '{"name": "Heartin"}' \
        --region us-east-1 \
        --profile admin \
        outputfile.txt
    ```    
    The actual output of the invocation does not matter, as long as it is a success message.
4. Verify the metric from the console by going through the following steps:
    1. Log in to AWS Console and go to CloudWatch.
    2. Click on Metrics in the sidebar.
    3.Click on Metrics with no dimensions. We should now see a new metric with the name `HelloCountMetric`. We can also add `HelloCountMetric` to the graph or create an alarm for it, as shown in the following screenshot:

## How it works...
We used metric log filters in this recipe. We can use metric log filters to match terms, phrases, and values in the log events. When a match is made, we can increment the value of a CloudWatch metric. For example, we can create a metric log filter to count the occurrence of the word ERROR. 

Metric filters can also extract numerical values from space-delimited log events, and in such cases, we can increment our metric value by the actual numerical value from the log. We can also use conditional operators and wildcards for matches.

We also looked at metric log creation from the CloudWatch console, and learned how it gives us the additional capability to test our search patterns against existing data. 

Log filters do not work on data that was already sent to CloudWatch, but only on data sent after the filter was created. At the time of writing, log filters will only return the first 50 results.

## There's more...
This was a very basic recipe to demonstrate the use of metric filters. You can now create alarms for the metric created in this recipe by following the recipe Setting up CloudWatch alarms (AWS CLI).

## See also
* https://docs.aws.amazon.com/cli/latest/reference/logs/put-metric-filter.html