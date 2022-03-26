package com.practice.shared_payment_backend.repository.utils;

import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Group;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.repository.FriendRepository;
import com.practice.shared_payment_backend.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FriendUtils {
    protected static final Logger LOGGER = LoggerFactory.getLogger(FriendUtils.class);

    public static Map<Friend, Set<Payment>> getFriendPaymentMap(Group group, FriendRepository friendRepository,
                                                                PaymentRepository paymentRepository) {
        Map<Friend, Set<Payment>> friendPaymentMap = new HashMap<>();

        if (group != null && group.getFriends() != null && !group.getFriends().isEmpty()) {
            friendRepository.findAllById(group.getFriends()).forEach(friend -> {
                Set<Payment> payments = new HashSet<>();

                if (friend.getPayments() != null && !friend.getPayments().isEmpty()) {
                    paymentRepository.findAllById(friend.getPayments()).forEach(payments::add);
                }

                LOGGER.debug("Adding Friend {} and payments {}", friend, payments);
                friendPaymentMap.put(friend, payments);
            });
        }

        return friendPaymentMap;
    }
}
