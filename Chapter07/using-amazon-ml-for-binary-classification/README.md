# Using Amazon ML for binary classification (AWS CLI)
This recipe will outline the steps to perform a simple machine learning service task following the AWS ML tutorial for the Management console, but also using AWS CLI APIs. The objective of this recipe is to get you started with using Amazon ML from an AWS CLI API perspective. 

## Getting ready
For the purpose of this demonstration, we will use the sample data CSV provided by AWS at `s3://aml-sample-data/banking.csv`. This CSV is provided by AWS for their Management console's step-by-step tutorial, as shown in the following screenshot:



> You may download and familiarize yourself with the data, or simply directly use it in this recipe by going through the steps in the How to do it section.

# How to do it...
In this recipe, we will learn how to make predictions with an AWS ML service for binary classification using AWS CLI by going through the following steps:

1. Prepare the data as a CSV and upload it to S3. Amazon ML requires a CSV with each row corresponding to an observation that may be used for training or testing. Each column also needs a name, which you can specify as the first row or specify separately using a schema file. You may also split data into multiple CSV files within the same bucket. If you have multiple files, you should provide a path ending with a forward slash (`/`).
    As mentioned in the Getting ready section, we will reuse the sample data available in AWS, which is already uploaded to S3 at `s3://aml-sample-data/banking.csv`. 
2. Create the data source.
    > You should always split your data into two sets and create two data sources, one for training and one for evaluation. For example, you may use 70 percent of the data for training the system and creating the ML model, and the remaining 30 percent can be used for evaluating the ML model that was created. For this recipe, I will be using the same data source for both training and testing to keep things simple, but you should create two data sources in real world—one for training and one for evaluation—and use them accordingly. 

    We can create an S3 data source object using the aws machinelearning create-data-source-from-s3 command by going through the following steps:
    1. Create a sample JSON input using the generate-cli-skeleton subcommand, as follows:
        ```bash    
        aws machinelearning create-data-source-from-s3 \
            --generate-cli-skeleton input
        ```    
        > Since the command input has nested JSON, it is a good practice to generate the sample input using the generate-cli-skeleton subcommand.
    2. Prepare the input JSON with actual values, as follows:
        ```json    
        {
            "DataSourceId": "my-first-s3-ds-id",
            "DataSourceName": "My First S3 DataSource",
            "DataSpec": {
                "DataLocationS3": "s3://aml-sample-data/banking.csv",
                "DataRearrangement": "{\"splitting\":{\"percentBegin\":10,\"percentEnd\":60}}",
                "DataSchemaLocationS3": "s3://aml-sample-data/banking.csv.schema"
            },
            "ComputeStatistics": true
        }
        ```
        Save this file as `create-data-source-from-s3-cli-input.json`.
    3. Execute the aws machinelearning create-data-source-from-s3 command, providing the input JSON file, as follows:
        ```bash    
        aws machinelearning create-data-source-from-s3 \
        --cli-input-json file://create-data-source-from-s3-cli-input.json \
        --region us-east-1 \
        --profile admin
        ``` 
        This command works asynchronously, and immediately returns the data source ID, as shown in the following screenshot:
        ```json
        ```
        You can check the status of your data sources with the describe-data-sources subcommand, as follows: 
        ```bash
        aws machinelearning describe-data-sources \
            --region us-east-1 \
            --profile admin
        ```
    > You can also filter the data sources returned in the response with additional options provided by the describe-data-sources subcommand. We did not use additional filtering here, as we only had one data source. You can check the See more section for a link to the AWS documentation for this sub command.

    Once completed successfully, you should see the following response:
    ```json
    ```
    Initially, when you run the commands, you will see the status as `INPROGRESS` until it is completed.

    Alternatively, you can use the `get-data-source` subcommand to get the details for a particular data source.

3. Create a ML binary model based on the data source by using the following command:
    ```bash
    aws machinelearning create-ml-model \
    --ml-model-id 'my-first-ml-model-id' \
    --ml-model-name 'My First ML Model' \
    --ml-model-type 'BINARY' \
    --training-data-source-id 'my-first-s3-ds-id' \
    --region us-east-1 \
    --profile admin
    ``` 
    This command works asynchronously and immediately returns the ML model source ID, as shown in the following screenshot:
    ```json
    ```
    You can check the status of your data sources with the describe-ml-models subcommand, as shown in the following code:
    ```bash
    aws machinelearning describe-ml-models \
        --region us-east-1 \
        --profile admin
    ```
    > You can also filter the ML models returned in the response with additional options provided by the describe-ml-models subcommand. We did not use additional filtering here, as we only had one ML model. You can check the See more section for a link to the AWS documentation for this subcommand.

    Once completed successfully, you should see the following response. The first part of the response contains the status and basic timestamp information:
    ```json
    ```
    Initially, when you run the commands, you will see the status as `INPROGRESS` until it is completed.
    The response also contains additional information of the model, including the defaults, as shown in the following screenshot:
    Alternatively, you can use the `get-ml-model` subcommand to get the details for a particular ML model.

4. Create an evaluation to verify your dataset. As mentioned before, I will be using the same dataset to keep things simple, as the aim of this recipe is to understand the process and syntax. However, you should always split your data into two sets and create two data sources, one for training and one for evaluation. 
    We can create an evaluation set using the aws machinelearning create-evaluation command, as follows: 
    ```bash
    aws machinelearning create-evaluation \
        --evaluation-id 'my-first-ml-evaluation-id' \
        --evaluation-name 'My First ML Evaluation' \
        --ml-model-id 'my-first-ml-model-id' \
        --evaluation-data-source-id 'my-first-s3-ds-id' \
        --region us-east-1 \
        --profile admin
    ```    
    > You can also filter the ML evaluations returned in the response with additional options provided by the describe-evaluations subcommand. We did not use additional filtering here as we only had one ML model. You can check the See more section for a link to the AWS documentation for this subcommand.

    Once completed successfully, you should see the following response:
    ```json
    ```

    The closer the value of `BinaryAUC` is to 1, the better the model is. We got a very good result since we used the same dataset for training and evaluation.

    Alternatively, you can use the `get-evaluation` subcommand to get the details of a particular evaluation.

5. Predictions can be real-time or batch. In this recipe, we will make a real-time prediction. First, we need to generate an endpoint. 
    Execute the  `get-ml-model` subcommand as follows:
    ```bash
    aws machinelearning get-ml-model \
        --ml-model-id 'my-first-ml-model-id' \
        --region us-east-1 \
        --profile admin
    ```    
    The response will contain an endpoint section, shown in the following screenshot, denoting that no endpoint is generated:
    ```json
    ```
1. Create the real-time endpoint using the following code:
    ```bash
    aws machinelearning create-realtime-endpoint \
        --ml-model-id 'my-first-ml-model-id' \
        --region us-east-1 \
        --profile admin
    ```    
    This will immediately return an endpoint with a status of UPDATING, as shown in the following screenshot:
    ```json
    ```
    You can use the `get-ml-model` subcommand as we did earlier in this section to get the details of the ML model, including the endpoint status. Once completed, the status and endpoint details should look as follows:
    ```json
    ```
    The response will also contain additional information, such as `TrainingParameters`, `InputDataLocationS3`, `MLModelType` (which in our case is `Binary`), `LogUri`, `ComputeTime`, `FinishedAt` , and `StartedAt`.

2. You can predict the target field (`0` or `1` for `Binary`) using the endpoint of the `predict` subcommand to provide the other record fields, as shown in the following code:
    ```bash
    aws machinelearning predict \
        --ml-model-id 'my-first-ml-model-id' \
        --record 'age=44,job=blue-collar,marital=married,education=basic.4y,default=unknown,housing=yes,loan=no,contact=cellular,month=aug,day_of_week=thu,duration=210,campaign=1,pdays=999,previous=0,poutcome=nonexistent,emp_var_rate=1.4,cons_price_idx=93.444,cons_conf_idx=-36.1,euribor3m=4.963,nr_employed=5228.1' \
        --predict-endpoint 'https://realtime.machinelearning.us-east-1.amazonaws.com' \
        --region us-east-1 \
        --profile admin
    ```    
    This will return the following response:
    ```json
    ```
    I just picked up a record from the data that we have. However, you can create a random record or pick one based on your use case and apply the preceding syntax.
    > Binary classification ML models use a ScoreThreshold to mark the boundary between a positive prediction and a negative prediction. Output values greater than or equal to the ScoreThreshold will receive a positive result from the ML model (such as 1 or true). Output values less than the ScoreThreshold receive a negative response from the ML model (e.g. 0 or false). We have not altered the default threshold score for this recipe, which was 0.5. However, you may change it using the update-ml-model subcommand.


## How it works...
In summary, we did the following in this recipe:
1. Learned how to prepare the data as a CSV and upload it to S3
2. Created an S3 data source
3. Created an ML model
4. Created an evaluation and verified the model
5. Created an endpoint for real-time prediction
6. Predicted the target value for a sample record

Let's discuss some of the concepts we learned in the recipe in a bit more detail.

### Types of models
Amazon ML is used primarily for the following prediction use cases:
* `Binary classification`: Classifies values as one of two categories, such as true or false (or 1 or 0)
* `Multivalue classification`: Classifies values into multiple groups
* `Regression`: Predicts a numeric value

### DataSource object
A `DataSource` object can reference data from different sources, such as S3, Redshift, and RDS. We used an S3 data source in this recipe. A `DataSource` object needs to be specified for operations such as `CreateMLModel`, `CreateEvaluation`, or `CreateBatchPrediction`.

### Receiver Operating Characteristic and Area Under the ROC
A **Receiver Operating Characteristic (ROC)** curve is a graph that shows the performance of a classification model at different classification thresholds. The **Area Under the ROC** Curve measures the entire two-dimensional area underneath the entire ROC curve, aggregating the measure of the performance across all classification thresholds. 

The AUC value denotes the ability of the model to predict a higher score for positive examples compared to negative examples. The AUC value is a decimal value from 0 to 1. The higher the value of the AUC, the better the ML model is. We use the AUC to measure the quality of the binary classification model. For our recipe, since I used the same dataset for training and testing, the AUC value was very close to 1.

## There's more...
We used the AUC value to measure the accuracy of our binary classification model. For multivalue classification models, AWS uses the macroaverage F1 score to evaluate the predictive accuracy of a multiclass metric. A larger F1 score indicates better predictive accuracy for a regression model; AWS uses the **root mean square error (RMSE)** metric. The smaller the value of the RMSE, the better the accuracy of the model. 

A detailed discussion of ML concepts is beyond the scope of this book. The aim of this recipe was to get you started with Amazon ML using AWS CLI APIs, and to familiarize you with a few ML terms that you can explore further. You can follow the reference links or other books on ML to learn more and experiment with the concepts further. I have also added links to some datasets in the See also section that you can use for your experiments.

## See also
* http://archive.ics.uci.edu/ml/index.php
* https://www.kaggle.com/datasets
* https://docs.aws.amazon.com/machine-learning/latest/dg/understanding-the-data-format-for-amazon-ml.html
* https://docs.aws.amazon.com/cli/latest/reference/machinelearning/describe-data-sources.html
* https://docs.aws.amazon.com/cli/latest/reference/machinelearning/describe-ml-models.html
* https://docs.aws.amazon.com/cli/latest/reference/machinelearning/describe-evaluations.html
* https://docs.aws.amazon.com/machine-learning/latest/dg/evaluating_models.html