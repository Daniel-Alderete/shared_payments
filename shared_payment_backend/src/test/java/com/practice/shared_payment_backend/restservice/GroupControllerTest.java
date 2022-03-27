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
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestMethodOrder(MethodOrderer.MethodName.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerTest {
    private static final String GROUPS_ENDPOINT = "/api/v1/groups";
    private static final String GROUP_ENDPOINT = "/api/v1/groups/%s";
    private static final String RANDOM_NUMBER = "123456";

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private MockMvc mockMvc;

    private String getGroupsEndpointUrl() {
        return GROUPS_ENDPOINT;
    }

    private String getGroupEndpointUrl(String groupId) {
        return String.format(GROUP_ENDPOINT, groupId);
    }

    @AfterEach
    public void tearDown() {
        groupRepository.deleteAll();
        friendRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllGroups_Empty_Ok() throws Exception {
        this.mockMvc.perform(get(getGroupsEndpointUrl()))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().createObjectNode().toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllGroups_GroupsFoundNoFriends_Ok() throws Exception {
        Group group1 = groupRepository.save(new FriendGroup("Test Group 1", "Test description", new HashSet<>()));
        Group group2 = groupRepository.save(new FriendGroup("Test Group 2", "Test description", new HashSet<>()));

        String result = new ObjectMapper().writeValueAsString(new ApiResponse(new GroupListResponse(
                Arrays.asList(new GroupResponse(group1, new HashMap<>()), new GroupResponse(group2, new HashMap<>())))));

        this.mockMvc.perform(get(getGroupsEndpointUrl()))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllGroups_GroupsFoundWithFriends_Ok() throws Exception {
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description", new HashSet<>()));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));
        Group group1 = groupRepository.save(new FriendGroup("Test Group 1", "Test description", new HashSet<>()));
        Group group2 = groupRepository.save(new FriendGroup("Test Group 2", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId()))));

        String result = new ObjectMapper().writeValueAsString(new ApiResponse(new GroupListResponse(
                Arrays.asList(new GroupResponse(group1, new HashMap<>()), new GroupResponse(group2,
                        new HashSet<>(Arrays.asList(friend1, friend2)))))));

        this.mockMvc.perform(get(getGroupsEndpointUrl()))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllGroups_GroupsFoundWithFriendsWithPayments_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment2 = paymentRepository.save(new FriendPayment(196.7f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description",
                new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId()))));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));

        Group group1 = groupRepository.save(new FriendGroup("Test Group 1", "Test description", new HashSet<>()));
        Group group2 = groupRepository.save(new FriendGroup("Test Group 2", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId()))));

        Map<Friend, Set<Payment>> friendPaymentMap = new HashMap<>();
        friendPaymentMap.put(friend1, new HashSet<>(Arrays.asList(payment1, payment2)));
        friendPaymentMap.put(friend2, new HashSet<>());

        String result = new ObjectMapper().writeValueAsString(new ApiResponse(new GroupListResponse(
                Arrays.asList(new GroupResponse(group1, new HashMap<>()), new GroupResponse(group2, friendPaymentMap)))));

        this.mockMvc.perform(get(getGroupsEndpointUrl()))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }
/*
    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_EmptyBody_BadRequest() throws Exception {
        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullParameters_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest());

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullName_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest(null, "Test surname", new HashSet<>()));

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullSurname_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", null, new HashSet<>()));

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullPayments_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test description", null));

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingName_BadRequest() throws Exception {
        String body = ((ObjectNode) new ObjectMapper()
                .valueToTree(new FriendRequest(null, "Test surname", new HashSet<>())))
                .remove("name").toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingSurname_BadRequest() throws Exception {
        String body = ((ObjectNode) new ObjectMapper()
                .valueToTree(new FriendRequest("Test name", null, new HashSet<>())))
                .remove("surname").toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingPayments_BadRequest() throws Exception {
        String body = ((ObjectNode) new ObjectMapper()
                .valueToTree(new FriendRequest("Test name", "Test description", null)))
                .remove("payments").toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingGroup_NotFound() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname", new HashSet<>()));

        this.mockMvc.perform(post(getFriendsEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NoPayments_Created() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname", new HashSet<>()));

        ResultActions result = this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(1, friendRepository.count());
        Friend friend = friendRepository.findAll().get(0);
        assertEquals("Test name", friend.getName());
        assertEquals("Test surname", friend.getSurname());
        assertTrue(friend.getPayments().isEmpty());

        result.andExpect(content().json(new ObjectMapper().writeValueAsString(new ApiResponse(new FriendResponse(friend)))));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_PaymentNotFound_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname",
                new HashSet<>(Collections.singletonList(RANDOM_NUMBER))));

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_WithPayment_Created() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname",
                new HashSet<>(Collections.singletonList(payment.getId()))));

        ResultActions result = this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(1, friendRepository.count());
        Friend friend = friendRepository.findAll().get(0);
        assertEquals("Test name", friend.getName());
        assertEquals("Test surname", friend.getSurname());
        assertFalse(friend.getPayments().isEmpty());
        assertTrue(friend.getPayments().contains(payment.getId()));

        result.andExpect(content().json(new ObjectMapper()
                .writeValueAsString(new ApiResponse(new FriendResponse(friend, Collections.singleton(payment))))));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_WithPaymentAlreadyExisting_Created() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        friendRepository.save(new FriendMember("Test Friend 2", "Test Description",
                new HashSet<>(Collections.singletonList(payment.getId()))));

        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname",
                new HashSet<>(Collections.singletonList(payment.getId()))));

        ResultActions result = this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(2, friendRepository.count());
        assertEquals(1, paymentRepository.count());
        Friend friend = friendRepository.findByNameAndSurname("Test name", "Test surname");
        assertNotNull(friend);
        assertFalse(friend.getPayments().isEmpty());
        assertTrue(friend.getPayments().contains(payment.getId()));
        Friend existingFriend = friendRepository.findByNameAndSurname("Test Friend 2", "Test Description");
        assertNotNull(existingFriend);
        assertTrue(existingFriend.getPayments().isEmpty());

        result.andExpect(content().json(new ObjectMapper()
                .writeValueAsString(new ApiResponse(new FriendResponse(friend, Collections.singleton(payment))))));
    }*/

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroup_GroupNotFound_NotFound() throws Exception {
        this.mockMvc.perform(get(getGroupEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroup_GroupWithoutFriends_Ok() throws Exception {
        Group group = groupRepository.save(new FriendGroup("Test Group 1", "Test description", new HashSet<>()));
        String result = new ObjectMapper().writeValueAsString(new ApiResponse(new GroupResponse(group, new HashMap<>())));

        this.mockMvc.perform(get(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroup_GroupWithFriendsWithoutPayments_Ok() throws Exception {
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description", new HashSet<>()));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));
        Group group = groupRepository.save(new FriendGroup("Test Group 2", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId()))));
        String result = new ObjectMapper().writeValueAsString(new ApiResponse(new GroupResponse(group, new HashSet<>(Arrays.asList(friend1, friend2)))));

        this.mockMvc.perform(get(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroup_GroupWithFriendsWithPayments_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment2 = paymentRepository.save(new FriendPayment(196.7f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description",
                new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId()))));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));

        Group group = groupRepository.save(new FriendGroup("Test Group 2", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId()))));

        Map<Friend, Set<Payment>> friendPaymentMap = new HashMap<>();
        friendPaymentMap.put(friend1, new HashSet<>(Arrays.asList(payment1, payment2)));
        friendPaymentMap.put(friend2, new HashSet<>());

        String result = new ObjectMapper().writeValueAsString(new ApiResponse(new GroupResponse(group, friendPaymentMap)));

        this.mockMvc.perform(get(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteGroup_GroupNotFound_NotFound() throws Exception {
        this.mockMvc.perform(delete(getGroupEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteGroup_GroupWithoutFriends_Ok() throws Exception {
        Group group = groupRepository.save(new FriendGroup("Test Group 1", "Test description", new HashSet<>()));

        this.mockMvc.perform(delete(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isNoContent());

        assertEquals(0, groupRepository.count());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteGroup_GroupWithFriendsWithoutPayments_Ok() throws Exception {
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description", new HashSet<>()));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));
        Group group = groupRepository.save(new FriendGroup("Test Group 2", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId()))));

        this.mockMvc.perform(delete(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isNoContent());

        assertEquals(0, groupRepository.count());
        assertEquals(0, friendRepository.count());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteGroup_GroupWithFriendsWithPayments_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment2 = paymentRepository.save(new FriendPayment(196.7f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "Test Description",
                new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId()))));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));

        Group group = groupRepository.save(new FriendGroup("Test Group 2", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId()))));

        this.mockMvc.perform(delete(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isNoContent());

        assertEquals(0, groupRepository.count());
        assertEquals(0, friendRepository.count());
        assertEquals(0, paymentRepository.count());
    }

    /*

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_EmptyBody_BadRequest() throws Exception {
        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullParameters_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest());

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullName_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest(null, "Test surname", new HashSet<>()));

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullSurname_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", null, new HashSet<>()));

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullPayments_BadRequest() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test description", null));

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingName_BadRequest() throws Exception {
        String body = ((ObjectNode) new ObjectMapper()
                .valueToTree(new FriendRequest(null, "Test surname", new HashSet<>())))
                .remove("name").toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingSurname_BadRequest() throws Exception {
        String body = ((ObjectNode) new ObjectMapper()
                .valueToTree(new FriendRequest("Test name", null, new HashSet<>())))
                .remove("surname").toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingPayments_BadRequest() throws Exception {
        String body = ((ObjectNode) new ObjectMapper()
                .valueToTree(new FriendRequest("Test name", "Test description", null)))
                .remove("payments").toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingGroup_NotFound() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname", new HashSet<>()));

        this.mockMvc.perform(put(getFriendEndpointUrl(RANDOM_NUMBER, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_FriendNotInGroup_NotFound() throws Exception {
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname", new HashSet<>()));

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_FriendNotFound_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname", new HashSet<>()));

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NoPayments_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname", new HashSet<>()));

        ResultActions result = this.mockMvc.perform(put(getFriendEndpointUrl(groupId, friend.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, friendRepository.count());
        friend = friendRepository.findAll().get(0);
        assertEquals("Test name", friend.getName());
        assertEquals("Test surname", friend.getSurname());
        assertTrue(friend.getPayments().isEmpty());

        result.andExpect(content().json(new ObjectMapper().writeValueAsString(new ApiResponse(new FriendResponse(friend)))));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_PaymentNotFound_BadRequest() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test Description", new HashSet<>()));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);
        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname",
                new HashSet<>(Collections.singletonList(RANDOM_NUMBER))));

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, friend.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_WithPayment_Ok() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test Description",
                Collections.singleton(payment.getId())));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);

        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname",
                new HashSet<>()));

        ResultActions result = this.mockMvc.perform(put(getFriendEndpointUrl(groupId, friend.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, friendRepository.count());
        friend = friendRepository.findAll().get(0);
        assertEquals("Test name", friend.getName());
        assertEquals("Test surname", friend.getSurname());
        assertTrue(friend.getPayments().isEmpty());
        assertEquals(1, paymentRepository.count());

        result.andExpect(content().json(new ObjectMapper()
                .writeValueAsString(new ApiResponse(new FriendResponse(friend, new HashSet<>())))));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_WithEmptyPayment_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 1", "Test Description", new HashSet<>()));
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "Test Description",
                Collections.singleton(payment.getId())));
        group.setFriends(new HashSet<>(Arrays.asList(friend.getId(), friend2.getId())));
        groupRepository.save((FriendGroup) group);

        String body = new ObjectMapper().writeValueAsString(new FriendRequest("Test name", "Test surname",
                new HashSet<>(Collections.singletonList(payment.getId()))));

        ResultActions result = this.mockMvc.perform(put(getFriendEndpointUrl(groupId, friend.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(2, friendRepository.count());
        friend2 = friendRepository.findByNameAndSurname("Test Friend 2", "Test Description");
        assertNotNull(friend2);
        assertTrue(friend2.getPayments().isEmpty());
        friend = friendRepository.findByNameAndSurname("Test name", "Test surname");
        assertNotNull(friend);
        assertFalse(friend.getPayments().isEmpty());
        assertTrue(friend.getPayments().contains(payment.getId()));

        result.andExpect(content().json(new ObjectMapper()
                .writeValueAsString(new ApiResponse(new FriendResponse(friend, Collections.singleton(payment))))));
    }*/
}
