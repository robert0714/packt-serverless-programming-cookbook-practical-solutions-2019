package tech.heartin.books.serverlesscookbook.services;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.CreateUserResponse;
import software.amazon.awssdk.services.iam.model.DeleteConflictException;
import software.amazon.awssdk.services.iam.model.DeleteUserRequest;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;
import tech.heartin.books.serverlesscookbook.domain.IAMOperationResponse;

/**
 * Service class for IAM operations using AWS SDK v2.
 */
public class IAMService {

    private final IamClient iamClient;

    public IAMService() {
        this.iamClient = IamClient.builder()
                .build();
    }

    /**
     * Create user.
     * @param userName - user name.
     * @return IAMOperationResponse
     */
    public final IAMOperationResponse createUser(final String userName) {
        CreateUserRequest request = CreateUserRequest.builder()
                .userName(userName)
                .build();

        CreateUserResponse response = iamClient.createUser(request);

        return new IAMOperationResponse(
                "Created user " + response.user().userName(),
                null);
    }

    /**
     * Check user.
     * @param userName - user name.
     * @return IAMOperationResponse
     */
    public final IAMOperationResponse checkUser(final String userName) {
        String marker = null;
        boolean done = false;

        while (!done) {
            ListUsersRequest.Builder requestBuilder = ListUsersRequest.builder();
            if (marker != null) {
                requestBuilder.marker(marker);
            }

            ListUsersResponse response = iamClient.listUsers(requestBuilder.build());

            for (User user : response.users()) {
                if (user.userName().equals(userName)) {
                    return new IAMOperationResponse("User " + userName + " exist", null);
                }
            }

            if (!response.isTruncated()) {
                done = true;
            } else {
                marker = response.marker();
            }
        }
        return new IAMOperationResponse(null, "User " + userName + " does not exist");
    }

    /**
     * Delete user.
     * @param userName - user name.
     * @return IAMOperationResponse
     */
    public final IAMOperationResponse deleteUser(final String userName) {
        DeleteUserRequest request = DeleteUserRequest.builder()
                .userName(userName)
                .build();

        try {
            iamClient.deleteUser(request);
        } catch (DeleteConflictException e) {
            return new IAMOperationResponse(null,
                    "Unable to delete user");
        }

        return new IAMOperationResponse(
                "Deleted user " + userName,
                null);
    }
}
