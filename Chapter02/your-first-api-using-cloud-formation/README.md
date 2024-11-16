# Building your first API using Amazon CloudFormation
In the previous recipe, we built a simple REST API using the AWS CLI. In this recipe, we will use the CloudFormation template to create an API and understand the benefits of using CloudFormation over the AWS CLI. Most enterprise projects use CloudFormation templates for their infrastructure provisioning in AWS.

## How to do it...
Let's create the same API that we created in the previous recipe, but do it by using a CloudFormation template, and then deploy it using the AWS CLI. Finally, we will invoke the API from a browser. The CLI commands corresponding to the CloudFormation template components were already discussed in the previous recipe:

1. Start by defining the template with AWSTemplateFormatVersion and a description, as follows:
   ```yaml
   ---
   AWSTemplateFormatVersion: '2010-09-09'
   Description: Building API with AWS CloudFormation
   ```
2. Define our REST API with the resource type AWS::ApiGateway::RestApi, as follows:
   ```yaml
   Resources:
    MyFirstRestAPI:
    Type: AWS::ApiGateway::RestApi
    Properties:
    Name: Greeting API
      Description: API for greeting an user
      FailOnWarnings: true
   ```      
   > The FailOnWarnings property tells CloudFormation to roll back the resource if a warning occurs during API creation. 
3. Define the parent resource, greeting, under the root path, using the type `AWS::ApiGateway::Resource`:
   ```yaml
   GreetingResource:
     Type: AWS::ApiGateway::Resource
     Properties:
       RestApiId: !Ref MyFirstRestAPI
       ParentId: !GetAtt MyFirstRestAPI.RootResourceId
       PathPart: 'greeting'
   ```    
   > We do not have to copy and paste our REST API IDs. Instead, we refer to our REST API by using the Ref intrinsic function. Also, we are now using the shorthand form for the intrinsic functions.
4. Define a path parameter resource under `greeting` by using the type `AWS::ApiGateway::Resource`:
   ```yaml
   NamePathParamResource:
     Type: AWS::ApiGateway::Resource
     Properties:
       RestApiId: !Ref MyFirstRestAPI
       ParentId: !Ref GreetingResource
       PathPart: '{name}'
   ```    
   > We are using the same properties from the AWS CLI commands, but in the CloudFormation way.
5. Create the method configuration with the resource type AWS::ApiGateway::Method, as follows:
   ```yaml
   MyMockMethod:
     Type: AWS::ApiGateway::Method
     Properties:
       AuthorizationType: NONE
       HttpMethod: GET
       Integration:
         Type: MOCK
         IntegrationHttpMethod: GET
         IntegrationResponses:
         - StatusCode: 200
           ResponseTemplates:
             application/json: "{\"message\": \"Hello $input.params('name')\" }"
         RequestTemplates:
           application/json: "{\"statusCode\": 200}"
       ResourceId: !Ref NamePathParamResource
       RestApiId: !Ref MyFirstRestAPI
       MethodResponses:
       - StatusCode: 200
   ```    
   > The CloudFormation template combines multiple CLI commands (`put-method`, `put-method-response`, `put-integration`, and `put-integration-response`) into a single and simple configuration. 

6. Deploy our application using the resource type AWS::ApiGateway::Deployment, as follows:
   ```yaml
   MyFirstDeployment:
     DependsOn: MyMockMethod
     Type: AWS::ApiGateway::Deployment
     Properties:
       Description: 'First Deployment'
       RestApiId: !Ref MyFirstRestAPI
       StageDescription:
         Description: 'Dev Stage'
       StageName: 'dev'
   ```       
   > We have to specify that our Deployment resource depends on our Method resource, by using DependsOn. Otherwise, the Deployment resource may be executed before the Method resource. 

7. Add an Outputs section to return the final URL for our REST API:
   ```yaml
   Outputs:
     SampleEndpoint:
       Description: 'Sample Endpoint'
       Value: !Sub
           - https://${API_ID}.execute-api.${AWS::Region}.amazonaws.com/dev/greeting/Heartin
           - API_ID: !Ref MyFirstRestAPI
   ```        
   Here, we use the intrinsic function Sub to create the final endpoint, using the pseudo-variable AWS::Region and the intrinsic function Ref.

8. Create a `cloudformation` stack with our template, as follows:
   ```bash
   aws cloudformation create-stack \
       --stack-name myteststack \
       --template-body file://your-first-rest-api-with-api-gateway-cf.yml \
       --region us-east-1 \
       --profile admin
   ```    
   Here, I have used the `template-body` option to read the template file from the local machine. You can use the `template-url` option to read the template file from an S3 bucket. 
   
   The `create-stack` command will immediately return a `stack-id`, which we can use to check the stack creation status and delete the stack. We can also use the stack names for these operations:
   ```json
   {
      "StackId": "arn:aws:cloudformation:us-east-1:□□□□□□□□□:stack/myteststack/e8201060-ac40-11e8-8038-503acac5c0fd"
   }
   ```  
9. Check the status of the stack creation by using the `describe-stacks` sub-command, until it shows `CREATE_COMPLETE`:
   ```bash
    aws cloudformation describe-stacks \
     --stack-name myteststack \
     --region us-east-1 \
     --profile admin
    ```       
    The `describe-stacks` command returns the current status of the stack (for example, `CREATE_IN_PROGRESS`, `CREATE_COMPLETE`, or `DELETE_COMPLETE`). If the stack creation completes successfully, it will return a status of <sphttps://packt-type-cloud.s3.amazonaws.com/uploads/sites/2819/2019/01/86d4cc9e-1a66-4aa3-ba42-f0d5dc363b52.pngan>`CREATE_COMPLETE`, along with the Outputs section with our sample URL:

    ```json
    {
      "StackId": "arn:aws:cloudformation:us-east-1:□□□□□□□□□:stack/myteststack/e8201060-ac40-11e8-8038-503acac5c0fd",
     "StackName": "myteststack",
     "Description": "Building API with AWS CloudFormation",
     "CreationTime": "2018-08-30T10:41:34.755Z",
     "RollbackConfiguration": {},
     "StackStatus": "CREATE_COMPLETE",
     "DisableRollback": false,
     "NotificationARNs": [],
     "Outputs": [
       {
         "OutputKey": "SampleEndpoint",
         "OutputValue": "https://q3d7dw2dqg.execute-api.us-east-1.amazonaws.com/dev/greeting/Heartin",
         "Description": "Sample Endpoint"
       }
     ],
     "Tags": [],
     "EnableTerminationProtection": false
    }
    ``` 
   You can verify the API by going to the URL in a browser. It should print the message Hello Heartin, as follows:
   ```json
   {
      "message": "Hello Heartin"
   }
   ```

10. You can delete the stack, and all of the resources that it created will automatically be cleaned up:
    ```bash
    aws cloudformation delete-stack \
     --stack-name myteststack \
     --region us-east-1 \
     --profile admin
    ```  


## How it works...
In this recipe, we created our first REST API using Amazon CloudFormation. In the previous recipe, *Building your first API using the AWS CLI*, we created a similar API, but with the AWS CLI. Since the properties related to API Gateway API creation were already discussed in that recipe, I will not repeat them here.

We introduced a new CloudFormation template component: `Outputs`. We also used a sub-function in its value field, in order to derive a sample API endpoint. The maximum output that you can define in a template is 60. Its export parameter (not used here) can be used to specify an export name for the `Outputs` and reference it from another stack (a **cross-stack reference**).    


## There's more...
We deployed our CloudFormation template from the AWS CLI. You can also use the CloudFormation designer for template validation, or to see the template components visually, in a design view. We used the CloudFormation designer to validate and deploy our template in Chapter 1, Getting Started with Serverless Computing on AWS. 

## See also
* Refer to *Building your first API using the AWS CLI* recipe to learn about the API creation properties and REST and HTTP basics.
* To understand CloudFormation basics, you may refer to the *There's more* section of the recipe *Building your first Lambda with Amazon CloudFormation* from Chapter 1, *Getting Started with Serverless Computing on AWS*.
* For additional CloudFormation concepts, such as pseudo parameters, you can refer to the recipe *Using AWS SDK, Amazon CloudFormation and AWS CLI with Lambda* of Chapter 1, *Getting Started with Serverless Computing on AWS*.
* For additional theory on the Outputs section, you may refer to https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/outputs-section-structure.html.