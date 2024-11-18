# Data Storage with Amazon DynamoDB
This chapter will cover the following topics:

* [Your first DynamoDB table](./your-first-dynamodb-table/README.md)
* [Throughput provisioning examples](./Throughput-provisioning-examples/README.md)
* [Working with data from the CLI](./working-with-data-from-cli/)
* Using the DynamoDB SDK from Lambda
* [Creating tables from Lambda](./creating-tables-from-lambda/)
* [Adding data from Lambda](./adding-data-items-from-lambda/)
* [Reading data from Lambda](./reading-data-items-from-lambda/)


## Introduction
In this chapter, we will learn to build a data store for our serverless applications using Amazon DynamoDB. DynamoDB is a fully managed NoSQL database service and is the primary data store in AWS for building serverless applications. If you have strict relational use cases, you may also consider Amazon Aurora, which is a fully managed relational database service. If you need more analytical features, such as aggregations, along with NoSQL flexibility, you may explore the Amazon Elasticsearch service.

A relational data model table consists of rows (records) with a fixed number of columns, and is queried using Structured Query Language (SQL). Different NoSQL databases are classified into different families, such as key-value store, document store, columnar, graph, and so on, and have different query mechanisms. DynamoDB has characteristics of both key-value and document-databased NoSQL families. Relational databases follow the ACID model for consistency and NoSQL databases generally follow the BASE model. 

In previous chapters, we saw different ways to work with Lambda and API gateway, such as the management console, the SDK, the CLI, and CloudFormation templates. From now on, our focus will be on using CloudFormation templates and the AWS SDK, along with essential CLI commands.