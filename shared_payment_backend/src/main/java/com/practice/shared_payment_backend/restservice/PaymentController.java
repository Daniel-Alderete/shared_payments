package com.practice.shared_payment_backend.restservice;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.friends.FriendMember;
import com.practice.shared_payment_backend.models.friends.FriendPayment;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.common.BaseController;
import com.practice.shared_payment_backend.restservice.models.requests.PaymentRequest;
import com.practice.shared_payment_backend.restservice.models.responses.PaymentResponse;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
public class PaymentController extends BaseController {

    @GetMapping("/api/{version}/groups/{groupId}/friends/{friendId}/payments")
    public List<PaymentResponse> getAllPayments(@PathVariable String version, @PathVariable String groupId,
                                                @PathVariable String friendId) {
        logger.trace("Starting with version {}, groupId {} and friendId {}", version, groupId, friendId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            if (group.get().getFriends().contains(friendId)) {
                Optional<FriendMember> friend = friendRepository.findById(friendId);

                if (friend.isPresent()) {
                    logger.debug("Returning payments {}", friend.get().getPayments());
                    List<PaymentResponse> response = new ArrayList<>();
                    paymentRepository.findAllById(friend.get().getPayments())
                            .forEach(payment -> response.add(new PaymentResponse(payment)));

                    return response;
                } else {
                    logger.error("Friend {} was not found in DB", friendId);
                    throw new EmptyResultDataAccessException(1);
                }
            } else {
                logger.error("Group {} does not contain friend {}", groupId, friendId);
                throw new EmptyResultDataAccessException(1);
            }
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new EmptyResultDataAccessException(1);
        }
    }

    @ResponseStatus(value = HttpStatus.CREATED, reason = "Created")
    @PostMapping("/api/{version}/groups/{groupId}/friends/{friendId}/payments")
    public PaymentResponse createPayment(@PathVariable String version, @PathVariable String groupId,
                                         @PathVariable String friendId, @RequestBody PaymentRequest request) {

        logger.trace("Starting with version {}, groupId {} and friendId {}", version, groupId, friendId);

        if (request.getAmount() != null && isNotBlank(request.getDescription()) && request.getDate() != null) {
            Optional<FriendGroup> group = groupRepository.findById(groupId);

            if (group.isPresent()) {
                if (group.get().getFriends().contains(friendId)) {
                    Optional<FriendMember> friend = friendRepository.findById(friendId);

                    if (friend.isPresent()) {
                        logger.debug("Creating payment with data {}", request);
                        Payment payment = paymentRepository.save(new FriendPayment(request.getAmount(),
                                request.getDescription(), request.getDate()));

                        logger.trace("Updating friend payments list");
                        Set<String> currentPayments = friend.get().getPayments();
                        currentPayments.add(payment.getId());
                        friend.get().setPayments(currentPayments);
                        friendRepository.save(friend.get());

                        logger.info("Payment correctly created");
                        return new PaymentResponse(payment);
                    } else {
                        logger.error("Friend {} was not found in DB", friendId);
                        throw new EmptyResultDataAccessException(1);
                    }
                } else {
                    logger.error("Group {} does not contain friend {}", groupId, friendId);
                    throw new EmptyResultDataAccessException(1);
                }
            } else {
                logger.error("Group {} was not found in DB", groupId);
                throw new EmptyResultDataAccessException(1);
            }
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new DataRetrievalFailureException("Missing parameter");
        }
    }

    @GetMapping("/api/{version}/groups/{groupId}/friends/{friendId}/payments/{paymentId}")
    public PaymentResponse getPayment(@PathVariable String version, @PathVariable String groupId,
                                      @PathVariable String friendId, @PathVariable String paymentId) {
        logger.trace("Starting with version {}, groupId {}, friendId {} and paymentId {}", version, groupId, friendId, paymentId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            if (group.get().getFriends().contains(friendId)) {
                Optional<FriendMember> friend = friendRepository.findById(friendId);

                if (friend.isPresent()) {
                    if (friend.get().getPayments().contains(paymentId)) {
                        Optional<FriendPayment> payment = paymentRepository.findById(paymentId);

                        if (payment.isPresent()) {
                            logger.info("Retrieving payment {}", paymentId);
                            return new PaymentResponse(payment.get());
                        } else {
                            logger.error("Payment {} was not found in DB", paymentId);
                            throw new EmptyResultDataAccessException(1);
                        }
                    } else {
                        logger.error("Friend {} does not contain payment {}", friendId, paymentId);
                        throw new EmptyResultDataAccessException(1);
                    }
                } else {
                    logger.error("Friend {} was not found in DB", friendId);
                    throw new EmptyResultDataAccessException(1);
                }
            } else {
                logger.error("Group {} does not contain friend {}", groupId, friendId);
                throw new EmptyResultDataAccessException(1);
            }
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new EmptyResultDataAccessException(1);
        }
    }

    @PutMapping("/api/{version}/groups/{groupId}/friends/{friendId}/payments/{paymentId}")
    public PaymentResponse updatePayment(@PathVariable String version, @PathVariable String groupId,
                                         @PathVariable String friendId, @PathVariable String paymentId,
                                         @RequestBody PaymentRequest request) {
        logger.trace("Starting with version {}, groupId {}, friendId {} and paymentId {}", version, groupId, friendId, paymentId);

        if (request.getAmount() != null && isNotBlank(request.getDescription()) && request.getDate() != null) {
            Optional<FriendGroup> group = groupRepository.findById(groupId);

            if (group.isPresent()) {
                if (group.get().getFriends().contains(friendId)) {
                    Optional<FriendMember> friend = friendRepository.findById(friendId);

                    if (friend.isPresent()) {
                        Optional<FriendPayment> optionalPayment = paymentRepository.findById(paymentId);

                        if (optionalPayment.isPresent()) {
                            Payment payment = optionalPayment.get();
                            logger.debug("Updating payment with data {}", request);

                            payment.setAmount(request.getAmount());
                            payment.setDescription(request.getDescription());
                            payment.setDate(request.getDate());
                            paymentRepository.save((FriendPayment) payment);

                            logger.info("Payment correctly updated {}", paymentId);
                            return new PaymentResponse(payment);
                        } else {
                            logger.error("Payment {} was not found in DB", paymentId);
                            throw new EmptyResultDataAccessException(1);
                        }
                    } else {
                        logger.error("Friend {} was not found in DB", friendId);
                        throw new EmptyResultDataAccessException(1);
                    }
                } else {
                    logger.error("Group {} does not contain friend {}", groupId, friendId);
                    throw new EmptyResultDataAccessException(1);
                }
            } else {
                logger.error("Group {} was not found in DB", groupId);
                throw new EmptyResultDataAccessException(1);
            }
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new DataRetrievalFailureException("Missing parameter");
        }
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT, reason = "No Content")
    @DeleteMapping("/api/{version}/groups/{groupId}/friends/{friendId}/payments/{paymentId}")
    public void deletePayment(@PathVariable String version, @PathVariable String groupId, @PathVariable String friendId,
                              @PathVariable String paymentId) {
        logger.trace("Starting with version {}, groupId {}, friendId {} and paymentId {}", version, groupId, friendId, paymentId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            if (group.get().getFriends().contains(friendId)) {
                Optional<FriendMember> friend = friendRepository.findById(friendId);

                if (friend.isPresent()) {
                    if (friend.get().getPayments().contains(paymentId)) {
                        Optional<FriendPayment> payment = paymentRepository.findById(paymentId);

                        if (payment.isPresent()) {
                            logger.debug("Deleting payment {}", paymentId);
                            paymentRepository.deleteById(paymentId);

                            logger.trace("Updating friend payments list");
                            Set<String> currentPayments = friend.get().getPayments();
                            currentPayments.remove(paymentId);
                            friend.get().setPayments(currentPayments);
                            friendRepository.save(friend.get());

                            logger.info("Payment {} successfully deleted", paymentId);
                        } else {
                            logger.error("Payment {} was not found in DB", paymentId);
                            throw new EmptyResultDataAccessException(1);
                        }
                    } else {
                        logger.error("Friend {} does not contain payment {}", friendId, paymentId);
                        throw new EmptyResultDataAccessException(1);
                    }
                } else {
                    logger.error("Friend {} was not found in DB", friendId);
                    throw new EmptyResultDataAccessException(1);
                }
            } else {
                logger.error("Group {} does not contain friend {}", groupId, friendId);
                throw new EmptyResultDataAccessException(1);
            }
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new EmptyResultDataAccessException(1);
        }
    }
}
