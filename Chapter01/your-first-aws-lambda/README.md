# Your first AWS Lambda
* We will create our first Lambda with Java as a Maven project. The javadoc comments and` package-info.java` files required for checkstyle checks from the parent are not shown here. We are also making use of the Maven shade plugin from the parent for generating the JAR files. You may refer to the code files for each recipe for the complete code:

* To package the Lambda as a JAR file, from the project root folder, run the following:
  ```bash
   mvn clean package
  ```

* Deploy the Lambda handler to the AWS:
1. Log in to the AWS console, and go to Lambda dashboard by clicking on Services and searching or selecting Lambda. Currently, it is under the compute category.
2. Create a Lambda function as follows:
   1. Click on Create Function.
   1. Select `Author From Scratch`, which is the default.
   1. Give a name, such as `myHelloWorldLambda`.
   1. Select `Java 17` as the runtime.
   1. Under Role, select `Create new role from one or more templates`.
   1. Give a role name, such as `myHelloWorldLambda`.
   1. Leave the field for specifying Policy templates blank.
   1. Click on Create Function. You should see a success message after a while.

* Upload the Lambda JAR:
  * Go to the Function code section and do the following:
   1. Select Code entry type as `Upload a .zip or .jar file`.
   1. Select Java 8 as the runtime.
   1. Specify the fully qualified class name with handler method name as the following:`tech.heartin.books.serverlesscookbook.HelloWorldLambdaHandler::handleRequest`.
   1. Click on Upload under Function package and select the `JAR` file. You can select the JAR whose name starts with `original-`.
   1. Click on Save to save with defaults for other fields.

* We can test the uploaded JAR:
   1. Select `Configure test events` from the Select a test event dropdown next to the Test button.
   1. Select Create new test event.
   1. Give a name for the event: `MyHelloWorldTest`.
   1. Within the JSON request content area, just specify your name, such as `Heartin`.
   1. Click on Create. If successful, it will take you to the `myHelloWorldLambda` function page.
   1. From the `myHelloWorldLambda` function page, select the test event, `MyHelloWorldTest`, next to the Test button, and click the Test button.
   1. You should see the message `Hello Heartin` after expanding the details of execution result.

* We can also check the logs printed using `context.getLogger().log()`:
   1.Under the Log output section, you can see the log you printed.
   1.You can also see the log in the CloudWatch service. There should be a Click here link to view the CloudWatch log group. Click on the link, wait or refresh for a stream that matches your invocation time, and click on the stream link to see the log statement within CloudWatch.