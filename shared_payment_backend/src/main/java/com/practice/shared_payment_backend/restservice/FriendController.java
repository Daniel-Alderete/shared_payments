package com.practice.shared_payment_backend.restservice;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.friends.FriendMember;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.common.BaseController;
import com.practice.shared_payment_backend.restservice.models.exceptions.BadRequestBodyException;
import com.practice.shared_payment_backend.restservice.models.exceptions.FriendNotFoundException;
import com.practice.shared_payment_backend.restservice.models.exceptions.GroupNotFoundException;
import com.practice.shared_payment_backend.restservice.models.requests.FriendRequest;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import com.practice.shared_payment_backend.restservice.models.responses.friend.FriendListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.friend.FriendResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
public class FriendController extends BaseController {

    @GetMapping("/api/{version}/groups/{groupId}/friends")
    public ApiResponse getAllFriends(@PathVariable String version, @PathVariable String groupId) {
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

            return new ApiResponse(new FriendListResponse(response));
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new GroupNotFoundException();
        }
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/api/{version}/groups/{groupId}/friends")
    public ApiResponse createFriend(@PathVariable String version, @PathVariable String groupId,
                                    @RequestBody FriendRequest request) {

        logger.trace("Starting with version {} and groupId {}", version, groupId);

        if (isNotBlank(request.getName()) && isNotBlank(request.getSurname()) && request.getPayments() != null) {
            Optional<FriendGroup> group = groupRepository.findById(groupId);

            if (group.isPresent()) {
                Set<Payment> payments = new HashSet<>();
                Set<Friend> friendsToUpdate = new HashSet<>();

                if (!request.getPayments().isEmpty()) {
                    paymentRepository.findAllById(request.getPayments()).forEach(payment -> {
                        payments.add(payment);
                        Friend friendToUpdate = friendRepository.findByPayments(Collections.singleton(payment.getId()));

                        if (friendToUpdate != null) {
                            friendsToUpdate.add(friendToUpdate);
                        }
                    });

                    if (payments.isEmpty() || request.getPayments().size() != payments.size()) {
                        logger.error("At least one payment was not found in the DB {}", request.getPayments());
                        throw new BadRequestBodyException();
                    }
                }

                logger.debug("Creating friend with data {}", request);
                Friend friend = friendRepository.save(new FriendMember(request.getName(), request.getSurname(), request.getPayments()));

                logger.trace("Removing payment ids from friends {}", friendsToUpdate);
                friendsToUpdate.forEach(friendToUpdate -> {
                    Set<String> currentPayments = friendToUpdate.getPayments();
                    currentPayments.removeAll(request.getPayments());
                    friendToUpdate.setPayments(currentPayments);
                    friendRepository.save((FriendMember) friendToUpdate);
                });

                logger.info("Friend correctly created");
                return new ApiResponse(new FriendResponse(friend, payments));
            } else {
                logger.error("Group {} was not found in DB", groupId);
                throw new GroupNotFoundException();
            }
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new BadRequestBodyException();
        }
    }

    @GetMapping("/api/{version}/groups/{groupId}/friends/{friendId}")
    public ApiResponse getFriend(@PathVariable String version, @PathVariable String groupId, @PathVariable String friendId) {
        logger.trace("Starting with version {}, groupId {} and friendId {}", version, groupId, friendId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            if (group.get().getFriends().contains(friendId)) {
                Optional<FriendMember> friend = friendRepository.findById(friendId);

                if (friend.isPresent()) {
                    Set<Payment> payments = new HashSet<>();
                    paymentRepository.findAllById(friend.get().getPayments()).forEach(payments::add);
                    logger.debug("Friend {} was found with payments {}", friend.get(), payments);

                    logger.info("Retrieving friend {}", friendId);
                    return new ApiResponse(new FriendResponse(friend.get(), new HashSet<>(payments)));
                } else {
                    logger.error("Friend {} was not found in DB", friendId);
                    throw new FriendNotFoundException();
                }
            } else {
                logger.error("Group {} does not contain friend {}", groupId, friendId);
                throw new FriendNotFoundException();
            }
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new GroupNotFoundException();
        }
    }

    @PutMapping("/api/{version}/groups/{groupId}/friends/{friendId}")
    public ApiResponse updateFriend(@PathVariable String version, @PathVariable String groupId,
                                    @PathVariable String friendId, @RequestBody FriendRequest request) {
        logger.trace("Starting with version {}, groupId {} and friendId {}", version, groupId, friendId);

        if (isNotBlank(request.getName()) && isNotBlank(request.getSurname()) && request.getPayments() != null) {
            Optional<FriendGroup> group = groupRepository.findById(groupId);

            if (group.isPresent()) {
                if (group.get().getFriends().contains(friendId)) {
                    Optional<FriendMember> optionalFriend = friendRepository.findById(friendId);

                    if (optionalFriend.isPresent()) {
                        Friend friend = optionalFriend.get();
                        Set<Payment> newPayments = new HashSet<>();
                        Set<Friend> friendsToUpdate = new HashSet<>();

                        if (!request.getPayments().isEmpty()) {
                            paymentRepository.findAllById(request.getPayments()).forEach(payment -> {
                                newPayments.add(payment);
                                Friend friendToUpdate = friendRepository.findByPayments(Collections.singleton(payment.getId()));

                                if (friendToUpdate != null && !friendId.equals(friendToUpdate.getId())) {
                                    friendsToUpdate.add(friendToUpdate);
                                }
                            });

                            if (newPayments.isEmpty() || request.getPayments().size() != newPayments.size()) {
                                logger.error("At least one payment was not found in the DB {}", request.getPayments());
                                throw new BadRequestBodyException();
                            }
                        }

                        logger.debug("Updating friend with data {}", request);
                        friend.setName(request.getName());
                        friend.setSurname(request.getSurname());
                        friend.setPayments(request.getPayments());
                        friendRepository.save((FriendMember) friend);

                        logger.trace("Removing payment ids from friends {}", friendsToUpdate);
                        friendsToUpdate.forEach(friendToUpdate -> {
                            Set<String> currentPayments = friendToUpdate.getPayments();
                            currentPayments.removeAll(request.getPayments());
                            friendToUpdate.setPayments(currentPayments);
                            friendRepository.save((FriendMember) friendToUpdate);
                        });

                        logger.info("Friend {} correctly updated", friendId);
                        return new ApiResponse(new FriendResponse(friend, newPayments));
                    } else {
                        logger.error("Friend {} was not found in DB", friendId);
                        throw new FriendNotFoundException();
                    }
                } else {
                    logger.error("Group {} does not contain friend {}", groupId, friendId);
                    throw new FriendNotFoundException();
                }
            } else {
                logger.error("Group {} was not found in DB", groupId);
                throw new GroupNotFoundException();
            }
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new BadRequestBodyException();
        }
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/{version}/groups/{groupId}/friends/{friendId}")
    public void deleteFriend(@PathVariable String version, @PathVariable String groupId, @PathVariable String friendId) {
        logger.trace("Starting with version {}, groupId {} and friendId {}", version, groupId, friendId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            if (group.get().getFriends().contains(friendId)) {
                Optional<FriendMember> friend = friendRepository.findById(friendId);

                if (friend.isPresent()) {
                    logger.debug("Deleting friend {}", friend.get());
                    paymentRepository.deleteAllById(friend.get().getPayments());
                    friendRepository.delete(friend.get());

                    logger.trace("Removing friend from group");
                    Set<String> currentFriends = group.get().getFriends();
                    currentFriends.remove(friendId);
                    group.get().setFriends(currentFriends);
                    groupRepository.save(group.get());

                    logger.info("Friend {} successfully deleted", friendId);
                } else {
                    logger.error("Friend {} was not found in DB", friendId);
                    throw new FriendNotFoundException();
                }
            } else {
                logger.error("Group {} does not contain friend {}", groupId, friendId);
                throw new FriendNotFoundException();
            }
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new GroupNotFoundException();
        }
    }
}
