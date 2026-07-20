package com.fullstack.adminservice.controller;

import com.fullstack.adminservice.common.AxonExceptions;
import com.fullstack.adminservice.query.client.UserQueryClient;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.commonservice.user.command.DeleteUserCommand;
import com.fullstack.commonservice.user.result.UserResult;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {
    private final UserQueryClient userQueryClient;
    private final CommandGateway commandGateway;

    public AdminCustomerController(UserQueryClient userQueryClient, CommandGateway commandGateway) {
        this.userQueryClient = userQueryClient;
        this.commandGateway = commandGateway;
    }

    @GetMapping
    public ResponseData<List<UserResult>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            String normalizedSearch = search == null ? "" : search.trim().toLowerCase();
            String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
            List<UserResult> customers = userQueryClient.findAll().get().stream()
                    .filter(user -> "USER".equals(user.getRole()))
                    .filter(user -> normalizedStatus.isBlank() || normalizedStatus.equals(user.getStatus()))
                    .filter(user -> normalizedSearch.isBlank() || matchesSearch(user, normalizedSearch))
                    .toList();
            return new ResponseData<>("SUCCESS", "Lay danh sach khach hang thanh cong", customers);
        } catch (ExecutionException e) {
            throw AxonExceptions.unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseData<Long> delete(@PathVariable Long userId) {
        if (userId == 1L) {
            throw new IllegalArgumentException("Khong the xoa tai khoan quan tri vien");
        }
        try {
            commandGateway.send(new DeleteUserCommand(userId)).get();
            return new ResponseData<>("SUCCESS", "Xoa tai khoan thanh cong", userId);
        } catch (ExecutionException e) {
            throw AxonExceptions.unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private boolean matchesSearch(UserResult user, String search) {
        return contains(user.getEmail(), search)
                || contains(user.getUsername(), search)
                || contains(user.getFullName(), search)
                || contains(user.getPhone(), search);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }
}
