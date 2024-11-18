# Your first DynamoDB table
In this recipe, we will create our first DynamoDB table. Amazon DynamoDB is the primary database in AWS for building serverless applications. DynamoDB is a fully managed NoSQL database and you do not have to manage any servers. Unlike most NoSQL databases, DynamoDB, also supports consistent reads, but with an additional cost. 

Attributes in DynamoDB are synonymous with columns, and items are synonymous with rows in a relational database. However, there is no table-level schema in DynamoDB. You can have different set of attributes in different items (rows). You can also have an attribute with the same name but different types in different items. 

## How to do it...
We will create a simple table, check its properties, update it, and finally delete the table. We will first use CLI commands to create the table and then use a CloudFormation template to do the same. We will also use CLI commands to check the created table. We will use the AWS SDK to do this in a later recipe.

### Creating a table using CLI commands
1.  We can create a simple DynamoDB table using the aws dynamodb create-table CLI command as follows:
    ```bash
    aws dynamodb create-table \
    --table-name my_table \
    --attribute-definitions 'AttributeName=id, AttributeType=S' 'AttributeName=datetime, AttributeType=N' \
    --key-schema 'AttributeName=id, KeyType=HASH' 'AttributeName=datetime, KeyType=RANGE' \
    --provisioned-throughput 'ReadCapacityUnits=5, WriteCapacityUnits=5' \
    --region us-east-1 \
    --profile admin
    ```
    Here, we define a table named `my_table` and use the `attribute-definitions` property to add two fields: `id` of type string (denoted by `S`) and `datetime` of type number (denoted by `N`). We then define a partition key (or hash key) and a sort key (or range key) using the `key-schema` property. We also define the maximum expected read and write capacity units per second using the `provisioned-throughput` property. I have specified the region even though `us-east-1` is the default. 
2. List tables using the aws dynamodb list-tables CLI command to verify our table was created:
    ```bash
    aws dynamodb list-tables \
    --region us-east-1 \
    --profile admin
    ```
3. Use the aws dynamodb describe-table CLI command to see the table properties:
    ```bash
    aws dynamodb describe-table \
    --table-name my_table \
    --profile admin
    ```
    The initial part of the response contains the table name, attribute definitions, and key schema definition we specified while creating the table:
    The later part of the response contains `TableStatus`, `CreationDateTime`, `ProvisionedThroughput`, `TableSizeBytes`, `ItemCount`, `TableArn` and `TableId`:

4. You may use the aws dynamodb update-table CLI command to update the table:
    ```bash
    aws dynamodb update-table \
    --table-name my_table \
    --provisioned-throughput 'ReadCapacityUnits=10, WriteCapacityUnits=10' \
    --profile admin
    ```
5. Finally, you may delete the table using aws dynamodb delete-table:
    ```bash
    aws dynamodb delete-table \
    --table-name my_table \
    --profile admin
    ```
> We will be reusing this table in a later recipe when we work with data. If you are continuing with other recipes in this chapter now, you may delete the table after completing those recipes.

### Creating a table using a CloudFormation template
We will see the components of the CloudFormation template needed for this recipe. The completed template file is available with the code files.
1. Start creating the CloudFormation template by defining the template format, the version, and a description:
    ```yaml
    ---
    AWSTemplateFormatVersion: '2010-09-09'
    Description: Your First DynamoDB Table
    Define the Resources section with the DynamoDB Table type:
    Resources:
    MyFirstTable:
        Type: AWS::DynamoDB::Table
    ```    
2. Define the properties section with the essential properties: TableName, ProvisionedThroughput, KeySchema, and AttributeDefinitions:
    ```yaml
    Properties:
    TableName: my_table
    ProvisionedThroughput:
        ReadCapacityUnits: 1
        WriteCapacityUnits: 1
    KeySchema:
        -
        AttributeName: id
        KeyType: HASH
        -
        AttributeName: dateandtime
        KeyType: RANGE
    AttributeDefinitions:
        -
        AttributeName: id
        AttributeType: S
        -
        AttributeName: dateandtime
        AttributeType: N
    ```
    > Properties and their values are the same as we saw with the AWS CLI commands earlier. You can use the AWS CLI dynamodb command actions list-tables and describe-table to check the created table.
4. Update the table properties with the CloudFormation template:
   * Change `ReadCapacityUnits` and `WriteCapacityUnits` in the template to 5 for each. You can then update the stack using the `aws cloudformation update-stack` CLI command:
    ```bash
    aws cloudformation update-stack \
        --stack-name myteststack \
        --template-body file://resources/your-first-dynamodb-table-cf-template-updated.yml \
        --region us-east-1 \
        --profile admin
    ```    
    > Whenever an update is made, CloudFormation compares the template with the existing stack and updates only those resources that are changed. This is the first time we are using the update-stack action in this book. 
5. Verify the table update using the `aws dynamodb describe-table` CLI command. 
6. Delete the stack using the `aws cloudformation delete-stack` CLI command. As mentioned earlier, the other recipes in the chapter use this table, so if you are planning to continue with other recipes now, you may delete the table after completing them.

## How it works...
We used the following DynamoDB CLI command actions in this recipe: create-table, list-tables, describe-table, update-table, and delete-table. We use the corresponding components and properties within our CloudFormation template as well. Some of these options will become clear after you read the following notes.
### DynamoDB data model 
Data in DynamoDB is stored in tables. A table contains items (similar to rows), and each item contains attributes, (similar to columns). Each item can have a different set of attributes and the same attribute names may be used with different types in different items. 

DynamoDB supports the datatypes string, number, binary, Boolean, string set, number set, binary set, and list. 

DynamoDB does not have a JSON data type; however, you can pass JSON data to DynamoDB using the SDK and it will be mapped to native DynamoDB data types.

You can also define indexes (global secondary indexes and local secondary indexes) to improve read performance. 

### Data model limits
The following are some of the important limits in the DynamoDB data model:
* There is an initial limit of 256 tables per region for an AWS account, but this can be changed by contacting AWS support.
* Names for tables and secondary indexes must be at least three characters long, but no more than 255 characters. Allowed characters are A-Z, a-z, 0-9, _ (underscore), - (hyphen), and . (dot).
* An attribute name must be at least one character long, but no greater than 64 KB long. Attribute names must be encoded using UTF-8, and the total size of each encoded name cannot exceed 255 bytes.
* The size of an item, including all the attribute names and attribute values, cannot exceed 400 KB.
* You can only create a maximum of five local secondary indexes and five global secondary indexes per table.

For a complete list of DynamoDB limits, refer to https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html. 

### DynamoDB keys and partitions
Each item is identified with a primary key, which can be either only the partition key if it can uniquely identify the item, or a combination of partition key and sort key. The partition key is also called a hash key and the sort key is also called a range key. Primary key attributes (partition and sort keys) can only be string, binary, or number. 

Initially, a single partition holds all table data. When a partition's limits are exceeded, new partitions are created and data is spread across them. Current limits are 10 GB storage, 3,000 RCU, and 1,000 WCU. Data belonging to one partition key is stored in the same partition; however, a single partition can have data for multiple partition keys. The partition key is used to locate the partition and the sort key is used to order items within that partition.

### Read and write capacity units
We specified the maximum read and write capacity units for our application per second, referred to as **read capacity unit (RCU)** and **write capacity unit (WCU)**. We also updated our RCU and WCU. Updating the table properties is an asynchronous operation and may take some time to take effect. We will see throughput provisioning in detail in another recipe. 

### Waiting for asynchronous operations
The CLI commands `create-table`, `update-table`, and `delete-table` are asynchronous operations. The control returns immediately to the command line, but the operation runs asynchronously.

To wait for table creation, you can use the `aws dynamodb wait table-exists --table <table-name` command, which polls the table until it is active. The `wait table-exists` command may be used in scripts to wait until the table is created before inserting data. Similarly, you can wait for table deletion using the `aws dynamodb wait table-not-exists --table <table-name` command, which polls with `describe-table` until `ResourceNotFoundException` is thrown. Both the wait options poll every 20 seconds and exit with a 255 return code after 25 failed checks.

### Other ways to create tables
In this recipe, we created our table by specifying the properties, such as `attribute-definitions`, `key-schema`, `provisioned-throughput`, and so on. Instead, you can specify a JSON snippet or JSON file using the `cli-input-json` option. The `generate-cli-skeleton` option returns a sample template as required by the `cli-input-json` option.

In this recipe, we created a table using the AWS CLI and CloudFormation. You can also create DynamoDB tables from Java code using the AWS SDK, as we will see in a later recipe. However, in most real-world cases, CloudFormation templates are used to create and provision tables, and the AWS SDK is used to work with data items. 

## There's more...
Let's first see some features and limitations of DynamoDB. We will also see some theory on the `LSI` and `GSI`.
### DynamoDB features
The following are some of the important features of DynamoDB:
* DynamoDB is a fully managed NoSQL database service. There are no servers to manage.
* DynamoDB has the characteristics of both the key-value and the document-based NoSQL families. 
* Virtually no limit on throughput or storage. It scales very well, but according to the provisioned throughout configuration. 
* DynamoDB replicates data into three different facilities within the same region for availability and fault tolerance. You can also set up cross-region replication manually.
* It supports eventual consistency reads as well as strongly consistent reads.
* DynamoDB is schemaless at the table level. Each item (rows) can have a different set of elements. Even the same attribute name can be associated with different types in different items.
* DynamoDB automatically partitions and re-partitions data as the table grows in size.
* You can store JSON and then do nested queries on that data using the AWS SDK.
* Data is stored on SSD storage.
* DynamoDB supports atomic updates and atomic counters.
* DynamoDB supports conditional operations for put, update, and delete.

### DynamoDB general limitations 
Here are some of the general limitations of DynamoDB:
* DynamoDB does not support complex relational queries such as joins or complex transactions. 
* DynamoDB is not suited for storing a large amount of data that is rarely accessed. S3 may be better suited for such use cases.
* You cannot select the Availability Zone for your DynamoDB table. 
* Default replication of data for availability and fault tolerance is only within a region.

### Local and global secondary indexes
You can define LSI and GSI for your tables to improve the read performance. An LSI can be considered as an alternate sort key for a given partition-key value. A GSI contains attributes from the base table and organizes them by a primary key that is different from that of the base table. Secondary indexes are useful when you want to query based on non-key parameters. You can create them with the CLI as well as CloudFormation templates. There is a limit of five LSIs and five GSIs per table.

You can read and learn more about LSIs and GSIs from the following links:
* https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LSI.html
* https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html

## See also
https://aws.amazon.com/rds/aurora
