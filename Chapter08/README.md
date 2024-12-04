# Monitoring and Alerting with Amazon CloudWatch
This chapter will cover the following topics:
* Checking logs, insights, and metrics (Console)
* [Your first custom metric (AWS CLI)](./your-first-custom-metric/README.md)
* [Setting up CloudWatch alarms (AWS CLI)](./setting-up-cloudwatch-alarms/README.md)
* [CloudWatch alarms with dimensions (AWS CLI)](./cloudwatch-alarms-with-dimensions/README.md)
* [Using CloudWatch metric log filters (AWS CLI and Console)](./filtering-log-events/README.md)

## Introduction
DevOps is a mindset where the development team and the operations team responsible for a product or service collaborate together as a single, larger team to achieve their common goal of making a project successful. The actual implementation of DevOps practices differs from DevOps team to DevOps team. In practice, while core development activities may still be done by programmers and core operations activities may be still done by operations experts, many activities such as monitoring, debugging, and so on may be shared between both sides based on the work capacity available. 

AWS provides many services that can help in monitoring and debugging projects deployed on the AWS cloud. Amazon CloudWatch is the most popular of these services, and without it we cannot be successful with AWS projects. CloudWatch can perform many functions, such as monitoring and alerting, gathering data and preparing metrics, visualizing the data sent to it using graphs, and so on. We have used CloudWatch for checking logs, and in Chapter 1, Getting Started with Serverless Computing on AWS, we used it for setting a billing alarm. 

In previous chapters, we looked at services that can help us build serverless web applications, as well as services that extend the basic functionality with capabilities such as messaging, analytics, machine learning, and natural language processing. In this chapter, we will briefly discuss recipes for the CloudWatch service to start monitoring and debugging the services we looked at in the previous chapters. By doing this, we will better understand the role of CloudWatch in following DevOps practices with AWS.

Even programmers still spend a large amount of time monitoring and debugging the logs from the Management Console, and so most of the recipes in this chapter will use the Management Console with or without the corresponding AWS CLI commands.