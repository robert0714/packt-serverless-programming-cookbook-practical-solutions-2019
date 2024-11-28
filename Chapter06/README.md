# Messaging and Notifications with SQS and SNS
This chapter will cover the following topics:
* [Your first SQS queue (AWS CLI + CloudFormation)](./your-first-sqs-queue/README.md)
* [Creating an SQS queue and sending messages with SDK (Java)](./creating-sqs-queue-and-sending-message-with-sdk/README.md)
* [Receiving and sending SQS messages in batches with SDK (Java)](./receiving-and-sending-sqs-messages-in-batch-with-sdk/README.md)
* [Invoking Lambda with SQS events (Java)](./invoking-lambda-with-sqs-event/)
* [Your first SNS topic for email and SMS (AWS CLI + CloudFormation)](./your-first-sqs-queue/)
* [Publishing to SNS topic with SDK (Java)](./publishing-to-sns-topic-with-sdk/)
* [Invoking Lambda with SNS events (Java)](./invoking-lambda-with-sns-event/)

## Introduction
In the previous chapters, we learned about various essential components for building a standard serverless web application such as functions, Rest API, datastore, user management, hosting, and domain registration. As discussed at the end of the previous chapter, a real-world application may not always be a web application, or may be extended with additional capabilities.

Until now, we were invoking Lambda from the API gateway. However, Lambdas may also be invoked reactively as a result of some triggers based on states of other services. In this chapter, we will first discuss adding messaging and notification support to serverless applications. Later, we will see how to trigger Lambdas based on state changes in various other services.

The following are the prerequisites for this chapter:
* A working AWS account
* Configuring AWS CLI as discussed in the [Your first Lambda with AWS CLI](../Chapter01/your-first-lambda-with-aws-cli/README.md) recipe of Chapter 1, [Getting Started with Serverless Computing on AWS](../Chapter01/README.md)

