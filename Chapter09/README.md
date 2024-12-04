# Serverless Programming Practices and Patterns
This chapter will cover the following:
* [Enabling **Cross-Origin Resource Sharing (CORS)** for the API and testing with CodePen](./enable-cors-for-the-api/README.md)
* [Implementing and testing Cognito operations with the JavaScript SDK](./cognito-operations-with-javascript-sdk/README.md)
* Federated identity with Amazon Cognito
* Creating a **Secure Sockets Layer (SSL)** certificate with **AWS Certificate Manager (ACM)**
* Fan-in and fan-out architectural patterns with AWS

## Introduction
In the previous chapters, we saw many AWS services that can be used for building serverless applications. In this chapter, we will explore some general practices and patterns that involve one or more of those AWS services. We have been using AWS CLI APIs and Java Lambdas until now. Services such as API Gateway and Cognito generally interact mostly with UI components and hence we will discuss their use with the JavaScript SDKs. We will also enable and use CORS, and then test our JavaScript SDK code from a browser using CodePen. 

Furthermore, we will see how to create a federated identity with Cognito. We did not try this recipe in Chapter 4, Application Security with Amazon Cognito, as federated identity requires a valid domain name. We registered a domain name in Chapter 5, Web Hosting with S3, Route53, and CloudFront. We will then discuss a pattern called the fan-out pattern, which involves the Simple Notification Service (SNS) and the Simple Queue Service (SQS). We covered SQS and SNS recipes in Chapter 6, Messaging and Notifications with SQS and SNS. Finally, we will conclude with a recipe on certificate generation using ACM. 

> This chapter tries to bridge the gap between the AWS serverless services we learned and how they are actually used in real-world projects. This chapter assumes that you are comfortable with all the services discussed in the previous chapters. We may not discuss in detail all the code and theory behind the practices and patterns we discuss, especially those that were already discussed.