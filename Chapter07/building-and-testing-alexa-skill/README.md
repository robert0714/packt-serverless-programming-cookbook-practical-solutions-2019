# Building and testing an Alexa skill (Java for Lambda, CLI for Alexa skill)
**Conversational user interfaces (CUI)** are platforms that support conversations similar to real human conversations. They help humans to talk to a system using their natural language rather than using a new programming language or a **domain-specific language (DSL)**. Smart appliances, such as TVs, smart speakers, and smart chatbots, that can understand and interpret natural language are examples of conversational interfaces.

Amazon Alexa is a virtual assistant that lives on the cloud. Alexa can listen to our requests and provide voice responses or take actions, such as switching on a smart bulb. Amazon ships Alexa along with its Echo devices, but Alexa can be used with other devices as well, making use of its APIs. You can extend the default Alexa functionality by creating your own Alexa skills. 

In this recipe, we will create a simple Alexa skill that will introduce a person. This is a very basic skill, but it can still be useful for any events you might be hosting at work or even outside. We can use an Echo device to interact with Alexa through our Alexa skill. To demonstrate the Alexa skill, you can use any Alexa-enabled device, and for learning and testing purposes you can also use a simulator, such as `echosim.io`.

## Getting ready
You should have the following prerequisites to go through this recipe:
* You need an active AWS account. You need to follow Getting started in the recipes [Your first AWS Lambda](../../Chapter01/your-first-aws-lambda/README.md) and [Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) sections from Chapter 1, Getting Started with Serverless Computing on AWS to set up Java, Maven, the parent project, [serverless-cookbook-parent-aws-java](../../serverless-cookbook-parent-aws-java/README.md), and AWS CLI, and may also read other notes there, including code usage guidelines, S3 bucket creation, and notes for Windows users.
* Knowledge of how to install and configure the Alexa skills Kit CLI. 
* An Echo device to talk to Alexa using the skill that we create. However, if you do not have an Echo device, you can use a simulator, such as `echosim.io`.
* A basic understanding of Java and Maven, and the steps for creating and invoking Lambda, as given in Chapter 1, [Getting Started with Serverless Computing on AWS](../../Chapter01/README.md).

### Installing and configuring the ASK CLI
> If you are working on a Windows platform, you need to first install the Node.js Windows build tools before installing ask-cli. 

You can install ask-cli using the following code:
```bash
npm install -g ask-cli
```

#### Configuring ask-cli for the first time
Once `ask-cli` is installed, you can initialize it as follows:
```bash
ask init
```
`ask-cli` will ask you to choose the AWS profile that you want. You can choose the profile that you created as part of [Your first Lambda with AWS CLI recipe](../../Chapter01/your-first-lambda-with-aws-cli/README.md) in Chapter 1, Getting Started with Serverless Computing on AWS. 

Once you select the profile, it will open a browser window where you have to sign in with your AWS credentials, and you will be shown a confirmation screen, as shown in the following screenshot:


Once you click Allow, you will be redirected to a success page, as shown in the following screeenshot:


You can close the window and return to the Terminal, as shown in the following screenshot:
```bash
```
> If you want to complete the initialization without opening a browser, you can use the `ask init --no-browser` command. 

## How to do it...
In this recipe, we will create a simple Alexa skill. When you ask Alexa to say introduction for a person, it will read out an introduction. You can use this simple skill in your company to introduce a guest or to introduce yourself in a talk or presentation. You can make changes to the recipe's code files and deploy it into your AWS account. 

Alexa skill building has two parts: a Lambda backend that does the actual processing (returning the introduction text in our case) and the Alexa skill in the developer portal that interpret user requests, talks to the backend and returns the response.

### Step 1 - Creating the Lambda project (Java)
The ASK SDK requires intent handlers for each of the expected intents. We will create intent handlers for our application-specific intent (for example, a self-intro intent), the launch intent—as well as help, stop, and cancel intents for the inbuilt intents—a fallback intent, and a session end request intent. We will then create a parent lambda handler class that registers all these intent handlers.

> I will be discussing only the core application logic and will not be discussing supporting code, such as imports, error handling, and Java doc comments in the book. However, the complete working code is provided along with the code files. 

### Step 2 - Provisioning Lambda (AWS CLI)
Go through the following steps to deploy and invoke the lambda. Refer to previous recipes or code files if you need more details on any of the steps. You can also follow [Your first Lambda with AWS CLI](../../Chapter01/your-first-lambda-with-aws-cli/README.md) recipe in Chapter 1, Getting Started with Serverless Computing on AWS and use CloudFormation for Lambda provisioning:
1. Run `mvn clean package` from inside the Lambda project root folder to create the `Uber JAR`.
2. Upload the `Uber JAR` to S3.
3. Create a role called `lambda-alexa-simple-intro-role` for the lambda, with an appropriate trust relationship definition.
4. Create a policy for basic logging permissions and attach it to the role.
5. Create the lambda function as follows:
    ```bash
    aws lambda create-function \
        --function-name lambda-alexa-simple-intro \
        --runtime java8 \
        --role arn:aws:iam::<account id>:role/lambda-alexa-simple-intro-role \
        --handler tech.heartin.books.serverlesscookbook.SelfIntroStreamHandler::handleRequest \
        --code S3Bucket=serverless-cookbook,S3Key=lambda-alexa-simple-intro-0.0.1-SNAPSHOT.jar \
        --timeout 15 \
        --memory-size 512 \
        --region us-east-1 \
        --profile admin
    ```    
6. Give permission for the Alexa skill to invoke this Lambda as follows:
    ```bash
    aws lambda add-permission \
    --function-name lambda-alexa-simple-intro \
    --statement-id "12345" \
    --action "lambda:InvokeFunction" \
    --principal "alexa-appkit.amazon.com" \
    --region us-east-1 \
    --profile admin
    ``` 
    We have given permission to any Alexa skill to invoke this lambda. Once you create a skill, you can remove this lambda and add a permission that allows only a particular skill to invoke this lambda, as shown in the following code:
    ```bash
    aws lambda remove-permission \
        --function-name lambda-alexa-simple-intro \
        --statement-id "12345" \
        --region us-east-1 \
        --profile admin
    
    aws lambda add-permission \
        --function-name lambda-alexa-simple-intro \
        --statement-id "12345" \
        --action "lambda:InvokeFunction" \
        --principal "alexa-appkit.amazon.com" \
        --event-source-token <skill id from lambda> \
        --region us-east-1 \
        --profile admin
    ```

### Step 3 - Building an Alexa skill with ASK CLI
We can build an Alexa skill by going through the following steps:
1. Prepare the skill manifest JSON file, as shown in the following code:
    ```json
    {
        "manifest": {
            "publishingInformation": {
                "locales": {
                    "en-US": {
                        "summary": "A simple skill to say introduction for someone",
                        "examplePhrases": [
                            "Alexa please say intro",
                            "say intro",
                            "help"
                        ],
                        "name": "self-intro",
                        "description": "Simple Intro Skill"
                    }
                },
                "isAvailableWorldwide": true,
                "testingInstructions": "1) Say 'Alexa, say intro'.",
                "category": "ORGANIZERS_AND_ASSISTANTS",
                "distributionCountries": []
            },
            "apis": {
                "custom": {
                    "endpoint": {
                        "uri": "arn:aws:lambda:us-east-1:<account id>:function:lambda-alexa-simple-intro"
                    }
                }
            },
            "manifestVersion": "1.0"
        }
    }
    ```
    Save this file as `skill.json`.

    I have shown only one locale section for US: `en-US`. You can add additional locales. For example, you can add `en-IN` if you are planning to test this with an echo with **English (IN)** set. I have added two locales in the code files to demonstrate this. 

2. Create a skill using this JSON file, as shown in the following code:
    ```bash
    ask api create-skill --file skill.json
    ```
    If the skill manifest JSON file was created successfully, you should get the following response:
    ```bash
    ```

    You can execute the command that is provided to track the skill as follows:

    ```bash
    ```
    If the skill creation was successful, you should get the following message:

    ```json
    ```
3. Prepare the model file as follows:
    ```json
    {
        "interactionModel": {
            "languageModel": {
                "invocationName": "self-intro",
                "intents": [
                    {
                        "name": "AMAZON.CancelIntent",
                        "samples": []
                    },
                    {
                        "name": "AMAZON.HelpIntent",
                        "samples": []
                    },
                    {
                        "name": "AMAZON.StopIntent",
                        "samples": []
                    },
                    {
                        "name": "SelfIntroIntent",
                        "samples": [
                            "please say intro",
                            "say intro",
                            "please say intro for",
                            "say intro for",
                            "intro",
                            "intro for"
                        ]
                    }
                ]
            }
        }
    }
    ```
    Save this file as `en-US.json`. If you need to support more locales, you can create model files for them as well.

    For more details on invocation names, intents, and utterances, refer to the How it works section.

4. Update the skill with the preceding model file, as follows:
    ```bash
    ask api update-model \
        --skill-id amzn1.ask.skill.ab1fdfac-42eb-42ae-aeae-b761f3c903c1 \
        --file en-US.json \
        --locale en-US
    ```    
    Replace the skill ID with the skill ID you receive in [Step 2: Provisioning Lambda (AWS CLI)](#step-2---provisioning-lambda-aws-cli).

    If the model JSON file was created successfully, you should get the following response:
    ```bash
    ```
    You can execute the command that is provided to track the skill as follows:
    ```bash
    ```
    If the model creation was successful, you should get the following message:
    ```json
    ```
    If you created more locale files in the previous step, you can use the update-model subcommand to update the skills with those models as well.
5. Enable the skill using the following code:
    ```bash
    ask api enable-skill \
        --skill-id amzn1.ask.skill.a585bf93-15bb-4361-ab56-ffbdc66027fd
    ```    
    If successful, you should see the following message:
    ```bash
    ```
    You can now ask Alexa to open our app ('self intro') and then say one of the sample utterances we defined ('please say intro').
    You can use the Alexa simulator available at `https://echosim.io` if you do not have an Echo device. The homepage for this simulator is shown in the following screenshot:

    You can also log in to the AWS developer console and test your skill from the Test tab of your skill, as shown in the following screenshot:

## How it works...
Alexa uses automated speech recognition (ASR) followed by natural language understanding (NLU) to process user requests. Internally, Alexa makes use of Amazon Lex. Amazon Lex is the primary AWS service for building conversational interfaces with voice and text, using ASR and NLU. 

In summary, we did the following in this recipe:
1. Created a Java lambda as the backend for the Alexa skill
2. Created an Alexa skill using the Alexa Skills Kit Command-Line Interface
3. Linked the Alexa skill and Lambda
4. Tested the skill

Now, let's learn some theory to understand these steps better.
### Alexa skill invocation basics
You start a conversation with Alexa using an invocation name, or wake word. The default wake word is **Alexa**. These days, you can also use the wake words **Echo**, **Computer**, and **Amazon**. You can set the wake word from the Amazon Alexa app, which you can download from the Apple App Store or Google Play Store.

Once you activate Alexa, you can invoke a skill published by Amazon, or launch a custom skill published by other developers, using an invocation name. You can launch your own skills from your Echo devices without needing to publish them, as long as the device is linked to the same Amazon account as your developer portal.

Your Alexa skill can define different intents. Intents can be considered as different functionalities provided by your skill, such as welcoming someone to the app, responding to a question, taking an action, and so on. After you launch a skill, you need to say a phrase, and each phrase will be mapped to an intent.

For example, consider the sentence Alexa, open Cloudericks and please say the intro. Alexa is the wake work here, Cloudericks is our invocation name, and please say the intro is the utterance. The utterance will invoke an intent that performs the actual introduction. The intent can be defined within an AWS lambda or an external API.

You usually define more utterances for an intent, such as please say intro, please say the intro, say intro, and so on. You can define more utterances to make your application more flexible and then improve it further from the analytics data for failed utterances. 

Instead of saying open Cloudericks to launch the skill, you can also use the invocation name in your sentence. For example, instead of saying Alexa, open Cloudericks and please say the intro, you can say Alexa, please say intro for Cloudericks, as we did in this recipe.

### Explaining the Lambda project (Java)
We used ASK SDK v2 (Java) in this recipe. With this SDK version, you need to define handlers for each of your intents. Each handler has a `canHandle` function and a `handle` function. The `canHandle` function checks and confirms what intents the handler responds to and the `handle` function contains the actual logic for the intent.

Apart from the intent handler classes, you also need to define a parent handler class that extends the `SkillStreamHandler` class. From this handler class, you need to pass an `AlexaSkill` object that contains all the other intent handlers into the `SkillStreamHandler` parent constructor through a super call. 

> If you are using the older version of the SDK, you can go to https://alexa-skills-kit-sdk-for-java.readthedocs.io/en/latest/Migrating-To-ASK-SDK-v2-For-Java.html to migrate to the version (v2) that we use in this recipe. 
In the introduction, I wrote `CloudMaterials.com` and `javajee.com` differently so that Alexa says them in the way that I want. You can use `speech synthesis markup language (SSML)` for better control over how Alexa generates speech. 

We first gave permission for any Alexa skill to invoke our lambda. This is because you can create a skill with a lambda endpoint only if the lambda has an Alexa trigger. Once you create a skill, you can remove this permission and add a permission that only allows this skill to invoke this lambda. The alternative is to create the skill without an endpoint, as shown in the following code, located in the `skill.json` file:
```json
"apis": {
    "custom": {
    }
},
```
You can then create the lambda and add a trigger with this skill's ID. After that, you can update the `skill.json` with the endpoint and use the `update-skill` subcommand to update the skill, as shown in the following code:
```bash
ask api update-skill \
    --skill-id amzn1.ask.skill.6fed53f3-661e-4f26-8de8-4ee4844f899b \
    --file skill-update.json
```    

### Explaining the ASK CLI steps
We used ASK CLI to create our Alexa skill. We can also use the developer portal UI or a voice app platform (more information on this can be found in the There's more section).

ASK CLI provides a set of high-level commands, as well as a set of low-level commands. High-level commands include commands such as `ask new`, `ask deploy`, and so on that make the creation and deployment of skills very easy. The `ask new` command creates a sample skill, sample Lambda, and all the required files from a template that you can modify. The `ask deploy` command allows you to deploy the skill to the developer portal easily.

Low-level commands, on the other hand, provide more flexibility as to what you want to do with your skill. They allow you to work on only the skill or lambda. The low-level command steps also correspond to the steps we perform from the developer portal UI. In this recipe, we use the low-level set of commands to create and deploy the Alexa skill. We create and deploy the lambda in the same way that we have been doing in the previous recipes in this book. 

To create a skill with ASK CLI, we created and used the following two files:
* `skill.json`: This is the skill manifest JSON file. It is the JSON representation of the skill and contains the required metadata for your skill. 
* `en-US.json`: This is the locale-specific model file for the US locale. This file defines the invocation name, intents, and the sample utterances. We created the model file only for the US locale. You could, however, create model files for other supported locales as well. I have included more than one locale with code files to refer to.

## There's more...
We created a simple Alexa skill, created a Lambda backend, linked both together, and tested it using the Echo device (or a simulator). You can now publish Alexa skills and get rewarded by Amazon through its Alexa promotional programs.

Amazon Lex is not just restricted to conversational interfaces that use voice inputs; it can also be used to build custom non-voice conversational interfaces, such as chatbots. Since the backends for both an Alexa skill and Lex-based chatbots can both be lambdas, you can reuse the same backend logic for both Alexa and a chatbot.

We created Alexa skills using ASK CLI. However, you can also use the developer portal to create and test Alexa skills easily. You can also explore the voice app platforms that make Alexa skill development even more easy.

### Voice app platforms
There are various voice app platforms that let you create Alexa skills with much less code, or even no code at all. Most of these support drag-and-drop functionalities. These tools also let you build your skills once and then deploy them in multiple places. Examples of such tools include VoiceFlow, Bluetag, Conversation.one, Jovo, Witlingo, although there are many other.

## See also
Detailed steps for installing and configuring ASK CLI are available at the following links:
* https://developer.amazon.com/docs/smapi/quick-start-alexa-skills-kit-command-line-interface.html
* https://developer.amazon.com/docs/smapi/skill-manifest.html