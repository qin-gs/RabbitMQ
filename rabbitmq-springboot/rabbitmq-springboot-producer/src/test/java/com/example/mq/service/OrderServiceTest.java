package com.example.mq.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService service;

    @Test
    void makeOrderFanout() {
        service.makeOrderFanout("1", "2", 3);
    }

    @Test
    void makeOrderDirect() {
        service.makeOrderDirect("1", "2", 3);

    }
}