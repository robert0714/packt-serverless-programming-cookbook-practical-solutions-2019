# Web Hosting with S3, Route53, and CloudFront
 This chapter will cover the following topics:
* [Setting up an S3 static website](./setting-up-s3-static-website/README.md)
* [Setting up CloudFront for an S3 website](./setting-up-cloudfront-for-s3-bucket/)
* [Registering a domain with Route 53](./registering-domain-name-with-route53/)
* Using domains registered with other registrars
* Creating a custom domain with an S3 static website 
* Creating a custom domain with CloudFront
* Using HTTPS with a CloudFront domain 

## Introduction
In the previous chapters, you saw how to create Serverless functions, add REST APIs, create data stores, and secure an application. In this chapter, we will look at recipes related to hosting a website, such as how to register domain names, host a static website, attach the custom domain for our S3 bucket, and use the CloudFront **Content Delivery Network (CDN)**. You already saw S3 in previous recipes. **Amazon Route 53** is a new service that we will introduce in this chapter. The Route 53 service is Amazon's DNS management service for registering domains and implementing routing strategies. 

Some Route 53 functionalities may be one-time activities (for example, domain registration), some may require user interaction at various stages (for example, domain ownership validation), and some may take a longer time to complete. For example, DNS propagation can take up to 24-48 hours. Therefore, these tasks are generally done from the AWS Management Console or by directly accessing the APIs, and less using CloudFormation. CloudFormation does not currently support domain registration, and it only has limited support for other DNS management activities. 