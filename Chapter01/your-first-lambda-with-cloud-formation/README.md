# Your first Lambda with Amazon CloudFormation
The concept of writing code to manage infrastructure is referred to as **Infrastructure as Code (IaC)** and is a practice that most enterprise companies follow. You can also maintain the provisioning code in a code repository and follow practices such as code reviews like any other code. Thus, it lets you reuse the provisioning code.

## Getting ready
You need to read and follow to the **Getting ready** section of the recipes *Your first AWS Lambda and Your first Lambda with AWS CLI* before proceeding.

## Set up the project and S3 bucket
In this recipe, we are reusing the Lambda we created in the *Your Lambda with AWS CLI* recipe. Generate a JAR by running `mvn clean package `inside that project, and upload the JAR to S3:
```bash
aws s3 cp target/original-serverless-cookbook-lambda-handler-with-pojos-0.0.1-SNAPSHOT.jar s3://serverless-cookbook/lambda-handler-with-pojos-0.0.1-SNAPSHOT.jar --profile admin
```
Replace the bucket name `serverless-cookbook` with your bucket's name. Refer to the Getting ready section of the recipe *Your First AWS CLI* to create the S3 bucket.

## Understanding YAML and JSON
CloudFormation templates are written in JSON or YAML. Both support data in key-value pairs, objects, arrays, and so on .YAML also supports additional features such as multi-line strings, comments, and so on. I will also be using YAML for the examples. Since YAML support was introduced later for CloudFormation, you will also see a lot of JSON templates in the web. So, it is also good to have a decent understanding of YAML and JSON. If you are familiar with one, you may also use one of the JSON to YAML or YAML to JSON converters available online.

## How to do it...
1. Create the CloudFormation template.
   * Resources components specify the AWS resources used. We need two resources for our use case: a role and a Lambda function with that role. The following is the basic structure of our CloudFormation template:
      ```yaml
      ---
      AWSTemplateFormatVersion: '2010-09-09'
      Description: Building Lambda with AWS CloudFormation
      Resources:
      IamRoleLambdaExecution:
          Type: AWS::IAM::Role
          Properties:
          # Properties for the role are shown later.
      LambdaFunctionWithCF:
          Type: AWS::Lambda::Function
          Properties:
          # Properties for the Lambda are shown later.
          DependsOn:
          - IamRoleLambdaExecution
      ```
   * I have also defined `AWSTemplateFormatVersion` and `Description` as a general practice, but they are optional. Note that properties for the `IamRoleLambdaExecution` and `LambdaFunctionWithCF` are not shown here. You may refer to further steps or use the template from the code files.
   * The role needs a trust relationship policy that allows the lambda to assume that role, and we need to attach a policy to the role that provides CloudWatch logging permissions. The `AssumeRolePolicyDocument` property specifies the trust relationship policy for the role:
      ```yaml 
      AssumeRolePolicyDocument:
        Version: '2012-10-17'
        Statement:
        - Effect: Allow
          Principal:
            Service:
            - lambda.amazonaws.com
          Action:
          - sts:AssumeRole
      ```
   * The policy is specified inline within the Policies property of the role:
      ```yaml 
      Policies:
      - PolicyName: 'lambda-with-cf-policy'
        PolicyDocument:
          Version: '2012-10-17'
          Statement:
          - Effect: Allow
            Action:
            - logs:CreateLogGroup
            - logs:CreateLogStream
            - logs:PutLogEvents
            Resource: arn:aws:logs:*:*:*
      ```
   * We will also define two more properties for the role, namely path and name:
      ```yaml 
      Path: "/"
      RoleName: "lambda-with-cf-role"
      ```
   * Our Lambda function will have the following basic configuration:
      ```yaml 
      LambdaFunctionWithCF:
        Type: AWS::Lambda::Function
        Properties:
          Code:
            S3Bucket: 'serverless-cookbook'
            S3Key: lambda-handler-with-pojos-0.0.1-SNAPSHOT.jar
          FunctionName: first-lambda-with-cloud-formation
          Handler: tech.heartin.books.serverlesscookbook.MyLambdaHandler::handleRequest
          MemorySize: 512
          Role:
            Fn::GetAtt:
            - IamRoleLambdaExecution
            - Arn
          Runtime: java17
          Timeout: 15
        DependsOn:
        - IamRoleLambdaExecution
      ```
   * We specify the role as a dependency for the Lambda function, and use `Fn::GetAtt` to retrieve the role dynamically instead of hardcoding the name. Most of the other properties are self-explanatory. 

   * A CloudFormation stack is a collection of AWS resources that you need to manage as a single unit. All the resources in a stack are defined by a CloudFormation template. When you delete the stack, all of its related resources are also deleted.

   * We can create a CloudFormation stack in different ways, including the following:
     1. Going through the Create Stack option within the CloudFormation service inside AWS Management Console
     1. Uploading directly from the Template Designer within the CloudFormation service inside AWS Management Console
     1. AWS CLI
   * In this recipe, I will use Designer, but in all other recipes I will be using AWS CLI. AWS CLI is the best way to deploy CloudFormation templates. Designer is also a good tool to visualize and validate your scripts. 

2. Create CloudFormation stack from Designer:
   1. Log in to AWS and go to CloudFormation service.
   1. Click on the Design template button to go to Designer. Within designer, you may do the following:
   1. Choose template language as YAML in the editor. (If you are using a JSON template, use JSON instead.)
   1. Select the Template tab in the editor.
   1. Copy and paste your template into the template editor window. 
   1. Click on refresh on the Designer to see the template in the Design view. 
   1. If any changes are required, you can either make changes within the Template tab or use the Components tab.
   1. If everything looks good, click on the upload button on the top left of the designer to launch the Stack creation wizard with the current template.
   1. Follow the wizard with defaults, and select the checkbox for I acknowledge that AWS CloudFormation might create IAM resources with custom names. Finally, click on Create Stack.
   1. Invoke our Lambda with AWS CLI as follows and verify:
      ```bash
      aws lambda invoke \
      --invocation-type RequestResponse \
      --function-name first-lambda-with-cloud-formation \
      --log-type Tail \
      --cli-binary-format raw-in-base64-out \
      --payload '{"name":"Heartin"}' \
      --profile admin \
      outputfile.txt
      ```
      Output can be viewed in the outputfile.txt file:
      ```bash
      $ cat outputfile.txt
      {"message":"Hello Hearin"}
      ```

## Cleaning up roles, policy, and Lambda
To clean up resources created by CloudFormation, you just need to delete the stack. This is the default setting. Since we have used AWS management console for stack creation, we will use it for deletion as well.

You can delete a CloudFormation stack from the management console as follows: go to CloudFormation service, select the stack, click on Actions, and click Delete Stack.

## How it works...
In this recipe, we used the following CloudFormation template components: **Resource**, **AWSTemplateFormatVersion**, and **Description**. Resources are the AWS resources used in the template. **AWSTemplateFormatVersion** is the version of CloudFormation template the template conforms to. 

> The only mandatory section in a CloudFormation template is Resource. However, it is a good practice to always define a version and a description for a template.
We used two resources: a role (`IAMRoleLambdaExecution`) and a Lambda function (`LambdaFunctionWithCF`) that depends on that role. Resource names can be anything. Type specifies the type of the resource. We used two types, namely `AWS::IAM::Role and AWS::Lambda::Function`.

The properties of the `AWS::IAM::Role resource` type that we used are as follows:
* `AssumeRolePolicyDocument` specifies the trust relationship policy for the role
* Policies specify the policies inline

The properties of the `AWS::Lambda::Function` resource type that we used are as follows:
* `Code` property specifies the S3 bucket and the key. You can also specify a reference to an S3 Bucket resource type so that a new bucket is created dynamically and its name is used here.
* `FunctionName` specifies the name of the Lambda function.
* `Handler` specifies the fully qualified name of the handler class with the handler method.
* `MemorySize` specifies the memory in MB. The number of CPU cores is decided by AWS based on the memory.
* `Role` specifies the role. 
* `Runtime` specifies the runtime (for instance, java17).
* `TimeOut` specifies the timeout. 

To get the role `Arn`, we used the `GetAtt` function passing the logical name of the Role and the property name `Arn`:

`Fn::GetAtt` is an intrinsic function that returns the value of an attribute from a resource in the template. 

We used CloudFormation designer in the recipe to see our template in design view, and then uploaded the template into a stack from the designer. You can also use the Designer to design CloudFormation templates from scratch. 

# CloudFormation Template Components
CloudFormation templates are composed of the following primary components:
* `AWSTemplateFormatVersion` is the version of CloudFormation template the template conforms to
* `Description` is a text that describes the template
* `Resource` components are the AWS resources used in the template
* `Parameter` components are the input (dynamic) to your template
* `Mapping` components are variables (static) for your template
* `Output` components describe the values that are returned
* `Condition` components control resource provisioning
* `Metadata` provides additional information about the template
* `Transform` specifies the version of the **AWS Serverless Application Model (AWS SAM)** for Serverless applications
> Resource is the only mandatory section of a CloudFormation template. 

We will talk about the components in the recipe in which they are introduced. Read more about template components at https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/template-anatomy.html.

## Resource component
The following are some of the important features of Resource component:
* `Resource` component of the template specifies the AWS resources used in the template
* `Resource` can reference each other using the ref element
* Resource names can be anything
* `Type` specifies the type of the resource
* Each type has its own set of properties that you can refer to from the documentation, given under the properties element
* `DependsOn` specifies the other resources that the current resource is dependent on


## Intrinsic functions
Intrinsic functions are built-in functions provided by AWS to use within a template for dynamically adding values. Common intrinsic functions used within CloudFormation templates are as follows: `Fn::Base64`, `Fn::Cidr`, `Fn::FindInMap`, `Fn::GetAtt`, `Fn::GetAZs`, `Fn::ImportValue`, `Fn::Join`, `Fn::Select`, `Fn::Split`, `Fn::Sub`, and `Ref`.

CloudFormation also supports the following conditional functions: `Fn::And`, `Fn::Equals`, `Fn::If`, `Fn::Not`, and `Fn::Or`.

We can specify the functions in the standard forms as mentioned here or in the short-hand form (for instance, `!Base64`, `!Cidr`, `!Ref`, and so on) if you are using YAML. We used the standard syntax for this recipe for reference, but will use the short-hand syntax in later recipes. 

We will discuss the functions introduced in each chapter. You can read more about all intrinsic functions at https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference.html.

## Additional benefits of CloudFormation 
Apart from automated provisioning of resources through code and enabling reuse, CloudFormation also has other important usages, including the following:
* Lets you estimate costs based on the templates
* Enables tracking costs effectively
* Helps in saving costs by automated deletion of resources when not needed
* Diagrams generated based on templates can help in understanding the system better, and can be used in design discussions

## Cloud Formation alternatives
Important alternatives to using CloudFormation include Ansible, Terraform, Chef, AWS OpsWorks, and AWS Elastic Beanstalk. 

## See also
All resources supported by CloudFormation are available at https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html