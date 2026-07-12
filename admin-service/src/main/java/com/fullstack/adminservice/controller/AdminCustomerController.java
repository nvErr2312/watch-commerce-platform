package com.fullstack.adminservice.controller;

import com.fullstack.adminservice.common.AxonExceptions;
import com.fullstack.adminservice.query.client.UserQueryClient;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.commonservice.user.result.UserResult;
import com.fullstack.commonservice.user.command.DeleteUserCommand;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {
    private final UserQueryClient userQueryClient;
    private final CommandGateway commandGateway;

    public AdminCustomerController(UserQueryClient userQueryClient, CommandGateway commandGateway) {
        this.userQueryClient = userQueryClient;
        this.commandGateway = commandGateway;
    }

    @GetMapping
    public ResponseData<List<UserResult>> list() {
        try {
            return new ResponseData<>("SUCCESS", "Lấy danh sách khách hàng thành công", userQueryClient.findAll().get());
        } catch (ExecutionException e) {
            throw AxonExceptions.unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseData<String> delete(@PathVariable String userId) {
        if ("1".equals(userId)) {
            throw new IllegalArgumentException("Không thể xóa tài khoản quản trị viên");
        }
        try {
            commandGateway.send(new DeleteUserCommand(Long.parseLong(userId))).get();
            return new ResponseData<>("SUCCESS", "Xóa tài khoản thành công", userId);
        } catch (ExecutionException e) {
            throw AxonExceptions.unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
