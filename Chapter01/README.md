# Getting Started with Serverless Computing on AWS
This chapter will cover the following topics:
* Getting started with the AWS platform
* [Your first AWS Lambda](./your-first-aws-lambda/README.md)
* [Your first Lambda with AWS CLI](./your-first-lambda-with-aws-cli/README.md)
* [Your first Lambda with Amazon CloudFormation](./your-first-lambda-with-cloud-formation/README.md)
* [Using AWS SDK, Amazon CloudFormation, and AWS CLI with Lambda](./using-aws-sdk-amazon-cloud-formation-aws-cli-with-lambda/README.md)
* [Dev practices: dependency injection and unit testing](./lambda-dev-practices-dependency-injection-unit-testing/README.md)
* [Your first Lambda with Serverless framework](./hello-world-java-maven/README.md)
## Getting ready
You can set up the parent project inside our parent folder (serverless in my case) by executing the following commands from the command line:

1. Clone our book's Github repository:
   ```bash
   git clone https://github.com/PacktPublishing/Serverless-Programming-Cookbook.git
   ```
2. Go inside the repository folder, go inside our project-specific parent project, and run mvn clean install:
   ```bash
   cd Serverless-Programming-Cookbook
   cd serverless-cookbook-parent-aws-java
   mvn clean install
   ```
