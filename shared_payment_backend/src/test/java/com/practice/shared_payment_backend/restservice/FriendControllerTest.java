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
import com.practice.shared_payment_backend.restservice.models.requests.FriendRequest;
import com.practice.shared_payment_backend.restservice.models.responses.ApiErrorResponse;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import com.practice.shared_payment_backend.restservice.models.responses.friend.FriendListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.friend.FriendResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestMethodOrder(MethodOrderer.MethodName.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FriendControllerTest {
    public static final String RANDOM_NUMBER = "123456";
    private static final String FRIENDS_ENDPOINT = "/api/v1/groups/%s/friends";
    private static final String FRIEND_ENDPOINT = "/api/v1/groups/%s/friends/%s";
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

    public static String getFriendsEndpointUrl(String groupId) {
        return String.format(FRIENDS_ENDPOINT, groupId);
    }

    private static String getFriendEndpointUrl(String groupId, String friendId) {
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
                .andExpect(content().json(new ObjectMapper().createObjectNode().toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllFriends_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(get(getFriendsEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllFriends_FriendsFoundNoPayments_Ok() throws Exception {
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "test surname", new HashSet<>()));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "test surname", new HashSet<>()));
        group.setFriends(new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId())));
        groupRepository.save((FriendGroup) group);
        String result = new ApiResponse(new FriendListResponse(Arrays.asList(new FriendResponse(friend1),
                new FriendResponse(friend2)))).toString();

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
        Friend friend1 = friendRepository.save(new FriendMember("Test Friend 1", "test surname",
                new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId()))));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "test surname", new HashSet<>()));
        group.setFriends(new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId())));
        groupRepository.save((FriendGroup) group);
        String result = new ApiResponse(new FriendListResponse(Arrays.asList(new FriendResponse(friend1,
                new HashSet<>(Arrays.asList(payment1, payment2))), new FriendResponse(friend2)))).toString();

        this.mockMvc.perform(get(getFriendsEndpointUrl(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_EmptyBody_BadRequest() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullParameters_BadRequest() throws Exception {
        String body = new FriendRequest().toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullName_BadRequest() throws Exception {
        String body = new FriendRequest(null, "Test surname", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullSurname_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NullPayments_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", "test surname", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingName_BadRequest() throws Exception {
        String body = new FriendRequest(null, "Test surname", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingSurname_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingPayments_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", "test surname", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_MissingGroup_NotFound() throws Exception {
        String body = new FriendRequest("Test name", "Test surname", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_NoPayments_Created() throws Exception {
        String body = new FriendRequest("Test name", "Test surname", new HashSet<>()).toString();

        ResultActions result = this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(1, friendRepository.count());
        Friend friend = friendRepository.findAll().get(0);
        assertEquals("Test name", friend.getName());
        assertEquals("Test surname", friend.getSurname());
        assertTrue(friend.getPayments().isEmpty());

        result.andExpect(content().json(new ApiResponse(new FriendResponse(friend)).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_PaymentNotFound_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", "Test surname",
                Collections.singleton(RANDOM_NUMBER)).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getFriendsEndpointUrl(groupId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_WithPayment_Created() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        String body = new FriendRequest("Test name", "Test surname",
                Collections.singleton(payment.getId())).toString();

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

        result.andExpect(content().json(new ApiResponse(new FriendResponse(friend, Collections.singleton(payment))).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createFriend_WithPaymentAlreadyExisting_Created() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        friendRepository.save(new FriendMember("Test Friend 2", "test surname",
                Collections.singleton(payment.getId())));

        String body = new FriendRequest("Test name", "Test surname",
                Collections.singleton(payment.getId())).toString();

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
        Friend existingFriend = friendRepository.findByNameAndSurname("Test Friend 2", "test surname");
        assertNotNull(existingFriend);
        assertTrue(existingFriend.getPayments().isEmpty());

        result.andExpect(content().json(new ApiResponse(new FriendResponse(friend, Collections.singleton(payment))).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getFriend_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(get(getFriendEndpointUrl(RANDOM_NUMBER, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getFriend_FriendNotInGroup_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(get(getFriendEndpointUrl(groupId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getFriend_FriendNotFound_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(get(getFriendEndpointUrl(groupId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getFriend_FriendWithoutPayments_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "test surname", new HashSet<>()));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);

        String result = new ApiResponse(new FriendResponse(friend)).toString();

        this.mockMvc.perform(get(getFriendEndpointUrl(groupId, friend.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getFriend_FriendWithPayments_Ok() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "test surname",
                Collections.singleton(payment.getId())));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);

        String result = new ApiResponse(new FriendResponse(friend, Collections.singleton(payment))).toString();

        this.mockMvc.perform(get(getFriendEndpointUrl(groupId, friend.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteFriend_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(delete(getFriendEndpointUrl(RANDOM_NUMBER, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteFriend_FriendNotInGroup_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(delete(getFriendEndpointUrl(groupId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteFriend_FriendNotFound_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(delete(getFriendEndpointUrl(groupId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteFriend_FriendWithoutPayments_NoContent() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "test surname", new HashSet<>()));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);

        this.mockMvc.perform(delete(getFriendEndpointUrl(groupId, friend.getId())))
                .andExpect(status().isNoContent());

        assertTrue(groupRepository.findById(groupId).isPresent());
        assertTrue(groupRepository.findById(groupId).get().getFriends().isEmpty());
        assertEquals(0, friendRepository.count());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deleteFriend_FriendWithPayments_NoContent() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "Test surname",
                Collections.singleton(payment.getId())));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);

        this.mockMvc.perform(delete(getFriendEndpointUrl(groupId, friend.getId())))
                .andExpect(status().isNoContent());

        assertTrue(groupRepository.findById(groupId).isPresent());
        assertTrue(groupRepository.findById(groupId).get().getFriends().isEmpty());
        assertEquals(0, friendRepository.count());
        assertEquals(0, paymentRepository.count());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_EmptyBody_BadRequest() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullParameters_BadRequest() throws Exception {
        String body = new FriendRequest().toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullName_BadRequest() throws Exception {
        String body = new FriendRequest(null, "Test surname", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullSurname_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NullPayments_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", "Test surname", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingName_BadRequest() throws Exception {
        String body = new FriendRequest(null, "Test surname", new HashSet<>()).asJson().toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingSurname_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", null, new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingPayments_BadRequest() throws Exception {
        String body = new FriendRequest("Test name", "test surname", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_MissingGroup_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();
        String body = new FriendRequest("Test name", "Test surname", new HashSet<>()).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(RANDOM_NUMBER, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_FriendNotInGroup_NotFound() throws Exception {
        String body = new FriendRequest("Test name", "Test surname", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_FriendNotFound_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String body = new FriendRequest("Test name", "Test surname", new HashSet<>()).toString();
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_NoPayments_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "test surname", new HashSet<>()));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);
        String body = new FriendRequest("Test name", "Test surname", new HashSet<>()).toString();

        ResultActions result = this.mockMvc.perform(put(getFriendEndpointUrl(groupId, friend.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, friendRepository.count());
        friend = friendRepository.findAll().get(0);
        assertEquals("Test name", friend.getName());
        assertEquals("Test surname", friend.getSurname());
        assertTrue(friend.getPayments().isEmpty());

        result.andExpect(content().json(new ApiResponse(new FriendResponse(friend)).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_PaymentNotFound_BadRequest() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "test surname", new HashSet<>()));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);
        String body = new FriendRequest("Test name", "Test surname",
                Collections.singleton(RANDOM_NUMBER)).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getFriendEndpointUrl(groupId, friend.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_WithPayment_Ok() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend = friendRepository.save(new FriendMember("Test Friend 2", "test surname",
                Collections.singleton(payment.getId())));
        group.setFriends(Collections.singleton(friend.getId()));
        groupRepository.save((FriendGroup) group);

        String body = new FriendRequest("Test name", "Test surname", new HashSet<>()).toString();

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

        result.andExpect(content().json(new ApiResponse(new FriendResponse(friend, new HashSet<>())).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updateFriend_WithEmptyPayment_Ok() throws Exception {
        Friend friend = friendRepository.save(new FriendMember("Test Friend 1", "test surname", new HashSet<>()));
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "test surname",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Friend friend2 = friendRepository.save(new FriendMember("Test Friend 2", "test surname",
                Collections.singleton(payment.getId())));
        group.setFriends(new HashSet<>(Arrays.asList(friend.getId(), friend2.getId())));
        groupRepository.save((FriendGroup) group);

        String body = new FriendRequest("Test name", "Test surname",
                Collections.singleton(payment.getId())).toString();

        ResultActions result = this.mockMvc.perform(put(getFriendEndpointUrl(groupId, friend.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(2, friendRepository.count());
        friend2 = friendRepository.findByNameAndSurname("Test Friend 2", "test surname");
        assertNotNull(friend2);
        assertTrue(friend2.getPayments().isEmpty());
        friend = friendRepository.findByNameAndSurname("Test name", "Test surname");
        assertNotNull(friend);
        assertFalse(friend.getPayments().isEmpty());
        assertTrue(friend.getPayments().contains(payment.getId()));

        result.andExpect(content().json(new ApiResponse(new FriendResponse(friend, Collections.singleton(payment))).toString()));
    }
}
