# Building Serverless REST APIs with API Gateway
## This chapter will cover the following topics:

* [Building your first API using the AWS CLI](./your-first-api-using-aws-cli/README.md)
* [Building your first API using Amazon CloudFormation](./your-first-api-using-cloud-formation/README.md)
* [Building your first API with Lambda integration](./your-first-api-with-lambda-integration/README.md)
* [Building and testing your first POST API method](./building-and-testing-your-first-post-api-method/README.md)
* [Mapping requests and responses using mapping templates](./mapping-requests-and-responses-using-mapping-templates/)
* [Validating request payloads with models](./validating-input-and-output-with-models/README.md)
* [Using Lambda and APIs with proxy integration](./api-with-lambda-proxy-integration/README.md)

## Introduction
An application programming interface (API) is a set of methods that allow us to programmatically access a component. APIs can be built for different layers, such as the web (http/rest), database, and operating system layers, and so on. A representational state transfer (REST) API is an API that uses REST principles and HTTP protocol to build web APIs that can be programmatically consumed by clients over a network, such as the internet. 

Amazon API Gateway is the primary service within AWS for building serverless, scalable, and secure REST APIs. It acts as a gateway between your application and the outside world. You only pay for the API calls and data that are transferred out, and you do not have to maintain a server. It also provides support for testing, authorization, API version management, deployment, and maintaining and monitoring your REST APIs. 

Amazon API Gateway is usually used with AWS Lambda, in order to build Serverless applications. In this chapter, we will discuss some core use cases of the API gateway, such as building, deploying, and testing APIs that interact with AWS Lambda. Additional use cases, such as security and integration with a user interface, will be discussed in later chapters. We will also discuss the REST principles and HTTP essentials. 