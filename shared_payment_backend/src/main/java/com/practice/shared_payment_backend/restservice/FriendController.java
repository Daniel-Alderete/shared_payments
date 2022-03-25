package com.practice.shared_payment_backend.restservice;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.interfaces.Group;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.models.responses.FriendResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class FriendController extends BaseController {

    @GetMapping("/api/{version}/groups/{groupId}/friends")
    public List<FriendResponse> friends(@PathVariable String version, @PathVariable String groupId) {
        logger.trace("Starting with version {} and groupId {}", version, groupId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            List<FriendResponse> response = new ArrayList<>();

            friendRepository.findAllById(group.get().getFriends()).forEach(friend -> {
                Set<Payment> payments = new HashSet<>();
                paymentRepository.findAllById(friend.getPayments()).forEach(payments::add);
                logger.debug("Friend {} was found with payments {}", friend, payments);
                response.add(new FriendResponse(friend, new HashSet<>(payments)));
            });

            return response;
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new EmptyResultDataAccessException(1);
        }
    }
}
