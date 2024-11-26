# Setting up CloudFront for an S3 website
**CloudFront** is a CDN web service from Amazon that caches data across the globe, through data centers known as **edge locations**. These edge locations speed up the distribution of static and dynamic web content originating in one location in one region, such as an S3 bucket, to users in other regions.

## Getting ready
You will need an S3 bucket configured as a website in order to follow this recipe. You can follow the recipe [Setting up an S3 static website](../setting-up-s3-static-website/README.md) to set up an S3 bucket as a static website. 

## How to do it...
You can set up CloudFront through AWS CLI commands, CloudFormation, or the AWS Management Console. In this recipe, we will look at how to set up CloudFront for an S3 bucket configured as a static website, through CLI commands and CloudFormation template. 

### AWS CLI commands
You can create a CloudFront distribution with the AWS CLI by either passing a distribution config, or by specifying the original domain name (for example, an S3 bucket name). In this recipe, we will use the shorter option:

1. Create the CloudFront distribution by using the `create-distribution` command with `aws cloudfront`:
    ```bash
    aws cloudfront create-distribution \
    --origin-domain-name qnatime.s3.amazonaws.com \
    --default-root-object index.html \
    --profile admin
    ```  
    This command will immediately return the ID of the distribution and the defaults that are considered:
    ```json
    {
    "Location": "https://cloudfront.amazonaws.com/2018-06-18/distribution/E1ZX6JAV6EDQVO",
    "ETag": "E2D80BE981CCBHB",
    "Distribution": {
        "Id": "E1ZX6JAV6EDQVO",
        "ARN": "arn:aws:cloudfront::218317422462:distribution/E1ZX6JAV6EDQVO",
        "Status": "InProgress",
        "LastModifiedTime": "2018-12-04T16:19:18.742Z",
        "InProgressInvalidationBatches": 0,
        "DomainName": "d39sfuvkc6hh8d.cloudfront.net",
        "ActiveTrustedSigners": {
        "Enabled": false,
        "Quantity": 0
        }
    }
    }
    ```
    The `Etag` in the response from the CloudFront command is needed to make further commands, such as update or delete requests, from the CLI. The response also contains the `DistributionConfig` property, which contains all of the properties related to the distribution. This should take some time to complete.

2. Check the status by using the `aws cloudfront get-distribution` command:
    ```
    aws cloudfront get-distribution \
        --id E1ZX6JAV6EDQVO \
        --profile admin
    ```    
    If it is successful, the status will change to Deployed in the response:
    ```json
        "Status": "Deployed",
    ```
    The remainder of the response is similar to the previous one. The `DistributionConfig` property within the response will be discussed in detail later on.

3. Execute the CloudFront domain URL in a browser, as shown in the following screenshot:

#### Understanding the DistributionConfig defaults in the response
Let's go through the `DistributionConfig` property that we received as part of the response, and try to understand the structure and defaults for the important properties of `DistributionConfig`. I have only displayed screenshots from the response for the important sections, and will mention other properties by name.

`DistributionConfig` starts with the properties `CallerReference`, `Aliases`, and the `DefaultRootObject` (set as `index.html`). Next, it contains the `Origins` property, with our bucket details:
```json
"Origins": {
  "Quantity": 1,
  "Items": [
    {
      "Id": "qnatime.s3.amazonaws.com-1543940355-477594",
      "DomainName": "qnatime.s3.amazonaws.com",
      "OriginPath": "",
      "CustomHeaders": {
        "Quantity": 0
      },
      "S3OriginConfig": {
        "OriginAccessIdentity": ""
      }
    }
  ]
}
```

Next, it contains the `DefaultCacheBehavior` section that starts with `TargetOriginId`, `ForwardedValues`, `TrustedSigners`, and `ViewerProtocolPolicy` (set as allow-all).

The `DefaultCacheBehavior` section continues with `MinTTL`, `AllowedMethods`, `SmoothStreaming`, `DefaultTTL`, `MaxTTL`, `Compress`, `LambdaFunctionAssociations`, and `FieldLevelEncryptionId`:
```json
{
  "MinTTL": 0,
  "AllowedMethods": {
    "Quantity": 2,
    "Items": [
      "HEAD",
      "GET"
    ]
  },
  "CachedMethods": {
    "Quantity": 2,
    "Items": [
      "HEAD",
      "GET"
    ]
  },
  "SmoothStreaming": false,
  "DefaultTTL": 86400,
  "MaxTTL": 31536000,
  "Compress": false,
  "LambdaFunctionAssociations": {
    "Quantity": 0
  },
  "FieldLevelEncryptionId": 1  ""
}
```

Furthermore, we can see the `CacheBehaviors`, `CustomErrorResponses`, `Comment`, `Logging`, and `PriceClass` sections (set as `PriceClass_All`). 

Finally, there are the `Enabled`, `ViewerCertificate`, `Restrictions`, `WebACLId`, `HttpVersion`, and `IsIPV6Enabled` sections:
```json
{
  "Enabled": true,
  "ViewerCertificate": {
    "CloudFrontDefaultCertificate": true,
    "MinimumProtocolVersion": "TLSv1",
    "CertificateSource": "cloudfront"
  },
  "Restrictions": {
    "GeoRestriction": {
      "RestrictionType": "none",
      "Quantity": 0
    }
  },
  "WebACLId": "",
  "HttpVersion": "http2",
  "IsIPV6Enabled": true
}
```

### The CloudFormation template
Unlike with the CLI commands, there is no shorthand way to create a CloudFront distribution with a CloudFormation template without specifying the distribution config parameters. I will, however, only add the essential parameters in this recipe:
1. Start the template with the template version and a description (optional).
2. Create a resource of the type `AWS::CloudFront::Distribution`:
    ```yaml
    Resources:
    MyCloudFrontDistribution:
        Type: AWS::CloudFront::Distribution
        Properties:
        DistributionConfig:
            Origins:
            - DomainName: quizzer.cloud.s3.amazonaws.com
            Id: myS3Origin
            S3OriginConfig:
                OriginAccessIdentity: ''
            Enabled: 'true'
            Comment: 'CloudFront Distribution for S3 Bucket'
            DefaultRootObject: index.html
            DefaultCacheBehavior:
            TargetOriginId: myS3Origin
            ForwardedValues:
                QueryString: 'false'
                Cookies:
                Forward: none
            ViewerProtocolPolicy: allow-all
    ```          
3. Add an `Outputs` section to return the CloudFront distribution ID and the CloudFront domain name:
    ```yaml
    Outputs:
    CloudFrontDistributionId:
        Value: !Ref MyCloudFrontDistribution
        Description: 'CloudFront distribution id'
    CloudFrontDomain:
        Value: !GetAtt MyCloudFrontDistribution.DomainName
        Description: 'CloudFront distribution domain name'
    ```    
4. Execute the stack, using the `create-stack` command.
    It will take some time for the distribution to be created. You can check the status by using the describe-stacks command. Once it has completed, you will get a response with the `Outputs` section, as follows:
    ```json
    "Outputs": [
        {
            "OutputKey": "CloudFrontDistributionId",
            "OutputValue": "E3CNIYON2WR354",
            "Description": "CloudFront distribution id"
        },
        {
            "OutputKey": "CloudFrontDomain",
            "OutputValue": "d130e9lj3phwkc.cloudfront.net",
            "Description": "CloudFront distribution domain name",
            "ExportName": "CloudFrontDomainName"
        }
    ]
    ```
5. Execute the CloudFront domain name in a browser, and verify whether the S3 static website has loaded:
## How it works...
We created a CloudFront distribution for an existing bucket that was configured as a static website. We created the bucket in a previous recipe. With AWS CLI commands, you can either pass in just the original server and accept the defaults for the other options, or you can pass in a distribution config JSON file with all of the required configurations. These options are mutually exclusive. In this recipe, we only specified the original server S3 bucket for the AWS CLI command version. However, with the CloudFormation template, we still had to use the distributed config, with the essential parameters. 

## There's more...
We only specified the original server when creating the CloudFront distribution with AWS CLI commands. However, to update or delete a CloudFront distribution, you also need to specify the `Etag` received in the previous step, from the command line. For updates, including enabling or disabling the CloudFront distribution, we will need to provide the distribution configurations with the essential parameters. We will see them in the next recipe. 

## See also
* https://docs.aws.amazon.com/cli/latest/reference/cloudfront/index.html#cli-aws-cloudfront
* https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html