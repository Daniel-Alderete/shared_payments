package com.practice.shared_payment_backend.restservice;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Group;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.common.BaseController;
import com.practice.shared_payment_backend.restservice.models.requests.GroupRequest;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.AmountResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.GroupInfoResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.MinimumPaymentResponse;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.practice.shared_payment_backend.repository.utils.FriendUtils.getFriendPaymentMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
public class GroupController extends BaseController {

    @GetMapping("/api/{version}/groups")
    public ApiResponse getAllGroups(@PathVariable String version) {
        logger.trace("Starting with version {}", version);
        List<GroupResponse> response = new ArrayList<>();

        groupRepository.findAll().forEach(group -> response.add(new GroupResponse(group,
                getFriendPaymentMap(group, friendRepository, paymentRepository))));

        return new ApiResponse(new GroupListResponse(response));
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/api/{version}/groups")
    public ApiResponse createGroup(@PathVariable String version, @RequestBody GroupRequest request) {
        logger.trace("Starting with version {}", version);

        if (isNotBlank(request.getName()) && isNotBlank(request.getDescription()) && request.getFriends() != null) {
            Set<Friend> friends = new HashSet<>();
            Set<Group> groupsToUpdate = new HashSet<>();

            if (!request.getFriends().isEmpty()) {
                friendRepository.findAllById(request.getFriends()).forEach(friend -> {
                    friends.add(friend);
                    Group group = groupRepository.findByFriends(Collections.singleton(friend.getId()));

                    if (group != null) {
                        groupsToUpdate.add(group);
                    }
                });

                if (friends.isEmpty() || request.getFriends().size() != friends.size()) {
                    logger.error("At least one friend was not found in the DB {}", request.getFriends());
                    throw new DataRetrievalFailureException("Payments not found");
                }
            }

            logger.debug("Creating group with data {}", request);
            Group group = groupRepository.save(new FriendGroup(request.getName(), request.getDescription(), request.getFriends()));

            logger.trace("Removing friend ids from groups {}", groupsToUpdate);
            groupsToUpdate.forEach(groupToUpdate -> {
                Set<String> currentFriends = groupToUpdate.getFriends();
                currentFriends.removeAll(request.getFriends());
                groupToUpdate.setFriends(currentFriends);
                groupRepository.save((FriendGroup) groupToUpdate);
            });

            logger.info("Group correctly created");
            return new ApiResponse(new GroupResponse(group, friends));
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new DataRetrievalFailureException("Missing parameter");
        }
    }

    @GetMapping("/api/{version}/groups/{groupId}")
    public ApiResponse getGroup(@PathVariable String version, @PathVariable String groupId) {
        logger.trace("Starting with version {} and groupId {}", version, groupId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            logger.info("Retrieving group {}", groupId);
            return new ApiResponse(new GroupResponse(group.get(), getFriendPaymentMap(group.get(), friendRepository,
                    paymentRepository)));
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new EmptyResultDataAccessException(1);
        }
    }

    @PutMapping("/api/{version}/groups/{groupId}")
    public ApiResponse updateGroup(@PathVariable String version, @PathVariable String groupId,
                                   @RequestBody GroupRequest request) {
        logger.trace("Starting with version {} and groupId {}", version, groupId);

        if (isNotBlank(request.getName()) && isNotBlank(request.getDescription()) && request.getFriends() != null) {
            Optional<FriendGroup> optionalGroup = groupRepository.findById(groupId);

            if (optionalGroup.isPresent()) {
                Group group = optionalGroup.get();
                Map<Friend, Set<Payment>> newFriendPaymentMap = new HashMap<>();
                Set<Group> groupsToUpdate = new HashSet<>();

                if (!request.getFriends().isEmpty()) {
                    friendRepository.findAllById(request.getFriends()).forEach(friend -> {
                        Set<Payment> payments = new HashSet<>();
                        paymentRepository.findAllById(friend.getPayments()).forEach(payments::add);
                        newFriendPaymentMap.put(friend, payments);

                        Group groupToUpdate = groupRepository.findByFriends(Collections.singleton(friend.getId()));

                        if (groupToUpdate != null && !groupId.equals(groupToUpdate.getId())) {
                            groupsToUpdate.add(groupToUpdate);
                        }
                    });

                    if (newFriendPaymentMap.isEmpty() || request.getFriends().size() != newFriendPaymentMap.size()) {
                        logger.error("At least one friend was not found in the DB {}", request.getFriends());
                        throw new DataRetrievalFailureException("Payments not found");
                    }
                }

                logger.debug("Updating group with data {}", request);
                group.setName(request.getName());
                group.setDescription(request.getDescription());
                group.setFriends(request.getFriends());
                groupRepository.save((FriendGroup) group);

                logger.trace("Removing friend ids from groups {}", groupsToUpdate);
                groupsToUpdate.forEach(groupToUpdate -> {
                    Set<String> currentFriends = groupToUpdate.getFriends();
                    currentFriends.removeAll(request.getFriends());
                    groupToUpdate.setFriends(currentFriends);
                    groupRepository.save((FriendGroup) groupToUpdate);
                });

                logger.info("Group {} correctly updated", groupId);
                return new ApiResponse(new GroupResponse(group, newFriendPaymentMap));
            } else {
                logger.error("Group {} was not found in DB", groupId);
                throw new EmptyResultDataAccessException(1);
            }
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new DataRetrievalFailureException("Missing parameter");
        }
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/{version}/groups/{groupId}")
    public void deleteGroup(@PathVariable String version, @PathVariable String groupId) {
        logger.trace("Starting with version {} and groupId {}", version, groupId);
        Optional<FriendGroup> optionalGroup = groupRepository.findById(groupId);

        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();

            friendRepository.findAllById(group.getFriends()).forEach(friend -> {
                logger.debug("Deleting friend {}", friend);
                paymentRepository.deleteAllById(friend.getPayments());
                friendRepository.delete(friend);
            });

            logger.debug("Deleting group {}", groupId);
            groupRepository.delete((FriendGroup) group);

            logger.info("Group {} successfully deleted", groupId);
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new EmptyResultDataAccessException(1);
        }
    }

    @GetMapping("/api/{version}/groups/{groupId}/info")
    public ApiResponse getGroupInfo(@PathVariable String version, @PathVariable String groupId) {
        logger.trace("Starting with version {} and groupId {}", version, groupId);
        Optional<FriendGroup> group = groupRepository.findById(groupId);

        if (group.isPresent()) {
            logger.debug("Searching for group {} info", groupId);
            List<AmountResponse> debts = new ArrayList<>();
            List<MinimumPaymentResponse> minimumPayment = new ArrayList<>();
            NavigableMap<Float, Friend> debtMap = new TreeMap<>();
            Map<Friend, Set<Payment>> friendPaymentMap = getFriendPaymentMap(group.get(), friendRepository, paymentRepository);
            int totalFriends = friendPaymentMap.size();

            if (totalFriends > 0) {
                float totalExpenses = 0;
                logger.debug("Group {} number of friends is {}", groupId, totalFriends);

                for (Set<Payment> payments : friendPaymentMap.values()) {
                    for (Payment payment : payments) {
                        totalExpenses += payment.getAmount();
                    }
                }

                logger.debug("Group {} total expenses are {}", groupId, totalExpenses);

                if (totalExpenses > 0) {
                    float sharedExpenses = totalExpenses / totalFriends;

                    for (Map.Entry<Friend, Set<Payment>> entry : friendPaymentMap.entrySet()) {
                        float friendExpenses = 0;

                        for (Payment payment : entry.getValue()) {
                            friendExpenses += payment.getAmount();
                        }

                        float debt = friendExpenses - sharedExpenses;

                        logger.info("Friend {} has a debt of {}", entry.getKey().getId(), debt);
                        debtMap.put(debt, entry.getKey());
                        debts.add(new AmountResponse(entry.getKey().getId(), debt));
                    }

                    minimumPayment = getMinimumPaymentResponse(calculateMinimumPayments(debtMap, new HashMap<>()));
                } else {
                    logger.info("Group {} has no expenses", groupId);
                }
            } else {
                logger.info("Group {} has no friends", groupId);
            }

            return new ApiResponse(new GroupInfoResponse(debts, minimumPayment));
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new EmptyResultDataAccessException(1);
        }
    }

    private Map<String, List<AmountResponse>> calculateMinimumPayments(NavigableMap<Float, Friend> debtMap,
                                                                       Map<String, List<AmountResponse>> minimumPayments) {

        Friend lowestDebtor = debtMap.lastEntry().getValue();
        Friend highestDebtor = debtMap.firstEntry().getValue();
        Float highestDebtorDebt = debtMap.firstEntry().getKey();
        Float lowestDebtorDebt = debtMap.lastEntry().getKey();
        NavigableMap<Float, Friend> newDebtMap = debtMap.subMap(highestDebtorDebt, false, lowestDebtorDebt, false);

        logger.debug("Calculating minimum payment between {} with a debt of {} and {} with a payment of {}",
                highestDebtor, highestDebtorDebt, lowestDebtor, lowestDebtorDebt);

        float remainingDebt = highestDebtorDebt + lowestDebtorDebt;
        float amount = 0;
        logger.debug("Remaining debt {}", remainingDebt);


        if (remainingDebt == 0) {
            logger.debug("Debt cancelled between lowest and highest debtor");
            amount = highestDebtorDebt;
        } else if (remainingDebt > 0) {
            logger.debug("Lowest debtor still has an amount to be payed");
            newDebtMap.put(remainingDebt, lowestDebtor);
            amount = lowestDebtorDebt;
        } else if (remainingDebt < 0) {
            logger.debug("Highest debtor still owes money");
            newDebtMap.put(remainingDebt, highestDebtor);
            amount = highestDebtorDebt;
        }

        List<AmountResponse> payments = minimumPayments.get(highestDebtor.getId());

        if (payments == null) {
            payments = new ArrayList<>();
        }

        payments.add(new AmountResponse(lowestDebtor.getId(), amount));
        minimumPayments.put(highestDebtor.getId(), payments);

        if (newDebtMap.size() > 0) {
            return calculateMinimumPayments(newDebtMap, minimumPayments);
        } else {
            logger.debug("Minimum payment calculation finished");
            return minimumPayments;
        }
    }

    private List<MinimumPaymentResponse> getMinimumPaymentResponse(Map<String, List<AmountResponse>> minimumPayments) {
        List<MinimumPaymentResponse> minimumPaymentResponses = new ArrayList<>();
        minimumPayments.forEach((key, value) -> minimumPaymentResponses.add(new MinimumPaymentResponse(key, value)));

        return minimumPaymentResponses;
    }
}
