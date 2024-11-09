# Your first Lambda with serverless framework
Serverless is an open source command line utility framework for building and deploying serverless applications. Serverless supports multiple cloud providers such as Amazon Web Services, Microsoft Azure, IBM OpenWhisk, Google Cloud Platform, Kubeless, Spotinst, Webtasks, and Fn. 

In this recipe, we will use the Serverless framework to develop, deploy, invoke, check logs, and finally remove a simple hello world Lambda function on the AWS cloud platform.

## Getting ready
Two dependencies are needed for the Serverless framework: node.js and AWS CLI. For installing AWS CLI, you may refer to the 'Deploying and Invoking Lambda with AWS CLI' recipe. You can install node using node packet as given at https://nodejs.org/en/download/package-manager.

You need to create a user for Serverless in AWS. It is a general practice to use the name `serverless-admin` and give administrator permission. It is not a very good practice to create users with administrator access, but currently that is the easiest way to work with Serverless. You should be careful about storing and using these credentials.

* official site: https://www.serverless.com/framework/docs/tutorial
* https://github.com/serverless/examples
## How to do it...
Let us create a simple Lambda using the Serverless framework:
1. Install Serverless in your machine using npm:
   ```bash
   npm install -g serverless
   ```
1. Configure Serverless with user credentials:
   ```bash
   serverless config credentials --provider aws --key <access key> --secret <secret access key> --profile serverless-admin
   ```
   You should get a success message stating that keys were stored under the `serverless-admin` profile.
   > The sls command is the shorthand of the Serverless command.
1. Create a Lambda function based on Java and Maven:
   ```bash
   sls create --template aws-java-maven --path hello-world-java-maven
   ```
   It creates a `hello-world-java-maven` folder, with `pom.xml` and `serverless.yml` files, and the src folder. You may open this Maven project in your IDE of choice. The auto-generated files looks as shown here in my IDE:
   As you can see, Serverless has created a bit more than a simple `hello world`. Serverless takes care of most of the things we did manually, including creating a role, setting memory, setting timeout, and so on. 
   Add a user profile and region to `serverless.yml`. The region is optional if you are using the default region:
   ```yaml
   provider: 
     name: aws
     runtime: java17
     profile: serverless-admin
     region: us-east
   ```
   Build the jar file with: 
   ```bash   
    mvn clean package
   ```
   Deploy the jar file to AWS:
   ```bash   
    sls deploy -v
   ```
   You can log in to the AWS console and verify the new Lambda service. From the log statements, you can see that Serverless framework internally makes use of CloudFormation. You can verify the same from AWS Management console. 
1. Invoke the function from sls:  
   ```bash
   sls invoke -f hello -l
   ```  
   Option `-f` specifies the function name, and `-l` specifies that logs need to be printed to terminal. The function name to invoke is `hello` and is available in the `serverless.yml` file. You can see the output and logs on the terminal. 
1.Checking logs from the CLI:
   ```bash
   sls logs -f hello -t
   ```
   Option `-f` specifies the function name and `-t` denotes to tail the logs. You can now run the invoke command from the other terminal and see the logs being printed.
1. Now, clean up everything:
   ```bash
   sls remove
   ```
1. Log in to AWS Management console and verify that everything is cleaned up.   