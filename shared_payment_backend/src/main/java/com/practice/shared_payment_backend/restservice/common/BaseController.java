package com.practice.shared_payment_backend.restservice.common;

import com.practice.shared_payment_backend.repository.FriendRepository;
import com.practice.shared_payment_backend.repository.GroupRepository;
import com.practice.shared_payment_backend.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class BaseController {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected FriendRepository friendRepository;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected PaymentRepository paymentRepository;
}
