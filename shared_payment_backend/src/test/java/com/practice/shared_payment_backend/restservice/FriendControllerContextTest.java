package com.practice.shared_payment_backend.restservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FriendControllerTest {

    @Autowired
    private FriendController controller;

    @Test
    public void contextLoadsTest() {
        assertNotNull(controller);
    }

}