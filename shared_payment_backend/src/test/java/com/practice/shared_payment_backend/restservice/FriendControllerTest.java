package com.practice.shared_payment_backend.restservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.friends.FriendMember;
import com.practice.shared_payment_backend.models.friends.FriendPayment;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Group;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.repository.FriendRepository;
import com.practice.shared_payment_backend.repository.GroupRepository;
import com.practice.shared_payment_backend.repository.PaymentRepository;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import com.practice.shared_payment_backend.restservice.models.responses.friend.FriendListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.friend.FriendResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class FriendControllerTest {
    private static final String FRIENDS_ENDPOINT = "/api/v1/groups/%s/friends";
    private static final String FRIEND_ENDPOINT = "/api/v1/groups/%s/friends/%s";
    private static final String RANDOM_NUMBER = "123456";
    private static Group group;
    private static String groupId;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private MockMvc mockMvc;

    private String getFriendsEndpointUrl(String groupId) {
        return String.format(FRIENDS_ENDPOINT, groupId);
    }

    private String getFriendEndpointUrl(String groupId, String friendId) {
        return String.format(FRIEND_ENDPOINT, groupId, friendId);
    }

    @BeforeEach
    public void setUp() {
        group = groupRepository.save(new FriendGroup("Test Group", "Test description", new HashSet<>()));
        groupId = group.getId();
    }

    @AfterEach
    public void tearDown() {
        groupRepository.deleteAll();
        friendRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllFriends_Empty_Ok() throws Exception {
        this.mockMvc.perform(get(getFriendsEndpointUrl(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().json("{'data':{'friends':[]},'error':null}"));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllFriends_GroupNotFound_NotFound() throws Exception {
        this.mockMvc.perform(get(getFriendsEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllFriends_FriendsFoundNoPayments_Ok() throws Exception {
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description", new HashSet<>()));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));
        group.setFriends(new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId())));
        groupRepository.save((FriendGroup) group);
        String result = new ObjectMapper().valueToTree(new ApiResponse(new FriendListResponse(
                Arrays.asList(new FriendResponse(friend1), new FriendResponse(friend2))))).toString();

        this.mockMvc.perform(get(getFriendsEndpointUrl(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllFriends_FriendsFoundWithPayments_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment2 = paymentRepository.save(new FriendPayment(196.7f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description",
                new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId()))));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));
        group.setFriends(new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId())));
        groupRepository.save((FriendGroup) group);
        String result = new ObjectMapper().valueToTree(new ApiResponse(new FriendListResponse(
                Arrays.asList(new FriendResponse(friend1, new HashSet<>(Arrays.asList(payment1, payment2))),
                        new FriendResponse(friend2))))).toString();

        this.mockMvc.perform(get(getFriendsEndpointUrl(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }
}
