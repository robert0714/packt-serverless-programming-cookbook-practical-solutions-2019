# Application Security with Amazon Cognito
This chapter will cover the following topics:
* [Creating a Cognito user pool](./creating-a-cognito-user-pool/README.md) (AWS CLI, CloudFormation)
* [Server-side authentication flow](./cognito-server-side-authentication-flow/README.md) (AWS CLI, CloudFormation)
* [Client-side authentication flow](./cognito-client-side-authentication-flow/README.md) (AWS CLI, CloudFormation)
* [User sign-up flow with Cognito](./user-sign-up-flow-with-cognito/README.md) (AWS CLI)
* [Working with groups](./working-with-groups/README.md) (AWS CLI)
* [Integrating Cognito in an API gateway](./integrating-cognito-with-api-gateway/README.md) (AWS CLI, CloudFormation)
* [User sign-up with SMS verification and MFA](./user-sign-up-with-sms-and-mfa-verification/README.md) (AWS CLI, CloudFormation)

## Introduction
In the previous chapters, we learned how to create serverless functions, REST APIs, and data stores. In this chapter, we will use Amazon Cognito to provide application-level security and user management, including user sign-in, sign-up, and access control. We will also discuss Cognito's integration with API gateway APIs.

Like we did in the other chapters, we will discuss provisioning resources using both AWS CLI commands and CloudFormation templates. For application flows, we will mostly use the AWS CLI, without Java Lambda code. In general, Cognito is used with the frontend, mostly using the JavaScript SDK, as we will see in Chapter 9, Serverless Programming Practices and Patterns.

The following are the prerequisites required for completing the recipes in this chapter:
1. A working AWS account
2. Configuring the AWS CLI, as discussed in the recipe *Your first Lambda with AWS CLI*, in Chapter 1, *Getting Started with Serverless Computing on AWS* 
3. A basic understanding of security concepts