# Your first Lambda with AWS CLI

# Getting ready

Following are the prerequisites for this recipe:

1. Follow the Getting ready section of the recipe Your first AWS Lambda to install and configure JDK, Maven and the parent project, serverless-cookbook-parent-aws-java, and follow the notes given in that section for code usage guidelines
1. Configure AWS CLI as given later in this section
1. Create an S3 bucket

## Creating S3 bucket
We will be using Amazon** Simple Storage Service (S3)** to upload our JAR files. Therefore it would be good to do some reading on basic S3 concepts, such as S3 buckets and S3 keys.

You can create a bucket using the below command:
```bash
aws s3 mb s3://<bucket name> --profile admin
```
Replace the <bucket name> with your bucket's name. Remember that the S3 bucket name has to be unique across AWS.  

# How to do it...
1. Package the JAR.
   * We can generate JARs by running `mvn clean package`. Two JARs are created: one with only class files (starting with `original-`) and an Uber JAR with dependencies (starting with `serverless-`). In this recipe, we will use the original JAR.
1. Upload the JAR file to your S3 bucket using AWS CLI:
   ```bash
   aws s3 cp target/original-serverless-cookbook-lambda-handler-with-pojos-0.0.1-SNAPSHOT.jar s3://serverless-cookbook/lambda-handler-with-pojos-0.0.1-SNAPSHOT.jar --profile admin
   ```
   > Replace the bucket name serverless-cookbook with your bucket's name. We saw the steps to create a bucket in the Getting ready section. Also, --profile admin is the profile we created in the Getting ready section.
1. Create a policy with the aws iam create-policy command:
   ```bash
    aws iam create-policy \
    --policy-name lambda_iam_policy_test \
    --policy-document file://basic-lambda-permissions.txt \
    --profile admin
   ```
   Replace <account_id> with your account id. You can get your account number by going to the My Account page after clicking on your name on the top right of your AWS management console. The policy file is also available in the resources folder of the recipe. If successful, you should get a response with the ARN of the policy created. 

   > You may create a more restricting policy after checking the basic Lambda permissions template at https://docs.aws.amazon.com/lambda/latest/dg/policy-templates.html.
1. Create a role using the aws iam create-role command:
   ```bash
   aws iam create-role \
   --role-name lambda_iam_role_test \
   --assume-role-policy-document file://iam-role-trust-relationship.txt \
   --profile admin
   ```
   The policy file is available in the resources folder of the recipe. If successful, you should get a response with the arn of the role created.

   > Trust relationship policies allow the Lambda service to assume this role whereas the standard policy document is attached to a role to allow or deny access to resources.
1. Attach the policy to the role:
   ```bash
   aws iam attach-role-policy \
   --role-name lambda_iam_role_test \
   --policy-arn arn:aws:iam::<account_id>:policy/lambda_iam_policy_test \
   --profile admin
   ```
   Replace `<account_id>` with your account number. 
1. Create a Lambda function providing the role and the S3 location:
   ```bash
   aws lambda create-function \
   --function-name demo-lambda-with-cli \
   --runtime java8 \
   --role arn:aws:iam::<account_id>:role/lambda_iam_role_test \
   --handler tech.heartin.books.serverlesscookbook.MyLambdaHandler::handleRequest \
   --code S3Bucket=serverless-cookbook,S3Key=lambda-handler-with-pojos-0.0.1-SNAPSHOT.jar \
   --timeout 15 \
   --memory-size 512 \
   --profile admin
   ```
   Replace <account_id> with your account number. The code option can accept the shorthand form as used here, or a JSON. 

1. Invoke our Lambda from CLI:
   ```bash
   aws lambda invoke \
   --invocation-type RequestResponse \
   --function-name demo-lambda-with-cli \
   --log-type Tail \
   --cli-binary-format raw-in-base64-out
   --payload '{"name":"Heartin"}' \
   --profile admin \
   outputfile.txt
   ```
   In certain platforms, you might have to add escaping for the payload specified in the command line. This is not required as the payload is specified as a file, as here:
   ```bash
   --payload file://input.txt \
   ```
1. Note the following regarding cleanup roles, policy, and Lambda.
   * To delete Lambda, perform the following:
     ```bash
     aws lambda delete-function \
     --function-name demo-lambda-with-cli \
     --profile admin
     ```
   * To detach policy from the role, perform the following:
     ```bash
     aws iam detach-role-policy \
     --role-name lambda_iam_role_test \
     --policy-arn arn:aws:iam::<account_id>:policy/lambda_iam_policy_test \
     --profile admin
     ```
   * Replace <account_id> with your account number. 
   * To delete a role, note the following: 
     ```bash
     aws iam delete-role \
     --role-name lambda_iam_role_test \
     --profile admin
     ```
   * To delete policy, perform the following:
     ```bash
     aws iam delete-policy \
     --policy-arn arn:aws:iam::<account_id>:policy/lambda_iam_policy_test \
     --profile admin 
     ```
   * Replace <account_id> with your account number. 
# How it works...
The following are the important details and concepts that were introduced in this recipe:
## Creating a role and attaching a policy
You need to create a role with a trust policy that allows our Lambda to assume the role. You also need to attach a policy that has CloudWatch permissions for logging. 

## Lambda memory-size and timeout
When creating a function from CLI, the default value of timeout is 3 seconds, and default value memory-size is 128 MB, which may not be sufficient for Lambdas with Uber JARs, and you may get a timeout exception or Process exited before completing request. Hence, I have set a higher timeout and memory-size. Other parameters are mostly self-explanatory.

## S3 Bucket and Key
Amazon S3 is an object store. Objects (files) are stored as simple key-value pairs within containers called buckets. Bucket names have to be unique across AWS. There is no folder hierarchy within the buckets like traditional file systems. However, we can simulate folder structure with hierarchical key names. For example, consider the `folder1/folder2/file.txt` key, that simulates a folder-like structure. Read more about simulating folders in S3 at https://docs.aws.amazon.com/AmazonS3/latest/user-guide/using-folders.html. 

## Cleaning up
You need to do a cleanup in the following order:
1. Delete Lambda that uses the role
1. Detach policy from role
1. Delete role and policy

> We cannot delete a role without detaching all policies. We can however delete a role without deleting the Lambda. If you try to invoke the Lambda before attaching another role, it will give you an error such as—The role defined for the function cannot be assumed by Lambda.