package com.fullstack.inventoryservice.query.controller;

import java.util.concurrent.ExecutionException;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.commonservice.bmad.nguoi3.query.inventory.CheckStockAvailabilityQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventoryByProductIdQuery;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.inventoryservice.query.entity.InventoryView;

@RestController
@RequestMapping("/api/inventory")
public class InventoryQueryController {

    private final QueryGateway queryGateway;

    public InventoryQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{productId}")
    public ResponseData<InventoryView> getByProductId(@PathVariable String productId)
            throws ExecutionException, InterruptedException {
        InventoryView view = queryGateway
                .query(new FindInventoryByProductIdQuery(productId), ResponseTypes.instanceOf(InventoryView.class))
                .get();

        return new ResponseData<>("SUCCESS", "Lấy tồn kho thành công", view);
    }

    @GetMapping("/check")
    public ResponseData<Boolean> checkAvailability(
            @RequestParam String productId,
            @RequestParam int quantity) throws ExecutionException, InterruptedException {
        Boolean available = queryGateway
                .query(new CheckStockAvailabilityQuery(productId, quantity), ResponseTypes.instanceOf(Boolean.class))
                .get();

        return new ResponseData<>("SUCCESS", "Kiểm tra tồn kho thành công", available);
    }
}
