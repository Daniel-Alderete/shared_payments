package com.practice.shared_payment_backend.restservice;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.practice.shared_payment_backend.restservice.models.requests.GroupRequest;
import com.practice.shared_payment_backend.restservice.models.responses.ApiErrorResponse;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.GroupResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.AmountResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.GroupInfoResponse;
import com.practice.shared_payment_backend.restservice.models.responses.group.info.MinimumPaymentResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.practice.shared_payment_backend.restservice.FriendControllerTest.RANDOM_NUMBER;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestMethodOrder(MethodOrderer.MethodName.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerTest {
    private static final String GROUPS_ENDPOINT = "/api/v1/groups";
    private static final String GROUP_ENDPOINT = "/api/v1/groups/%s";
    private static final String GROUP_INFO_ENDPOINT = "/api/v1/groups/%s/info";

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

    private String getGroupInfoEndpointUrl(String groupId) {
        return String.format(GROUP_INFO_ENDPOINT, groupId);
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

        String result = new ApiResponse(new GroupListResponse(Arrays.asList(new GroupResponse(group1, new HashMap<>()),
                new GroupResponse(group2, new HashMap<>())))).toString();

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

        String result = new ApiResponse(new GroupListResponse(Arrays.asList(new GroupResponse(group1, new HashMap<>()),
                new GroupResponse(group2, new HashSet<>(Arrays.asList(friend1, friend2)))))).toString();

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

        String result = new ApiResponse(new GroupListResponse(Arrays.asList(new GroupResponse(group1, new HashMap<>()),
                new GroupResponse(group2, friendPaymentMap)))).toString();

        this.mockMvc.perform(get(getGroupsEndpointUrl()))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_EmptyBody_BadRequest() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_NullParameters_BadRequest() throws Exception {
        String body = new GroupRequest().toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_NullName_BadRequest() throws Exception {
        String body = new GroupRequest(null, "Test Description", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_NullDescription_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_NullFriends_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", "Test Description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_MissingName_BadRequest() throws Exception {
        String body = new GroupRequest(null, "Test Description", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_MissingDescription_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_MissingFriends_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", "Test description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_NoFriends_Created() throws Exception {
        String body = new GroupRequest("Test name", "Test description", new HashSet<>()).toString();

        ResultActions result = this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(1, groupRepository.count());
        Group group = groupRepository.findAll().get(0);
        assertEquals("Test name", group.getName());
        assertEquals("Test description", group.getDescription());
        assertTrue(group.getFriends().isEmpty());

        result.andExpect(content().json(new ApiResponse(new GroupResponse(group, new HashSet<>())).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_FriendNotFound_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(RANDOM_NUMBER)).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_WithFriendWithoutPayments_Created() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 1", "test surname", new HashSet<>()));
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(friend.getId())).toString();

        ResultActions result = this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(1, groupRepository.count());
        Group group = groupRepository.findAll().get(0);
        assertEquals("Test name", group.getName());
        assertEquals("Test description", group.getDescription());
        assertFalse(group.getFriends().isEmpty());
        assertTrue(group.getFriends().contains(friend.getId()));

        result.andExpect(content().json(new ApiResponse(new GroupResponse(group, Collections.singleton(friend))).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_WithFriendWithPayments_Created() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend = friendRepository.save(new FriendMember("Test Friend 1", "test surname",
                Collections.singleton(payment.getId())));
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(friend.getId())).toString();

        Map<Friend, Set<Payment>> friendPaymentMap = new HashMap<>();
        friendPaymentMap.put(friend, Collections.singleton(payment));

        ResultActions result = this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(1, groupRepository.count());
        Group group = groupRepository.findAll().get(0);
        assertEquals("Test name", group.getName());
        assertEquals("Test description", group.getDescription());
        assertFalse(group.getFriends().isEmpty());
        assertTrue(group.getFriends().contains(friend.getId()));

        result.andExpect(content().json(new ApiResponse(new GroupResponse(group, friendPaymentMap)).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createGroup_WithFriendAlreadyExisting_Created() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test surname", new HashSet<>()));
        groupRepository.save(new FriendGroup("Test name 2", "Test description",
                Collections.singleton(friend.getId())));
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(friend.getId())).toString();

        ResultActions result = this.mockMvc.perform(post(getGroupsEndpointUrl())
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(2, groupRepository.count());
        assertEquals(1, friendRepository.count());
        Group group = groupRepository.findAll().get(1);
        assertNotNull(group);
        assertFalse(group.getFriends().isEmpty());
        assertTrue(group.getFriends().contains(friend.getId()));
        Group existingGroup = groupRepository.findAll().get(0);
        assertNotNull(existingGroup);
        assertTrue(existingGroup.getFriends().isEmpty());

        result.andExpect(content().json(new ApiResponse(new GroupResponse(group, Collections.singleton(friend))).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroup_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(get(getGroupEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroup_GroupWithoutFriends_Ok() throws Exception {
        Group group = groupRepository.save(new FriendGroup("Test Group 1", "Test description", new HashSet<>()));
        String result = new ApiResponse(new GroupResponse(group, new HashMap<>())).toString();

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
        String result = new ApiResponse(new GroupResponse(group, new HashSet<>(Arrays.asList(friend1, friend2)))).toString();

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

        String result = new ApiResponse(new GroupResponse(group, friendPaymentMap)).toString();

        this.mockMvc.perform(get(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteGroup_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(delete(getGroupEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteGroup_GroupWithoutFriends_NoContent() throws Exception {
        Group group = groupRepository.save(new FriendGroup("Test Group 1", "Test description", new HashSet<>()));

        this.mockMvc.perform(delete(getGroupEndpointUrl(group.getId())))
                .andExpect(status().isNoContent());

        assertEquals(0, groupRepository.count());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteGroup_GroupWithFriendsWithoutPayments_NoContent() throws Exception {
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
    public void deleteGroup_GroupWithFriendsWithPayments_NoContent() throws Exception {
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

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_EmptyBody_BadRequest() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_NullParameters_BadRequest() throws Exception {
        String body = new GroupRequest().toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_NullName_BadRequest() throws Exception {
        String body = new GroupRequest(null, "Test Description", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_NullDescription_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_NullFriends_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", "Test Description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_MissingName_BadRequest() throws Exception {
        String body = new GroupRequest(null, "Test Description", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_MissingDescription_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_MissingFriends_BadRequest() throws Exception {
        String body = new GroupRequest("Test name", "Test description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_NoFriends_Ok() throws Exception {
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description", new HashSet<>()));
        String body = new GroupRequest("Test name", "Test description", new HashSet<>()).toString();

        ResultActions result = this.mockMvc.perform(put(getGroupEndpointUrl(group.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, groupRepository.count());
        group = groupRepository.findAll().get(0);
        assertEquals("Test name", group.getName());
        assertEquals("Test description", group.getDescription());
        assertTrue(group.getFriends().isEmpty());

        result.andExpect(content().json(new ApiResponse(new GroupResponse(group, new HashSet<>())).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_FriendNotFound_BadRequest() throws Exception {
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description", new HashSet<>()));
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(RANDOM_NUMBER)).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getGroupEndpointUrl(group.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_WithFriendWithoutPayments_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 1", "test surname", new HashSet<>()));
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                Collections.singleton(friend.getId())));
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(friend.getId())).toString();

        ResultActions result = this.mockMvc.perform(put(getGroupEndpointUrl(group.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, groupRepository.count());
        group = groupRepository.findAll().get(0);
        assertEquals("Test name", group.getName());
        assertEquals("Test description", group.getDescription());
        assertFalse(group.getFriends().isEmpty());
        assertTrue(group.getFriends().contains(friend.getId()));

        result.andExpect(content().json(new ApiResponse(new GroupResponse(group, Collections.singleton(friend))).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_WithFriendWithPayments_Ok() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend = friendRepository.save(new FriendMember("Test Friend 1", "test surname",
                Collections.singleton(payment.getId())));
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                Collections.singleton(friend.getId())));
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(friend.getId())).toString();

        Map<Friend, Set<Payment>> friendPaymentMap = new HashMap<>();
        friendPaymentMap.put(friend, Collections.singleton(payment));

        ResultActions result = this.mockMvc.perform(put(getGroupEndpointUrl(group.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, groupRepository.count());
        group = groupRepository.findAll().get(0);
        assertEquals("Test name", group.getName());
        assertEquals("Test description", group.getDescription());
        assertFalse(group.getFriends().isEmpty());
        assertTrue(group.getFriends().contains(friend.getId()));

        result.andExpect(content().json(new ApiResponse(new GroupResponse(group, friendPaymentMap)).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateGroup_WithGroupAlreadyExisting_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test surname", new HashSet<>()));
        groupRepository.save(new FriendGroup("Test name 2", "Test description",
                Collections.singleton(friend.getId())));
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description", new HashSet<>()));
        String body = new GroupRequest("Test name", "Test description",
                Collections.singleton(friend.getId())).toString();

        ResultActions result = this.mockMvc.perform(put(getGroupEndpointUrl(group.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(2, groupRepository.count());
        assertEquals(1, friendRepository.count());
        Group retrievedGroup = groupRepository.findAll().get(1);
        assertNotNull(retrievedGroup);
        assertFalse(retrievedGroup.getFriends().isEmpty());
        assertTrue(retrievedGroup.getFriends().contains(friend.getId()));
        Group existingGroup = groupRepository.findAll().get(0);
        assertNotNull(existingGroup);
        assertTrue(existingGroup.getFriends().isEmpty());

        result.andExpect(content().json(new ApiResponse(new GroupResponse(retrievedGroup, Collections.singleton(friend))).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithoutFriends_Ok() throws Exception {
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description", new HashSet<>()));
        String result = new ApiResponse(new GroupInfoResponse(new ArrayList<>(), new ArrayList<>())).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendWithoutExpenses_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test surname", new HashSet<>()));
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                Collections.singleton(friend.getId())));
        String result = new ApiResponse(new GroupInfoResponse(new ArrayList<>(), new ArrayList<>())).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendWithExpenses_Ok() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test surname",
                Collections.singleton(payment.getId())));
        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                Collections.singleton(friend.getId())));
        String result = new ApiResponse(new GroupInfoResponse(new ArrayList<>(), new ArrayList<>())).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendsWithExpenses_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.0f, "Cena",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Francisco", "Buyo",
                Collections.singleton(payment1.getId())));

        Payment payment2 = paymentRepository.save(new FriendPayment(10.0f, "Taxi",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment3 = paymentRepository.save(new FriendPayment(53.40f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Alfonso", "Pérez",
                new HashSet<>(Arrays.asList(payment2.getId(), payment3.getId()))));

        Friend friend3 = friendRepository.save(new FriendMember("Raúl", "González",
                new HashSet<>()));
        Friend friend4 = friendRepository.save(new FriendMember("José María", "Gutiérrez",
                new HashSet<>()));

        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId(), friend3.getId(), friend4.getId()))));

        MvcResult result = this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertNotNull(response.get("data"));
        assertNotNull(response.get("error"));
        JsonNode data = response.get("data");
        assertNotNull(data.get("debts"));
        assertTrue(data.get("debts").isArray());
        assertEquals(4, data.get("debts").size());
        assertNotNull(data.get("minimumPayment"));
        assertTrue(data.get("minimumPayment").isArray());
        assertEquals(2, data.get("minimumPayment").size());

        for (JsonNode debt : data.withArray("debts")) {
            assertNotNull(debt.get("friendId"));
            assertNotNull(debt.get("amount"));

            if (debt.get("friendId").asText().equals(friend1.getId())) {
                assertEquals("59.15", debt.get("amount").asText());
            } else if (debt.get("friendId").asText().equals(friend2.getId())) {
                assertEquals("22.55", debt.get("amount").asText());
            } else if (debt.get("friendId").asText().equals(friend3.getId())) {
                assertEquals("-40.85", debt.get("amount").asText());
            } else if (debt.get("friendId").asText().equals(friend4.getId())) {
                assertEquals("-40.85", debt.get("amount").asText());
            }
        }

        for (JsonNode minimumPayment : data.withArray("minimumPayment")) {
            assertNotNull(minimumPayment.get("friendId"));
            assertNotNull(minimumPayment.get("payments"));
            assertTrue(minimumPayment.get("payments").isArray());
        }
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendsWithExpenses2_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.0f, "Cena",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Francisco", "Buyo",
                Collections.singleton(payment1.getId())));

        Payment payment2 = paymentRepository.save(new FriendPayment(10.0f, "Taxi",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment3 = paymentRepository.save(new FriendPayment(53.40f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment4 = paymentRepository.save(new FriendPayment(1.0f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Alfonso", "Pérez",
                new HashSet<>(Arrays.asList(payment2.getId(), payment3.getId()))));

        Friend friend3 = friendRepository.save(new FriendMember("Raúl", "González",
                new HashSet<>()));
        Friend friend4 = friendRepository.save(new FriendMember("José María", "Gutiérrez",
                Collections.singleton(payment4.getId())));

        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId(), friend3.getId(), friend4.getId()))));

        AmountResponse debt1 = new AmountResponse(friend1.getId(), 58.9f, friend1.getName(), friend1.getSurname());
        AmountResponse debt2 = new AmountResponse(friend2.getId(), 22.3f, friend2.getName(), friend2.getSurname());
        AmountResponse debt3 = new AmountResponse(friend3.getId(), -41.1f, friend3.getName(), friend3.getSurname());
        AmountResponse debt4 = new AmountResponse(friend4.getId(), -40.1f, friend4.getName(), friend4.getSurname());

        AmountResponse minimumPayment1 = new AmountResponse(friend1.getId(), 41.1f, friend1.getName(), friend1.getSurname());
        AmountResponse minimumPayment2 = new AmountResponse(friend1.getId(), 17.8f, friend1.getName(), friend1.getSurname());
        AmountResponse minimumPayment3 = new AmountResponse(friend2.getId(), 22.3f, friend2.getName(), friend2.getSurname());

        MinimumPaymentResponse minimumPaymentResponse1 = new MinimumPaymentResponse(friend4.getId(), friend4.getName(),
                friend4.getSurname(), Arrays.asList(minimumPayment2, minimumPayment3));
        MinimumPaymentResponse minimumPaymentResponse2 = new MinimumPaymentResponse(friend3.getId(), friend3.getName(),
                friend3.getSurname(), Collections.singletonList(minimumPayment1));

        String result = new ApiResponse(new GroupInfoResponse(Arrays.asList(debt1, debt2, debt3, debt4),
                Arrays.asList(minimumPaymentResponse1, minimumPaymentResponse2))).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendsWithExpenses3_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.0f, "Cena",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Francisco", "Buyo",
                Collections.singleton(payment1.getId())));

        Payment payment2 = paymentRepository.save(new FriendPayment(10.0f, "Taxi",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment3 = paymentRepository.save(new FriendPayment(53.40f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment4 = paymentRepository.save(new FriendPayment(1.0f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Alfonso", "Pérez",
                new HashSet<>(Arrays.asList(payment2.getId(), payment3.getId()))));

        Friend friend3 = friendRepository.save(new FriendMember("Raúl", "González",
                new HashSet<>()));
        Friend friend4 = friendRepository.save(new FriendMember("José María", "Gutiérrez",
                Collections.singleton(payment4.getId())));

        Friend friend5 = friendRepository.save(new FriendMember("Pepe", "Viyuela",
                new HashSet<>()));

        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId(), friend3.getId(), friend4.getId(),
                        friend5.getId()))));

        AmountResponse debt1 = new AmountResponse(friend1.getId(), 67.12f, friend1.getName(), friend1.getSurname());
        AmountResponse debt2 = new AmountResponse(friend2.getId(), 30.52f, friend2.getName(), friend2.getSurname());
        AmountResponse debt3 = new AmountResponse(friend3.getId(), -32.88f, friend3.getName(), friend3.getSurname());
        AmountResponse debt4 = new AmountResponse(friend4.getId(), -31.88f, friend4.getName(), friend4.getSurname());
        AmountResponse debt5 = new AmountResponse(friend5.getId(), -32.88f, friend5.getName(), friend5.getSurname());

        AmountResponse minimumPayment1 = new AmountResponse(friend1.getId(), 32.88f, friend1.getName(), friend1.getSurname());
        AmountResponse minimumPayment2 = new AmountResponse(friend1.getId(), 1.36f, friend1.getName(), friend1.getSurname());
        AmountResponse minimumPayment3 = new AmountResponse(friend2.getId(), 30.52f, friend2.getName(), friend2.getSurname());
        AmountResponse minimumPayment4 = new AmountResponse(friend1.getId(), 32.88f, friend1.getName(), friend1.getSurname());

        MinimumPaymentResponse minimumPaymentResponse1 = new MinimumPaymentResponse(friend4.getId(), friend4.getName(),
                friend4.getSurname(), Arrays.asList(minimumPayment2, minimumPayment3));
        MinimumPaymentResponse minimumPaymentResponse2 = new MinimumPaymentResponse(friend3.getId(), friend3.getName(),
                friend3.getSurname(), Collections.singletonList(minimumPayment1));
        MinimumPaymentResponse minimumPaymentResponse3 = new MinimumPaymentResponse(friend5.getId(), friend5.getName(),
                friend5.getSurname(), Collections.singletonList(minimumPayment4));

        String result = new ApiResponse(new GroupInfoResponse(Arrays.asList(debt1, debt2, debt3, debt4, debt5),
                Arrays.asList(minimumPaymentResponse1, minimumPaymentResponse2, minimumPaymentResponse3))).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendsWithExpenses4_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.0f, "Cena",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Francisco", "Buyo",
                Collections.singleton(payment1.getId())));

        Payment payment2 = paymentRepository.save(new FriendPayment(10.0f, "Taxi",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment3 = paymentRepository.save(new FriendPayment(53.40f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment4 = paymentRepository.save(new FriendPayment(1.0f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Alfonso", "Pérez",
                new HashSet<>(Arrays.asList(payment2.getId(), payment3.getId()))));

        Friend friend3 = friendRepository.save(new FriendMember("Raúl", "González",
                new HashSet<>()));
        Friend friend4 = friendRepository.save(new FriendMember("José María", "Gutiérrez",
                Collections.singleton(payment4.getId())));

        Payment payment5 = paymentRepository.save(new FriendPayment(16.0f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend5 = friendRepository.save(new FriendMember("Pepe", "Viyuela",
                Collections.singleton(payment5.getId())));

        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId(), friend3.getId(), friend4.getId(),
                        friend5.getId()))));

        AmountResponse debt1 = new AmountResponse(friend1.getId(), 63.92f, friend1.getName(), friend1.getSurname());
        AmountResponse debt2 = new AmountResponse(friend2.getId(), 27.32f, friend2.getName(), friend2.getSurname());
        AmountResponse debt3 = new AmountResponse(friend3.getId(), -36.08f, friend3.getName(), friend3.getSurname());
        AmountResponse debt4 = new AmountResponse(friend4.getId(), -35.08f, friend4.getName(), friend4.getSurname());
        AmountResponse debt5 = new AmountResponse(friend5.getId(), -20.08f, friend5.getName(), friend5.getSurname());

        AmountResponse minimumPayment1 = new AmountResponse(friend1.getId(), 27.84f, friend1.getName(), friend1.getSurname());
        AmountResponse minimumPayment2 = new AmountResponse(friend1.getId(), 36.08f, friend1.getName(), friend1.getSurname());
        AmountResponse minimumPayment3 = new AmountResponse(friend2.getId(), 20.08f, friend2.getName(), friend2.getSurname());
        AmountResponse minimumPayment4 = new AmountResponse(friend2.getId(), 7.24f, friend2.getName(), friend2.getSurname());

        MinimumPaymentResponse minimumPaymentResponse1 = new MinimumPaymentResponse(friend4.getId(), friend4.getName(),
                friend4.getSurname(), Arrays.asList(minimumPayment1, minimumPayment4));
        MinimumPaymentResponse minimumPaymentResponse2 = new MinimumPaymentResponse(friend3.getId(), friend3.getName(),
                friend3.getSurname(), Collections.singletonList(minimumPayment2));
        MinimumPaymentResponse minimumPaymentResponse3 = new MinimumPaymentResponse(friend5.getId(), friend5.getName(),
                friend5.getSurname(), Collections.singletonList(minimumPayment3));

        String result = new ApiResponse(new GroupInfoResponse(Arrays.asList(debt1, debt2, debt3, debt4, debt5),
                Arrays.asList(minimumPaymentResponse1, minimumPaymentResponse2, minimumPaymentResponse3))).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendsWithExpenses5_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.0f, "Cena",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Francisco", "Buyo",
                Collections.singleton(payment1.getId())));

        Payment payment2 = paymentRepository.save(new FriendPayment(10.0f, "Taxi",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment3 = paymentRepository.save(new FriendPayment(53.40f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Alfonso", "Pérez",
                new HashSet<>(Arrays.asList(payment2.getId(), payment3.getId()))));

        Friend friend3 = friendRepository.save(new FriendMember("Raúl", "González",
                new HashSet<>()));

        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId(), friend3.getId()))));

        AmountResponse debt1 = new AmountResponse(friend1.getId(), 45.53f, friend1.getName(), friend1.getSurname());
        AmountResponse debt2 = new AmountResponse(friend2.getId(), 8.93f, friend2.getName(), friend2.getSurname());
        AmountResponse debt3 = new AmountResponse(friend3.getId(), -54.47f, friend3.getName(), friend3.getSurname());

        AmountResponse minimumPayment1 = new AmountResponse(friend1.getId(), 45.53f, friend1.getName(), friend1.getSurname());
        AmountResponse minimumPayment2 = new AmountResponse(friend2.getId(), 8.94f, friend2.getName(), friend2.getSurname());

        MinimumPaymentResponse minimumPaymentResponse2 = new MinimumPaymentResponse(friend3.getId(), friend3.getName(),
                friend3.getSurname(), Arrays.asList(minimumPayment1, minimumPayment2));

        String result = new ApiResponse(new GroupInfoResponse(Arrays.asList(debt1, debt2, debt3),
                Collections.singletonList(minimumPaymentResponse2))).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getGroupInfo_GroupWithFriendsWithExpenses6_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.0f, "Cena",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend1 = friendRepository.save(new FriendMember("Francisco", "Buyo",
                Collections.singleton(payment1.getId())));

        Payment payment2 = paymentRepository.save(new FriendPayment(100.0f, "Taxi",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Alfonso", "Pérez",
                new HashSet<>(Collections.singletonList(payment2.getId()))));

        Payment payment3 = paymentRepository.save(new FriendPayment(100.0f, "Compra",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend3 = friendRepository.save(new FriendMember("Raúl", "González",
                new HashSet<>(Collections.singletonList(payment3.getId()))));

        Group group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId(), friend3.getId()))));

        AmountResponse debt1 = new AmountResponse(friend1.getId(), 0.0f, friend1.getName(), friend1.getSurname());
        AmountResponse debt2 = new AmountResponse(friend2.getId(), 0.0f, friend2.getName(), friend2.getSurname());
        AmountResponse debt3 = new AmountResponse(friend3.getId(), 0.0f, friend3.getName(), friend3.getSurname());

        String result = new ApiResponse(new GroupInfoResponse(Arrays.asList(debt1, debt2, debt3),
                new ArrayList<>())).toString();

        this.mockMvc.perform(get(getGroupInfoEndpointUrl(group.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }
}
