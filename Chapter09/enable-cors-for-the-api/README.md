# Enabling CORS for the API and testing with CodePen
CORS is a mechanism that allows a web application running at one domain (origin) to access selected resources from a different server. Without CORS, the server will respond with a status code of 403 in such cases. We will create an API gateway service similar to what we did in Chapter 2, Building Serverless REST APIs with API Gateway, but will also enable CORS on the API.

We will first get familiar with the CodePen website and will test our API by invoking it using JavaScript SDK code from within the CodePen website. This will also be a revision of the API Gateway concepts that we learned in Chapter 2, Building Serverless REST APIs with API Gateway, but with the additional support for CORS. We will only discuss new concepts here; for theory on already discussed topics, you may refer to the recipes of Chapter 2, Building Serverless REST APIs with API Gateway.

## Getting ready
The following are the prerequisites for this recipe:
* A working AWS account
* A configured AWS CLI, as discussed in the Your first Lambda with the AWS CLI recipe of Chapter 1, Getting Started with Serverless Computing on AWS
* You have followed the steps in the recipe Your first API using the AWS CLI from Chapter 2, Building Serverless REST APIs with API Gateway, and created and deployed an API with a GET URL of the form `https://<rest-api-id>.execute-api.us-east-1.amazonaws.com/dev/greeting/Heartin`
* You are familiar with CodePen or any browser-based tool from which we can send JavaScript requests to our API

### Getting familiar with CodePen
Follow these steps to use CodePen to connect to our API through GET:
1. Go to `https://codepen.io`. This will take us to the CodePen website.
2. Click tab Create and then select the Pen option.
3. Click the Settings menu, select the Behavior tab, and uncheck the Enabled option under Auto-Updating Preview. This will enable us to Run our code.

Enter the following code in the JS section of the CodePen UI and click Run:
```JavaScript
var xhr = new XMLHttpRequest();
xhr.open('GET', 'https://8vqyyjelad.execute-api.us-east-1.amazonaws.com/dev/greeting/Heartin');
xhr.onreadystatechange = function (event) {
    console.log(event.target.response);
}
xhr.setRequestHeader('Content-Type', 'application/json');
xhr.send();
```
We should receive a blank response in the CodePen UI. But if we open the developer tools for our browser (for example, Chrome Developer tools), we should see the actual error message as follows:


This is because CORS is not enabled. We will create an API with CORS enabled and test it again in the How to do it... section. We will also see how we can enable CORS on the current API. 


## How to do it...
Let's create an API with CORS enabled from scratch. Steps 1 to 4 are the same as we have seen in Chapter 2, Building Serverless REST APIs with API Gateway:
1. Create an API:
    ```bash
    aws apigateway create-rest-api \
        --name 'API WITH CORS' \
        --region us-east-1 \
        --profile admin
    ```    
    > I will not display the region and profile parameters for further commands. You may either add them manually to every command or configure them as the defaults with the AWS CLI configuration.
2. Get the ID of the root resource path `\`:
    ```bash
    aws apigateway get-resources \
        --rest-api-id xenqybowjg
    ```    
3. Create a resource `greeting` under the root path:
    ```bash
    aws apigateway create-resource  \
        --rest-api-id xenqybowjg \
        --parent-id p8yd8xde55 \
        --path-part greeting
    ```    
4. Create a subresource with a path parameter that can accept a string:
    ```bash
    aws apigateway create-resource \
        --rest-api-id xenqybowjg \
        --parent-id xkjhh7 \
        --path-part "{name}"
    ``` 
5. Next, we will create the `GET` method:
    1. Execute `put-method` for the `GET` method:
        ```bash    
        aws apigateway put-method  \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method GET \
            --authorization-type "NONE"
        ```    
    2. Execute `put-method-response` for the `GET` method:
        ```bash
        aws apigateway put-method-response \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method GET \
            --status-code 200 \
            --response-parameters file://put-method-response-get.json
        ```    
        `put-method-response-get.json` should look as follows:
        ```json
        {
        "method.response.header.Access-Control-Allow-Origin": false
        }
        ```
    3. Execute `put-integration` for the `GET` method:
        ```bash    
        aws apigateway put-integration \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method GET  \
            --type MOCK  \
            --integration-http-method GET \
            --request-templates "{\"application/json\": \"{"statusCode": "200"}\"}"
        ```    
    4. Execute `put-integration-response` for the GET method:
        ```bash    
        aws apigateway put-integration-response \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method GET \
            --status-code 200 \
            --response-templates file://response-template-get.json \
            --response-parameters file://put-method-integration-get.json \
            --selection-pattern "" \
            --region us-east-1 --profile admin
        ```    
        The `response-template-get.json` file should have the following contents:
        ```json
        {"application/json": "Hello $input.params('name')"}
        ```
        The `put-method-integration-get.json` file should have the following contents:
        ```json
        {
        "method.response.header.Access-Control-Allow-Origin": "'*'"
        }
        ```

6. Now, we will create the `OPTIONS` method:
    1. Execute `put-method` for the `OPTIONS` method:
        ```bash
        aws apigateway put-method  \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method OPTIONS \
            --authorization-type "NONE" 
        ````
    2. Execute `put-method-response` or the `OPTIONS` method:
        ```bash  
        aws apigateway put-method-response \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method OPTIONS \
            --status-code 200 \
            --response-parameters file://put-method-options.json
        ```    
        `put-method-options.json` should look like this:
        ```json
        {
        "method.response.header.Access-Control-Allow-Origin": false,
        "method.response.header.Access-Control-Allow-Headers": false,
            "method.response.header.Access-Control-Allow-Methods": false
        }
        ``` 
    3. Execute `put-integration` for the `OPTIONS` method:
        ```bash    
        aws apigateway put-integration \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method OPTIONS \
            --type MOCK  \
            --integration-http-method OPTIONS \
            --request-templates "{\"application/json\": \"{"statusCode": "200"}\"}"
        ```    
    4. Execute `put-integration-response` for the `OPTIONS` method:
        ```bash    
        aws apigateway put-integration-response \
            --rest-api-id xenqybowjg \
            --resource-id sfgfk6 \
            --http-method OPTIONS \
            --status-code 200 \
            --response-parameters file://put-method-integration-response-options.json \
            --selection-pattern ""
        ```    
        The `put-method-integration-response-options.json` file should contain the following content:
        ```json
        {
        "method.response.header.Access-Control-Allow-Origin": "'*'",
        "method.response.header.Access-Control-Allow-Headers": "'Content-Type,Authorization,X-Amz-Date,X-Api-Key,X-Amz-Security-Token'"
        }
        ```
7. Deploy the API:
    ```bash
    aws apigateway create-deployment \
        --rest-api-id xenqybowjg \
        --stage-name dev  \
        --stage-description "Dev stage" \
        --description "Dev deployment"
    ```    
    Execute the following URL from the browser: https://xenqybowjg.execute-api.us-east-1.amazonaws.com/dev/greeting/Heartin. 
8. Execute the URL from CodePen as follows:
    ```JavaScript
    var xhr = new XMLHttpRequest();
    xhr.open('GET', 'https://qngs4lsxob.execute-api.us-east-1.amazonaws.com/dev/greeting/Heartin');
    xhr.onreadystatechange = function (event) {
    console.log(event.target.response);
    }
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send();
    ```
    In the Chrome developer console, we should now see a success message as follows, instead of the errors we saw in the Getting ready section.

    > Components within the CloudFormation template correspond to what we have seen with AWS CLI commands and are mostly self-explanatory. The corresponding CloudFormation template is provided with the code files for reference.

## How it works...
We created an API from scratch with CORS enabled. We did it using both AWS CLI commands and the CloudFormation template. Let's first understand the CLI steps in detail.

The first four steps with the CLI are similar to what we have seen in recipes in Chapter 2, Building Serverless REST APIs with API Gateway:
1. Create an API
2. Get the ID of the root resource
3. Create a resource `greeting` 
4. Create a subresource with a path parameter

The next four steps create the GET method: 
5. `put-method` for the `GET` method
6. `put-method-response` for the `GET` method
7. `put-integration for the` `GET` method
8. `put-integration-response` for the `GET` method

The `put-method` subcommand and `put-integration` subcommand are similar to what we have seen in the recipes of Chapter 2, Building Serverless REST APIs with API Gateway. The `put-method-response` and `put-integration-response` subcommands now also should specify the `response-parameters` property. 

The `response-parameters` property of the `put-method-response` subcommand contains a key-value map specifying required or optional response parameters that the API can send back in the response. The key of this map is a method response header name and the value is a Boolean flag indicating whether the method response parameter is required or not (true for required and false for optional).

The `response-parameters` property of the `put-method-integration` subcommand contains a key-value map that specifies the response parameters that are passed to the method response from the backend (mock integration in our case). The key is a method response header parameter name and the value is an integration response header value, a static string value enclosed within single quotes, or a JSON expression from the integration response body.

As we can see from the previous section, we need to use four subcommands to configure an HTTP method with API Gateway when using the AWS CLI. However, with a CloudFormation template, we needed only one resource of type AWS::ApiGateway::Method:  

The next four steps create an `OPTIONS` HTTP method for the resource:

9. `put-method` for the `OPTIONS` method
10. `put-method-response` for the `OPTIONS` method
11. `put-integration` for the `OPTIONS` method
12. `put-integration-response` for the `OPTIONS` method

`OPTIONS` is required for the preflight requests. 

For Ajax and HTTP request methods, especially for ones that can modify data, such as `non-GET` methods, or for `POST` method with certain MIME types, the specification mandates browsers to preflight a request to server, asking for supported methods with an HTTP OPTIONS request. The server responds back with a header `Access-Control-Allow-Methods` that lists all support methods other than GET (for example, DELETE).  The browser will then send the actual request only for the supported HTTP request methods.

The OPTIONS response should also contain the headers `Access-Control-Allow-Origin` and `Access-Control-Allow-Headers`. The `Access-Control-Allow-Origin` header specifies the servers (origins) that can access a particular resource. A value of `*` in our case indicates that any other domain name can access it with CORS. In practice, you may make it more specific to particular domains. The `Access-Control-Allow-Headers` header specifies the headers that are allowed in the actual request. We just specified the basis headers `Content-Type`, `Authorization`, `X-Amz-Date`, `X-Api-Key`, and `X-Amz-Security-Token`.

Even if we only use a `GET URI`, we still need the OPTIONS method configured as we are making an AJAX call with the XMLHttpRequest object. With the XMLHttpRequest object, we can exchange data with a web server without reloading the whole page. All modern web browsers have a built-in support for the `XMLHttpRequest` object. In our case, the `Access-Control-Allow-Methods` header may be empty or not specified with the `put-method-integration` subcommand, since we are not supporting any other methods than `GET` (it still has to be defined with the `put-method-response` subcommand, but we can specify it as optional). 

Finally, we deploy the API and then test it using CodePen:

13. Deploy the API
14. Execute the URL from CodePen    

## There's more...
In this recipe, we did a `GET` request from another domain through CORS. We can also try out other HTTP methods. One of the important changes we need to make is to specify the HTTP methods that are allowed using the `Access-Control-Allow-Methods` header in the CORS response.

## See also
* https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request