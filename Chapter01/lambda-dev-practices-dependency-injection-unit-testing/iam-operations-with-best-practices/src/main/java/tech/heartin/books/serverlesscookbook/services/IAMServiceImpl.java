package tech.heartin.books.serverlesscookbook.services;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;
import lombok.AllArgsConstructor;
import tech.heartin.books.serverlesscookbook.domain.IAMOperationResponse;

/**
 * Implementation of {@link IAMService} using AWS SDK v2.
 */
@AllArgsConstructor
public class IAMServiceImpl implements IAMService {

    private final IamClient iamClient;

    public IAMServiceImpl() {
        this.iamClient = IamClient.builder()
                .build();
    } 

    @Override
    public final IAMOperationResponse createUser(final String userName) {
        try {
            CreateUserRequest request = CreateUserRequest.builder()
                    .userName(userName)
                    .build();

            CreateUserResponse response = iamClient.createUser(request);

            return new IAMOperationResponse(
                    "Created user " + response.user().userName(),
                    null);
        } catch (IamException e) {
            return new IAMOperationResponse(
                    null,
                    "Failed to create user: " + e.getMessage());
        }
    }

    @Override
    public final IAMOperationResponse checkUser(final String userName) {
        try {
            ListUsersRequest request = ListUsersRequest.builder().build();
            ListUsersResponse response;
            boolean found = false;

            do {
                response = iamClient.listUsers(request);

                for (User user : response.users()) {
                    if (user.userName().equals(userName)) {
                        found = true;
                        return new IAMOperationResponse("User " + userName + " exists", null);
                    }
                }

                if (response.isTruncated()) {
                    request = ListUsersRequest.builder()
                            .marker(response.marker())
                            .build();
                }

            } while (response.isTruncated());

            if (!found) {
                return new IAMOperationResponse(null, "User " + userName + " does not exist");
            }
        } catch (IamException e) {
            return new IAMOperationResponse(
                    null,
                    "Failed to check user: " + e.getMessage());
        }
        
        return new IAMOperationResponse(null, "Error checking user");
    }

    @Override
    public final IAMOperationResponse deleteUser(final String userName) {
        try {
            DeleteUserRequest request = DeleteUserRequest.builder()
                    .userName(userName)
                    .build();

            iamClient.deleteUser(request);

            return new IAMOperationResponse(
                    "Deleted user " + userName,
                    null);
        } catch (DeleteConflictException e) {
            return new IAMOperationResponse(
                    null,
                    "Unable to delete user: " + e.getMessage());
        } catch (IamException e) {
            return new IAMOperationResponse(
                    null,
                    "Failed to delete user: " + e.getMessage());
        }
    }
}