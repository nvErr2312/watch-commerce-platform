package com.fullstack.adminservice.query.client;

import com.fullstack.commonservice.user.query.FindAllUsersQuery;
import com.fullstack.commonservice.user.result.UserListResult;
import com.fullstack.commonservice.user.result.UserResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

@Component
public class UserQueryClient {
    private final QueryGateway queryGateway;

    public UserQueryClient(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    public CompletableFuture<List<UserResult>> findAll() {
        return queryGateway.query(new FindAllUsersQuery(), ResponseTypes.instanceOf(UserListResult.class))
                .thenApply(UserListResult::getUsers);
    }
}
