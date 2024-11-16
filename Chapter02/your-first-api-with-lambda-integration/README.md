# Building your first API with Lambda integration
In the previous two recipes in this chapter, we created an API with mock integration. We also discussed REST principles, HTTP essentials, and the AWS CLI commands and CloudFormation template components used. In this recipe, we will integrate an API gateway API with Lambda. The API is similar to the previous one (with a path parameter), but we will use an AWS integration instead of mock integration. 

## How to do it...
First, we will create a Lambda, and then we will invoke it from an API Gateway API by using AWS integration. We will look at how to use CLI commands, as well as a CloudFormation template, to create the API. 

### CLI commands
The options for the following commands have not changed much, aside from their names and descriptions: `create-rest-api`, `get-resources`, `create-resource`, `put-method-response`, `put-integration-response`, and `create-deployment`. We also added request-parameters to put-method, to create the path param, as required.

We used the `AWS` integration type. We also specified the URI in the format required for AWS integrations: `arn:aws:apigateway:<region>:lambda:path/2015-03-31/functions/<lambda arn>/invocations`. The `2015-03-31` refers to the latest API version of the AWS Lambda service. 

We defined the `request-templates` option of the `put-integration` sub-command to specify the mapping template for the JSON passed to Lambda from the API. Within the mapping template key is the content type, and the value is the mapping template for that content type. We also used the path and query parameter values within the mapping template. We will look at mapping templates in detail in a later recipe. 

The `integration-http-method` of the `put-integration` command specifies the HTTP method used by API Gateway to connect with the Lambda. The `integration-http-method` should be POST, for Lambda integration. This is not the HTTP method that we use to access our API endpoint from a browser (which is `GET`, as specified by `http-method`).

We did not specify `iam` roles or policies for API Gateway to talk to Lambda; instead, we used Lambda's add-permission command to allow our API to invoke it. We specifically gave `lambda:InvokeFunction` permission, but you can also give all of the permissions by using `lambda:*`. For logging to CloudWatch, you will still need to add a role with the required permissions. 

The `lambda add-permission` properties are as follows:
* `function-name` is the name of the Lambda function.
* `statement-id` is a unique number to identify this permission.
* `action` refers to the permitted actions.
* `principal` denotes the AWS service that is granted permission.
* `source-arn` is the arn of the resource invoking the function. You can specify * to denote that any of the part is matched (for example, `tyu4dw36th/*/*/lambdagreeting/{name}` matches any stage and any HTTP method).

### CloudFormation template components
As always, we will start with the template version and description. The options for the following resource types have not changed much: `RestApi`, `Resource`, and `Deployment`. The `Outputs` section is also the same as before. Within the method declaration, we used the AWS integration type, along with a URI in the required format.

Within the management console, you have a separate `Lambda Integration` option and AWS integration option. However, with CLI commands and CloudFormation templates, the AWS integration option is used for both cases. 

We have introduced a new intrinsic function: `Fn::ImportValue`. `Fn::ImportValue` gets the value of an output exported by another stack (in our case, the Lambda stack). Exports and imports are only allowed within regions, and export names must be unique within a region.

We also used a new type, `AWS::Lambda::Permission`, to add a permission to a Lambda. The options are similar to the CLI commands we used, but we have used CloudFormation intrinsic functions and variables to avoid any hardcoding. When working with API gateway, you only need to use `SourceArn` to specify the `SourceAccount`. However, if you are specifying a resource, such as an S3 bucket, you need to specify the `SourceAccount` option as well. 

## There's more...
We have now created our first API with a Lambda backend. We used API gateway's request and response modeling capabilities to structure the input and output from the Lambda. We can also configure API gateway to act as a proxy, in order to forward the request as it is to Lambda. We will see that in a later recipe.

## See also
* For more information on the intrinsic function `Fn::ImportValue`, you can refer to https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-importvalue.html
* For more information on `AWS::Lambda::Permission`, you can refer to https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-lambda-permission.html.