# Setting up an S3 static website
**Simple Storage Service (S3)** is an object store in AWS that allows us to store objects against keys. We already used S3 to deploy our Lambda code and within CloudFormation scripts, in earlier recipes. S3 can also be configured to host a static website. In this recipe, we will create an S3 bucket and configure it as a static website by using AWS CLI commands and CloudFormation scripts.

## Getting ready
A basic understanding of Amazon S3 is required for this section. We used S3 to upload our Lambda code in Chapter 1, Getting Started with Serverless Computing on AWS, and we discussed a few of its properties. A decent understanding of web technologies, such as HTML, would be beneficial, but is not mandatory.

Up to this point, we have only been using the default region, us-east-1. An S3 bucket can be created in a region closer to you. To demonstrate this, I will be using a different region, Mumbai (ap-south-1), for this recipe. However, this is not a requirement for the recipe. We will use the CloudFront CDN later in this chapter, in order to cache the results across locations.

## How to do it...
We will create one S3 static website bucket, and we will configure it as a website.

### AWS CLI commands
I will use a bucket name (`qnatime`, or a variation thereof) for the AWS CLI commands within this chapter.

> S3 bucket names are <font color=red>unique across all regions</font>. Please use a different domain name than the one I use in the recipes.
1. Create an S3 bucket, as follows:
    ```bash
    aws s3api create-bucket \
        --bucket qnatime \
        --create-bucket-configuration LocationConstraint=ap-south-1 \
        --region ap-south-1 \
        --profile admin
    ```    
    We need to specify the `LocationConstraint` explicitly, in addition to the `--region` option for non-US regions. This command will provide you with the location of the S3 bucket:
    ```json
    {
        "Location": "http://qnatime.s3.amazonaws.com/"
    }
    ``` 
    From this location and the knowledge of the region, you can derive the URL for our static website: `http://qnatime.s3-website.ap-south-1.amazonaws.com`. However, the website link will not work now, as we have not configured the bucket as a website.

    We are also using the `aws cli s3api` command, instead of the `aws cli s3` command that we were using hitherto. Some of the actions that we will perform will require more control, as provided by the `s3api` sub-command over the high-level s3 sub-command. 
2. Create an index document and an error document.
    An S3 static website requires you to provide two HTML files: an index document and an error document. The index document is the website landing page, and the error document is displayed in the case of an error. 

    Create a simple `index.html` file, with only an `<h1>` tag inside the body:
    ```html
    <body>
        <h1> Welcome to Q & A Time! </h1>
    </body>
    ```
    Similarly, you can also create a simple error.html file, with a different text within the `<h1>` tag: 
    ```html
    <body>
        <h1> Error page for Q & A Time! </h1>
    <body>
    ```
    Refer to the code files for the complete index.html and error.html files.
3. Upload the index and error documents, as follows:
    ```bash
    aws s3 cp resources/index.html s3://qnatime/index.html \
        --profile admin

    aws s3 cp resources/error.html s3://qnatime/error.html \
        --profile admin
    ``` 
4. Create a website configuration JSON file specifying the index and error filenames, as follows:
    ```json
    {
        "IndexDocument": {
            "Suffix": "index.html"
        },
        "ErrorDocument": {
            "Key": "error.html"
        }
    }
    ```
5. Create a static website specifying the website configuration JSON file, as follows:
    ```bash
    aws s3api put-bucket-website \
    --bucket qnatime.com \
    --website-configuration file://resources/s3-website-configuration.json \
    --profile admin
    ```
6. Create a bucket policy with read permission for everyone.
    By default, an S3 bucket and its objects do not provide read access to the public. However, for an S3 bucket to act as a website, all of the files need to be made accessible to the public. This can be done by using the following bucket policy:
    ```json
    {
    "Version":"2012-10-17",
    "Statement":[
        {
        "Sid":"PublicReadGetObjectAccess",
        "Effect":"Allow",
        "Principal": "*",
        "Action":["s3:GetObject"],
        "Resource":["arn:aws:s3:::qnatime/*"]
        }
    ]
    }
    ```
    Execute the bucket policy, as follows:
    ```bash
    aws s3api put-bucket-policy \
        --bucket qnatime \
        --policy file://resources/s3-website-policy.json \
        --profile admin
    ```
7. Execute the bucket website URL; the result will look like the following screenshot:
### The CloudFormation template
I will use a bucket named `quizzercloud` (or one of its variations) for all of the CloudFormation templates within this chapter:

1. Start the template with the template version and a description (optional).
2. Define a parameter for the bucket name:
    ```yaml
    Parameters:
    BucketName:
        Description: Bucket name for your website
        Type: String
    ```    
3. Define a resource for the bucket:
    ```yaml
    Resources:
    MyBucket:
        Type: AWS::S3::Bucket
        Properties:
        BucketName: !Ref BucketName
        AccessControl: PublicRead
        WebsiteConfiguration:
            IndexDocument: index.html
            ErrorDocument: error.html
    ```        
4. Define a bucket access policy that allows for everyone to access the bucket's contents:
    ```yaml
    WebsitePublicAccessPolicy:
    Type: AWS::S3::BucketPolicy
    Properties:
        Bucket: !Ref MyBucket
        PolicyDocument:
        Statement:
            -
            Action:
                - "s3:GetObject"
            Effect: "Allow"
            Resource:
                Fn::Join:
                - ""
                -
                    - "arn:aws:s3:::"
                    - !Ref MyBucket
                    - "/*"
            Principal: "*"
    ```
5. Add an `Outputs` section to return the URL of the S3 website (optional):
    ```yaml
    Outputs:
    S3WebsiteURL:
        Value: !Sub
        - http://${Bucket}.s3-website.${AWS::Region}.amazonaws.com
        - Bucket: !Ref MyBucket
        Description: URL for S3 static website
    ```    
6. Execute the CloudFormation template by passing the values for the parameters:
    ```bash
    aws cloudformation create-stack \
        --stack-name s3websitestack \
        --template-body file://resources/s3-static-website-cf-template.yml \
        --parameters ParameterKey=BucketName,ParameterValue=quizzercloud \
        --region ap-south-1 \
        --profile admin
    ```
7. Check the creation status by using the `aws cloudformation describe-stacks` command. If it is successful, you should get a response with an `Outputs` section, as follows:
    ```json
    "Outputs": [
        {
            "OutputKey": "S3WebsiteURL",
            "OutputValue": "http://quizzercloud.s3-website-ap-south-1.amazonaws.com",
            "Description": "URL for S3 static website"
        }
    ]
    ```
8. Once the stack creation has completed, you will need to upload the `index.html` and `error.html` files into the root bucket. Refer to the [AWS CLI commands section](#aws-cli-commands) or the code files for the command.
9. Finally, execute the S3 static website URL in a browser, as shown in the following screenshot:
## How it works...
To summarize, we did the following in this recipe:
1. We created an S3 bucket as a static website (for example, qnatime)
2. We added the index.html and error.html files
3. We added a bucket policy that allows for everyone to read the bucket
4. We verified the S3 static website from the browser
## There's more...
In the real world, an S3 website is usually pointed to by a custom domain (for example, `qnatime.com`). One restriction with this approach is that the bucket name and the custom domain have to be the same. We can work around this by using the CloudFront CDN. You will see that in a later recipe in this chapter. 

S3 website endpoints do not currently support HTTPS. We can, however, work around this, by configuring the CloudFront CDN over our website configuration, and then adding SSL support, utilizing **Amazon Certificate Manager (ACM)**. We will cover that in a later recipe within this book. 

## See also
* https://docs.aws.amazon.com/cli/latest/reference/s3/index.html
* https://docs.aws.amazon.com/cli/latest/reference/s3api/index.html