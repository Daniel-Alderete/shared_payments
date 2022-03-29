package com.practice.shared_payment_backend.restservice;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Group;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.common.BaseController;
import com.practice.shared_payment_backend.restservice.models.exceptions.BadRequestBodyException;
import com.practice.shared_payment_backend.restservice.models.exceptions.GroupNotFoundException;
import com.practice.shared_payment_backend.restservice.models.requests.GroupRequest;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.AmountResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.GroupInfoResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.MinimumPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.practice.shared_payment_backend.repository.utils.FriendUtils.getFriendPaymentMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8081"})
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
            Set<Group> groupsToUpdate = new HashSet<>();
            Map<Friend, Set<Payment>> newFriendPaymentMap = new HashMap<>();

            if (!request.getFriends().isEmpty()) {
                friendRepository.findAllById(request.getFriends()).forEach(friend -> {
                    Set<Payment> payments = new HashSet<>();
                    paymentRepository.findAllById(friend.getPayments()).forEach(payments::add);
                    newFriendPaymentMap.put(friend, payments);
                    Group group = groupRepository.findByFriends(Collections.singleton(friend.getId()));

                    if (group != null) {
                        groupsToUpdate.add(group);
                    }
                });

                if (newFriendPaymentMap.isEmpty() || request.getFriends().size() != newFriendPaymentMap.size()) {
                    logger.error("At least one friend was not found in the DB {}", request.getFriends());
                    throw new BadRequestBodyException();
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
            return new ApiResponse(new GroupResponse(group, newFriendPaymentMap));
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new BadRequestBodyException();
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
            throw new GroupNotFoundException();
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
                        throw new BadRequestBodyException();
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
                throw new GroupNotFoundException();
            }
        } else {
            logger.error("Missing at least one parameter in the request");
            throw new BadRequestBodyException();
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
            throw new GroupNotFoundException();
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
            Map<Friend, BigDecimal> debtMap = new HashMap<>();
            Map<Friend, Set<Payment>> friendPaymentMap = getFriendPaymentMap(group.get(), friendRepository, paymentRepository);
            int totalFriends = friendPaymentMap.size();

            if (totalFriends > 1) {
                BigDecimal totalExpenses = BigDecimal.valueOf(0);
                logger.debug("Group {} number of friends is {}", groupId, totalFriends);

                for (Set<Payment> payments : friendPaymentMap.values()) {
                    for (Payment payment : payments) {
                        totalExpenses = totalExpenses.add(BigDecimal.valueOf(payment.getAmount()).setScale(2, RoundingMode.HALF_EVEN));
                    }
                }

                logger.debug("Group {} total expenses are {}", groupId, totalExpenses);

                if (totalExpenses.compareTo(BigDecimal.valueOf(0)) > 0) {
                    BigDecimal sharedExpenses = totalExpenses.divide(BigDecimal.valueOf(totalFriends), 2, RoundingMode.HALF_EVEN);

                    for (Map.Entry<Friend, Set<Payment>> entry : friendPaymentMap.entrySet()) {
                        BigDecimal friendExpenses = BigDecimal.valueOf(0);

                        for (Payment payment : entry.getValue()) {
                            friendExpenses = friendExpenses.add(BigDecimal.valueOf(payment.getAmount()).setScale(2, RoundingMode.HALF_EVEN));
                        }

                        BigDecimal debt = friendExpenses.subtract(sharedExpenses);

                        logger.info("Friend {} has a debt of {}", entry.getKey().getId(), debt);
                        debtMap.put(entry.getKey(), debt);
                        debts.add(new AmountResponse(entry.getKey().getId(), debt.floatValue(), entry.getKey().getName(),
                                entry.getKey().getSurname()));
                    }

                    minimumPayment = getMinimumPaymentResponse(calculateMinimumPayments(debtMap, new HashMap<>()));
                } else {
                    logger.info("Group {} has no expenses", groupId);
                }
            } else {
                logger.info("Group {} has either no friends or only one", groupId);
            }

            return new ApiResponse(new GroupInfoResponse(debts, minimumPayment));
        } else {
            logger.error("Group {} was not found in DB", groupId);
            throw new GroupNotFoundException();
        }
    }

    private Map<Friend, List<AmountResponse>> calculateMinimumPayments(Map<Friend, BigDecimal> debtMap,
                                                                       Map<Friend, List<AmountResponse>> minimumPayments) {
        Map.Entry<Friend, BigDecimal> lowestDebtorEntry = getLowestDebtor(debtMap);
        Map.Entry<Friend, BigDecimal> highestDebtorEntry = getHighestDebtor(debtMap);
        Friend lowestDebtor = lowestDebtorEntry.getKey();
        Friend highestDebtor = highestDebtorEntry.getKey();
        BigDecimal highestDebtorDebt = highestDebtorEntry.getValue();
        BigDecimal lowestDebtorDebt = lowestDebtorEntry.getValue();

        logger.debug("Calculating minimum payment between {} with a debt of {} and {} with a payment of {}",
                highestDebtor, highestDebtorDebt, lowestDebtor, lowestDebtorDebt);

        BigDecimal remainingDebt = highestDebtorDebt.add(lowestDebtorDebt);
        logger.debug("Remaining debt {}", remainingDebt);

        BigDecimal amount = BigDecimal.valueOf(0);
        debtMap.remove(highestDebtor);
        debtMap.remove(lowestDebtor);

        if (remainingDebt.compareTo(BigDecimal.valueOf(0)) == 0) {
            logger.debug("Debt cancelled between lowest and highest debtor");
            amount = highestDebtorDebt;
        } else if (remainingDebt.compareTo(BigDecimal.valueOf(0)) > 0) {
            logger.debug("Lowest debtor still has an amount to be payed");
            debtMap.put(lowestDebtor, remainingDebt);
            amount = highestDebtorDebt;
        } else if (remainingDebt.compareTo(BigDecimal.valueOf(0)) < 0) {
            logger.debug("Highest debtor still owes money");
            debtMap.put(highestDebtor, remainingDebt);
            amount = lowestDebtorDebt;
        }

        List<AmountResponse> payments = minimumPayments.get(highestDebtor);

        if (payments == null) {
            payments = new ArrayList<>();
        }

        payments.add(new AmountResponse(lowestDebtor.getId(), amount.abs().floatValue(), lowestDebtor.getName(),
                lowestDebtor.getSurname()));
        minimumPayments.put(highestDebtor, payments);

        if (debtMap.size() > 0) {
            return calculateMinimumPayments(debtMap, minimumPayments);
        } else {
            logger.debug("Minimum payment calculation finished");
            return minimumPayments;
        }
    }

    private List<MinimumPaymentResponse> getMinimumPaymentResponse(Map<Friend, List<AmountResponse>> minimumPayments) {
        List<MinimumPaymentResponse> minimumPaymentResponses = new ArrayList<>();
        minimumPayments.forEach((key, value) -> minimumPaymentResponses.add(new MinimumPaymentResponse(key.getId(),
                key.getName(), key.getSurname(), value)));

        return minimumPaymentResponses;
    }

    private Map.Entry<Friend, BigDecimal> getLowestDebtor(Map<Friend, BigDecimal> debtMap) {
        BigDecimal debt = BigDecimal.valueOf(0);
        Map.Entry<Friend, BigDecimal> lowestDebtor = null;

        for (Map.Entry<Friend, BigDecimal> entry : debtMap.entrySet()) {
            if (entry.getValue().compareTo(debt) >= 0) {
                lowestDebtor = entry;
                debt = entry.getValue();
            }
        }

        return lowestDebtor;
    }

    private Map.Entry<Friend, BigDecimal> getHighestDebtor(Map<Friend, BigDecimal> debtMap) {
        BigDecimal debt = BigDecimal.valueOf(0);
        Map.Entry<Friend, BigDecimal> lowestDebtor = null;

        for (Map.Entry<Friend, BigDecimal> entry : debtMap.entrySet()) {
            if (entry.getValue().compareTo(debt) <= 0) {
                lowestDebtor = entry;
                debt = entry.getValue();
            }
        }

        return lowestDebtor;
    }
}
