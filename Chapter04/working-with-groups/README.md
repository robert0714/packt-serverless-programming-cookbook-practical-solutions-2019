# Working with groups
Cognito users can be added to different groups, and we can treat users differently based on those groups. For example, as admin user can be shown an admin menu, whereas a regular user can be shown a regular menu. In this recipe, we will look at how to create and use groups with Cognito user pools.
## Getting ready
The following are the prerequisites for completing this recipe:
* Ensure that you have created a Cognito user pool, following the recipe [Creating a Cognito user pool](../creating-a-cognito-user-pool/README.md). 

## How to do it...
We will look at how to create a group, how to add users to it, and finally, how to check a user for their groups:
1. Create the following users: `admin_user` and `regular_user`.
2. Create a group called administrators, as follows:
    ```bash
    aws cognito-idp create-group \
        --group-name 'administrators' \
        --user-pool-id us-east-1_fYsb1Gyec \
        --description 'Administrator Group' \
        --profile admin
    ```
    If this is successful, it should return the following response:
    ```json
    {
        "id": "dxr47i",
        "name": "First_Cognito_Custom_Authorizer",
        "type": "COGNITO_USER_POOLS",
        "providerARNs": [
            "arn:aws:cognito-idp:us-east-1:㊣㊣㊣㊣㊣㊣㊣:userpool/us-east-1_fYsb1Gyec"
        ],
        "authType": "cognito_user_pools",
        "identitySource": "method.request.header.Authorization"
    }
    ```

3. Create a group called `authenticated_users`, following the command in the previous step.
4. Add the user regular_user to the `authenticated_users`, as follows:
    ```bash
    aws cognito-idp admin-add-user-to-group \
        --user-pool-id us-east-1_fYsb1Gyec \
        --username regular_user \
        --group-name authenticated_users \
        --profile 
    ```
5. Add the user admin_user to the groups `administrators` and `authenticated_users`, following the command in the previous step.
6. Check for the groups that admin_user belongs to, as follows:
    ```bash
    aws cognito-idp admin-list-groups-for-user \
        --username admin_user \
        --user-pool-id us-east-1_fYsb1Gyec \
        --profile admin
    ```
    If this is successful, it should return the details of the two groups to which the user `admin_user` belongs, as follows:
    ```json
    {
        "Groups": [
            {
                "GroupName": "administrators",
                "UserPoolId": "us-east-1_fYsb1Gyec",
                "Description": "Administrator Group",
                "LastModifiedDate": 1544632026.41,
                "CreationDate": 1544632026.41
            },
            {
                "GroupName": "authenticated_users",
                "UserPoolId": "us-east-1_fYsb1Gyec",
                "Description": "Authenticated User Group",
                "LastModifiedDate": 1544632036.115,
                "CreationDate": 1544632036.115
            }
        ]
    }
    ```
7. Check for the groups that `regular_user` belongs to, following the command in the previous step.
If this is successful, it should return the details of the one group to which the user `regular_user` belongs, as follows:
    ```json
    {
        "Groups": [
            {
                "GroupName": "authenticated_users",
                "UserPoolId": "us-east-1_fYsb1Gyec",
                "Description": "Authenticated User Group",
                "LastModifiedDate": 1544632036.115,
                "CreationDate": 1544632036.115
            }
        ]
    }
    ```

## How it works...
This was a small and simple recipe to add a user to a group, and to check the groups to which a user belongs. Once you know the group a user belongs to, you can treat that user in a certain way. I have not included the commands with syntax as they are the same as those of the previous ones; a complete set of commands is available in the code files.

## There's more...
We can also associate an IAM role to a group, and allow the users to access different AWS services based on the role and its associated policies. To attach a role, you can use the `role-arn` property of the `aws cognito-idp admin-list-groups-for-user` command.

## See also
* https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-user-groups.html
* https://aws.amazon.com/blogs/aws/new-amazon-cognito-groups-and-fine-grained-role-based-access-control-2/