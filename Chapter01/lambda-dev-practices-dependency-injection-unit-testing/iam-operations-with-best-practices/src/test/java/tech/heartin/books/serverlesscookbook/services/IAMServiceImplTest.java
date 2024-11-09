package tech.heartin.books.serverlesscookbook.services;

import java.util.Objects;
import java.util.ArrayList;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.CreateUserResponse;
import software.amazon.awssdk.services.iam.model.DeleteUserRequest;
import software.amazon.awssdk.services.iam.model.DeleteUserResponse;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.heartin.books.serverlesscookbook.domain.IAMOperationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IAMServiceImplTest {

    @Mock
    private IamClient iamClient;

    private IAMService service;

    @BeforeEach
    public void setUp() {
        service = new IAMServiceImpl(iamClient);
        Objects.requireNonNull(service);
    }

    @Test
    public void testCreateUser() {
        String testUser = "test_user";
        IAMOperationResponse expectedResponse = new IAMOperationResponse(
                "Created user test_user", null);

        User user = User.builder()
                .userName(testUser)
                .build();

        CreateUserResponse createUserResponse = CreateUserResponse.builder()
                .user(user)
                .build();

        when(iamClient.createUser(any(CreateUserRequest.class)))
                .thenReturn(createUserResponse);
        
        IAMOperationResponse actualResponse = service.createUser(testUser);
        
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testCheckUser() {
        String testUser = "test_user";
        IAMOperationResponse expectedResponse = new IAMOperationResponse(
                "User test_user exist", null);

        ListUsersResponse listUsersResponse = ListUsersResponse.builder()
                .users(User.builder()
                        .userName(testUser)
                        .build())
                .build();

        when(iamClient.listUsers(any(ListUsersRequest.class)))
                .thenReturn(listUsersResponse);
        
        IAMOperationResponse actualResponse = service.checkUser(testUser);
        
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testDeleteUser() {
        String testUser = "test_user";
        IAMOperationResponse expectedResponse = new IAMOperationResponse(
                "Deleted user test_user", null);

        DeleteUserResponse deleteUserResponse = DeleteUserResponse.builder().build();

        when(iamClient.deleteUser(any(DeleteUserRequest.class)))
                .thenReturn(deleteUserResponse);
        
        IAMOperationResponse actualResponse = service.deleteUser(testUser);
        
        assertEquals(expectedResponse, actualResponse);
    }
}