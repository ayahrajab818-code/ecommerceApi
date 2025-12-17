package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yearup.models.Order;

@RestController
@RequestMapping(value = "/orders", produces = "application/json")
public class OrdersController {
    Order order = new Order();// build it with real values
    @PostMapping
    public ResponseEntity<Order> createOrder() {
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

}

