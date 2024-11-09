# Using AWS SDK, Amazon CloudFormation, and AWS CLI with Lambda
AWS SDK allows you to write code that interacts with AWS services. In this recipe, we will use AWS Java SDK for IAM to do some basic IAM operations to form a Lambda programmatically. We will use it along with Amazon CloudWatch and AWS CLI, which is a general practice followed in most real-world projects. 

> The aim of this recipe is to understand the use of AWS Java SDK inside Lambda. Therefore, we will not go deep into the details of the IAM operations discussed in the recipe. The IAM operations details are available at https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-iam-users.html. 

## Creating the POJOs for requests and response.

1. Package the dependencies into an uber JAR using `mvn clean package`.
  * Two JARs will be created: one with only class files (starting with `original-`) and an Uber JAR with all dependencies (starting with `serverless-`). We will use the Uber JAR in this recipe.

1. Upload the JAR to S3:
   ```bash
    aws s3 cp target/iam-operations-0.0.1-SNAPSHOT.jar s3://dev-for-tw-robert-20241020/ --profile admin
   ``` 
1. Create a CloudFormation template for our lambda function.
   * You need to create a role with a trust policy that allows our Lambda to assume the role. You also need to create a policy with CloudFormation and IAM permissions. 
   * We need to add permissions for IAM operations in our policies:
      ```yaml
      - Effect: Allow
        Action:
        - iam:CreateUser
        - iam:DeleteUser
        - iam:ListUsers
        Resource:
        - Fn::Sub: arn:aws:iam::${AWS::AccountId}:user/*
      ```
   * We have used a pseudo-parameter, AWS::AccountId, within a sub-intrinsic function to dynamically populate the account ID. I also improved the CloudWatch logging permission policy from the previous recipe using the pseudo-parameters:
      ```yaml
      - Effect: Allow
        Action:
        - logs:CreateLogStream
        Resource:
        - Fn::Sub: arn:${AWS::Partition}:logs:${AWS::Region}:${AWS::AccountId}:log-group:/aws/lambda/aws-sdk-iam-with-cf-cli:*
      - Effect: Allow
        Action:
        - logs:PutLogEvents
        Resource:
        - Fn::Sub: arn:${AWS::Partition}:logs:${AWS::Region}:${AWS::AccountId}:log-group:/aws/lambda/aws-sdk-iam-with-cf-cli:*:*
      ```
   * You should be able to complete this recipe by referring to the previous recipe, Your First Lambda using CloudFormation.
     > The completed template file is available in the resources folder as cf-template-iam-operations.yml.
1. Upload the CloudFormation template to S3:
   ```bash
   aws s3 cp ../resources/cf-template-iam-operations.yml s3://dev-for-tw-robert-20241020/cf-template-iam-operations.yml --profile admin
   ```
1. Create a CloudFormation stack using the CloudFormation template from AWS CLI:
   ```bash
   aws cloudformation create-stack --stack-name myteststack --template-url https://s3.amazonaws.com/dev-for-tw-robert-20241020/cf-template-iam-operations.yml --capabilities CAPABILITY_NAMED_IAM --profile admin
   ``` 
   * This immediately responds with StackId. Note that you used a parameter, --capabilities CAPABILITY_NAMED_IAM. This is a security-related precaution. You are explicitly telling CloudFormation that you know what you are doing. 
   * You can check the status of stack creation using the describe-stacks command:
   ```bash
   aws cloudformation describe-stacks --stack-name myteststack --profile admin
   ```
   StackStatus: `CREATE_COMPLETE` means stack creation was successful.
1. Verify the deployment with AWS CLI Lambda invoke:
   ```bash
   aws lambda invoke --invocation-type RequestResponse  \
          --function-name aws-sdk-iam-with-cf-cli \
          --log-type Tail \
           --cli-binary-format raw-in-base64-out \
          --payload '{"operation":"CREATE", "userName":"abcd"}' \
          --profile admin \
          outputfile.txt
   ```
   You can replace CREATE in the payload with CHECK for checking if the user was created, and DELETE for deleting the user.
1. Delete the CloudFormation stack:
   ```bash
   aws cloudformation delete-stack --stack-name myteststack --profile admin
   ```

## How it works...
AWS SDKs are used to interact with AWS services programmatically. There are SDKs available for programming languages such as Java, .Net, Node.js. PHP, Python, Ruby, Browser, Go, and C++. 

We uploaded our CloudFormation template to S3 and provided the location using `--template-url`. You can also specify the template contents directly or from a file using `file://` with another option `--template-body`.

We created our roles for Lambda manually. If we are using Management console, we can create custom Lambda roles from within our Lambda create function page, or directly from IAM. 

We used one new intrinsic function in our CloudFormation template, Fn::Sub. Fn::Sub, which substitutes variables in an input string with values that you specify. We used it to substitute the AWS Account ID and a few other values rather than hard-coding them.

We also used the following pseudo- `parameters:` `AWS::AccountId`, `AWS::Partition`, and `AWS::Region`, which represents the current account ID, partition, and region respectively. For most regions, the partition is aws. For resources in other partitions, the partition is named as aws-partitionn (for instance, `aws-cn` for China and `aws-us-gov` for the AWS GovCloud (US) region). Using pseudo-parameters lets us avoid worrying about the actual partition name.

## There's more...
We used only basic IAM operations in this recipe. You can check the documentation and implement more complex operations from within Lambda code if interested.

> We will use CloudFormation and AWS CLI for most of our recipes. However, you may follow these steps to try to do the same in the management console. Doing things visually will help you remember the concepts for a longer time.

## Pseudo-parameters
Pseudo-parameters are predefined parameters provided by AWS CLoudFormation. You can use them within a Ref or a Sub function to dynamically populate values. Pseudo-parameters available to use within a CloudFormation template include `AWS::AccountId`, `AWS::NotificationARNs`, `AWS::NoValue`, `AWS::Partition`, `AWS::Region`,` AWS::StackId`, `AWS::StackName`, and `AWS::URLSuffix`.

Read more about pseudo-parameters at https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html. 

## See also
* https://aws.amazon.com/sdk-for-java
* https://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html
* https://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_examples.html
* https://docs.aws.amazon.com/lambda/latest/dg/limits.html
* https://docs.aws.amazon.com/cli/latest/reference/cloudformation/index.html#cli-aws-cloudformatio