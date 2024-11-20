# User sign-up flow with Cognito
In the previous recipes, we created our users as admins. However, many real-world applications, including most web applications, allow users to sign-up by themselves. During the sign-up, a user might have to verify their email or phone number. An admin might also confirm a user sign-up.

In this recipe, we will demonstrate the user sign-up flow with self-confirmation, as well as with admin confirmation, using CLI commands. You can follow the same steps while working with any SDK of your choice. You can refer to the documentation of the particular SDK for the exact usage. 

## Getting ready
The following are the prerequisites for completing this recipe:
* Ensure that you have created a Cognito user pool, following the recipe [`Creating a Cognito user pool`](../creating-a-cognito-user-pool/README.md). 
* Ensure that you have created a Cognito app client with a `USER_PASSWORD_AUTH` explicit flow declaration, following the recipe [`Client-side authentication`](../cognito-client-side-authentication-flow/README.md). 


## How to do it...
I will discuss two sign-up flows: one that requires the user to provide an email address and confirm sign-up based on a code received in their email, and one in which the admin will confirm the user. In real-world applications, these two are often combined.

### User sign-up with self-confirmation
The following steps describe how to set up user sign-up with self-confirmation:
1. Use the `sign-up` sub-command to initiate the sign-up flow, providing your `username` and `password`:
    ```bash
    aws cognito-idp sign-up \
        --client-id 4s69op0v8es2cojl5ncjql2v4g \
        --username testuser4 \
        --password Passw0rd$ \
        --user-attributes Name=email,Value=testemail@heartin.tech
    ```    
    Replace `testemail@heartin.tech` with your email address. 

    If this is successful, you should get the following response:
    ```json
    {
        "UserConfirmed": false,
        "CodeDeliveryDetails": {
            "Destination": "+********4114",
            "DeliveryMedium": "SMS",
            "AttributeName": "phone_number"
        },
        "UserSub": "f8f7f918-23dc-43da-a88b-4a7364c78072"
    }
    ```
    You will also receive an email with the confirmation code, as shown in the following screenshot:

    > The preceding message format was configured while creating the user pool. You can also use the email configuration section in the input CLI JSON, or within the CloudFormation template, to make additional customizations, such as a reply email address.
2. Confirm the user sign-up with the code received, as follows:
    ```bash
    aws cognito-idp confirm-sign-up \
    --client-id 4s69op0v8es2cojl5ncjql2v4g \
    --username testuser4 \
    --confirmation-code 156202
    ```
    If it is successful, you should see no errors in the response.
3. Now, try to sign-in with your username and password, as follows:
    ```bash
    aws cognito-idp initiate-auth \
        --client-id 4s69op0v8es2cojl5ncjql2v4g \
        --auth-flow USER_PASSWORD_AUTH \
        --auth-parameters USERNAME=testuser4,PASSWORD=Passw0rd$
    ```
    If it is successful, you will get a response with the `AccessToken`, `RefreshToken`, and `IdToken`:
    ```json
    {
    "ChallengeParameters": {},
    "AuthenticationResult": {
        "AccessToken": "eyJraWQiOiI0VTY1ZHNqWlgyRHZoUDNwVFhnaTVkNU4zNkhmUkd00FFVZHFvd1pmTXVnPSIsImFsZyIjA2MGMyMGRkLWVjZjktMTFl0C040Tg1LWNkMzIwOGIxODA5NSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ2aXRvLWlkcC51cy1lYXN0LTEuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0xX2ZZc2IxR3llYyIsImV4cCI6MTU0MjCONTA5MiwiaWF0IjOiI1aGg0djdudmV1MjJ2ZWE3NGg4c3R00TIzOCIsInVzZXJuYW1lIjoidGVzdHVzZXIifQ.Eg3Z1kAJyJ-NEXmWmbavMV325_Uh-UTecuFeXtYLKHjc_rD7gj8vp50NfDQN5m_u8fP8Q8JRFTsLPaGR6C3qX6u0F_HR6BN_YWpHYtBudsShz2qGlryxcvqSzfpzbws8rMli5xZNIxmwna0c0CycbED8buKQ49Mj_g",
        "ExpiresIn": 3600,
        "TokenType": "Bearer",
        "RefreshToken": "eyJjdHki0iJKV1QiLCJlbmMi0iJBMjU2RONNIiwiYWxnIjoiUlSBLU9BRVAifQ.BnFlQ7rap5v7g4aapMvMJPUejJIwTkqnPpAYjRNJsECnIlKgVYa8gmfkVLmPaGT8p2NmuIicHDZoPhY60VwAHrtVBWzq9xXX4g-k4PKIerEaMK4vZGMcLlMHcUNex7usnE0xvu0ryXrSniWAi3Sq940xjfEFgpfM2g.4WtMGmzXd8KEzU1P.g7zcSVH6RXguLWAeTa0ALcJghunwYB7Z5gSAEfbdCTXsWaAfddTlNyGweh0e6S34q4t4egQtgTZWjcUdBuCRkvcCUU_V3YC38SxENfNmxw9AzVfRg7PQKM4M5Pt2vU-CZx8Hklat31fojErd-3YBOLzgYIq8_0qMNhVWoeJCA3AjsB0vQ2R7z_qyaTXqbQBBpR0QfjaQD0psT404xRJ_Blqxs_PEm2Ego7mXsjo6SoILgVRX5q gZ0KjqXci91M-65MJB7HbLOUXYouOYPLazE_J3P0npEEAUUc9hx2RCHbfh8EUyPFiHv890mNukhcuyfNlh5N8EPLyy5Gmxf8MGgfe0jqPtnA4J5f380vD5mXF2Dx_iF1-1MNdcZnQzUG-1Z0yG9rTchnrPIk6JLMCXhUQFu9791plCSKRf1oLiZTSg0e0PB_h2lGTJaU2ULEklYj6qpxKaryd-ysY7C1YDPf-ee_w0-MN5maUjwXuzpKrboiwEBsjfrGSnwd4M58GHHQtqUZMUbVQn6hoVElyYNvhgXdXByuVTxGKmHdmBu28hbuhYt7Y1h409AqhBWAhqUFez2BqBeGYT_tsv3FELK1-s7qPrNvkwLQYPaXDooLgKNVMqjnVbpIsbLU4DW4nAHLWNx9d165saUwcaMUuw.Wa_lkFBRerl1zeoKjE32XA",
        "IdToken": "eyJraWQi0iJlVWh6bWYzR28wNDcrVW01b3dybDdReHZuamdvYjFlbk9ZV3NnV1FvZEc0PSIsImFsZyI6I1dmV1MjJ2ZWE3NGg4c3R00TIzOCIsImV2ZW50X2lkIjoiMDYwYzIwZGQtZWNm0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA4YjE4MDk1IiwidG9rZWYXN0S0xMWU4LTg50DUtY2QzMjA"
        }
    }
    ```
4. Use the access token to delete the user, as follows:
    ```bash
    aws cognito-idp delete-user \
        --access-token <access token>
    ```    
    Replace the `AccessToken` with the access token received in the previous command response. If it is successful, you should see no response.
5. Verify that the user was actually deleted by trying to initiate the authentication flow again. This time, you should get an error that the user does not exist.

### User sign-up with admin confirmation
User sign-up with admin confirmation is similar to self-confirmation, as indicated by the following steps:
1. Use the `sign-up` sub-command to initiate the `sign-up` flow, as follows:
    ```bash
    aws cognito-idp sign-up \
        --client-id 4s69op0v8es2cojl5ncjql2v4g \
        --username testuser4 \
        --password Passw0rd$
    ```
    The email is optional here, since the user will be confirmed by an admin. However, you may specify an email and it will send the verification code, as we had specified the email as an `AutoVerifiedAttributes` while creating the user pool.
    
    If this is successful, you should get the following response:
    ```json
    {
        "UserConfirmed": false,
        "UserSub": "e9ff2a2a-f7d9-44d3-b5a3-24ef7ee5288a"
    }
    ```
2. Confirm the user as an admin, as follows:
    ```bash
    aws cognito-idp admin-confirm-sign-up \
        --user-pool-id us-east-1_fYsb1Gyec \
        --username testuser4 \
        --profile admin
    ```    
    If this is successful, you should not see a response.
3. Now, try to sign-in with your username and password, as follows:
    ```bash
    aws cognito-idp initiate-auth \
        --client-id 4s69op0v8es2cojl5ncjql2v4g \
        --auth-flow USER_PASSWORD_AUTH \
        --auth-parameters USERNAME=testuser4,PASSWORD=Passw0rd$
    ```
    If it is successful, you will get a response with the `AccessToken`, `RefreshToken`, and `IdToken`, similar to the one in the self-confirmation flow.

4. You can delete the user by using the `delete-user` sub-command (refer to the self-confirmation flow).

## How it works...
We have discussed two flows for user sign-up. In the first flow, the user provided an email while signing up, and an email was sent with a passcode. The user then used this passcode to confirm the sign-up process. In the second flow, the user created an account without providing an email, and then an admin confirmed the user. In the second flow, the user can still provide email; in such cases, the user will get the passcode, and an admin can still confirm that user. Most real-world projects support both of these options in a single flow. 

## There's more...
We have discussed passing an email as a user attribute. You can also pass any of the other built-in user attributes, such as name, given_name, family_name, middle_name, nick_name, preferred_username, profile, picture, website, email, email_verified, gender, birthdate, zoneinfo, locale, phone_number, phone_number_verified, address, and updated_at. You can also define a custom attribute.

We only discussed email verification in this recipe. You can add phone verification by adding it to the `AutoVerifiedAttributes` list. We also did user `sign-up` with a text username. We could have also used an `email` or `phone_number` as usernames. In a later recipe, we will create a user pool to support SMS and MFA verification, and to support user sign-up with a phone_number as a username.

## See also
https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-email-phone-verification.html