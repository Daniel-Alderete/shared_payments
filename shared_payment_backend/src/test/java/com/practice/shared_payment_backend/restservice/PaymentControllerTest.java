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
import com.practice.shared_payment_backend.restservice.models.requests.PaymentRequest;
import com.practice.shared_payment_backend.restservice.models.responses.ApiErrorResponse;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import com.practice.shared_payment_backend.restservice.models.responses.payment.PaymentListResponse;
import com.practice.shared_payment_backend.restservice.models.responses.payment.PaymentResponse;
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

import static com.practice.shared_payment_backend.restservice.FriendControllerTest.RANDOM_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestMethodOrder(MethodOrderer.MethodName.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {
    private static final String PAYMENTS_ENDPOINT = "/api/v1/groups/%s/friends/%s/payments";
    private static final String PAYMENT_ENDPOINT = "/api/v1/groups/%s/friends/%s/payments/%s";
    private static final long NOW = Instant.now(Clock.systemUTC()).getEpochSecond();
    private static Group group;
    private static String groupId;
    private static Friend friend;
    private static String friendId;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private MockMvc mockMvc;

    private String getPaymentsEndpointUrl(String groupId, String friendId) {
        return String.format(PAYMENTS_ENDPOINT, groupId, friendId);
    }

    private String getPaymentEndpointUrl(String groupId, String friendId, String paymentId) {
        return String.format(PAYMENT_ENDPOINT, groupId, friendId, paymentId);
    }

    @BeforeEach
    public void setUp() {
        friend = friendRepository.save(new FriendMember("Test Friend 1", "test surname", new HashSet<>()));
        friendId = friend.getId();
        group = groupRepository.save(new FriendGroup("Test Group", "Test description",
                Collections.singleton(friendId)));
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
    public void getAllPayments_Empty_Ok() throws Exception {
        this.mockMvc.perform(get(getPaymentsEndpointUrl(groupId, friendId)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().createObjectNode().toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllPayments_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();

        this.mockMvc.perform(get(getPaymentsEndpointUrl(RANDOM_NUMBER, friendId)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllPayments_FriendNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(get(getPaymentsEndpointUrl(groupId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllPayments_FriendNotFoundInDB_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(get(getPaymentsEndpointUrl(groupId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllPayments_FriendsFoundWithPayments_Ok() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment2 = paymentRepository.save(new FriendPayment(196.7f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        friend.setPayments(new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId())));
        friendRepository.save((FriendMember) friend);

        String result = new ApiResponse(new PaymentListResponse(Arrays.asList(new PaymentResponse(payment1),
                new PaymentResponse(payment2)))).toString();

        this.mockMvc.perform(get(getPaymentsEndpointUrl(groupId, friendId)))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_EmptyBody_BadRequest() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_NullParameters_BadRequest() throws Exception {
        String body = new PaymentRequest().toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_NullName_BadRequest() throws Exception {
        String body = new PaymentRequest(null, "Test Description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_NullSurname_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, null, NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_NullPayments_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, "test description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_MissingName_BadRequest() throws Exception {
        String body = new PaymentRequest(null, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_MissingSurname_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, null, NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_MissingPayments_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, "test description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_MissingGroup_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(RANDOM_NUMBER, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_MissingFriend_NotFound() throws Exception {
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_FriendNotInGroup_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_FriendWithoutPayments_Created() throws Exception {
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();

        ResultActions result = this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(1, paymentRepository.count());
        Payment payment = paymentRepository.findAll().get(0);
        assertEquals(54.5f, payment.getAmount());
        assertEquals("Test description", payment.getDescription());
        assertEquals(NOW, payment.getDate());
        friend = friendRepository.findAll().get(0);
        assertEquals(1, friend.getPayments().size());

        result.andExpect(content().json(new ApiResponse(new PaymentResponse(payment)).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void createPayment_FriendWithPayments_Created() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.5f, "Test description", NOW));
        friend.setPayments(Collections.singleton(payment1.getId()));
        friendRepository.save((FriendMember) friend);

        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();

        ResultActions result = this.mockMvc.perform(post(getPaymentsEndpointUrl(groupId, friendId))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(2, paymentRepository.count());
        Payment payment = paymentRepository.findAll().get(1);
        assertEquals(54.5f, payment.getAmount());
        assertEquals("Test description", payment.getDescription());
        assertEquals(NOW, payment.getDate());
        friend = friendRepository.findAll().get(0);
        assertEquals(2, friend.getPayments().size());

        result.andExpect(content().json(new ApiResponse(new PaymentResponse(payment)).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getPayment_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();
        this.mockMvc.perform(get(getPaymentEndpointUrl(RANDOM_NUMBER, friendId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getPayment_FriendNotInGroup_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(get(getPaymentEndpointUrl(groupId, RANDOM_NUMBER, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getPayment_FriendNotFound_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(get(getPaymentEndpointUrl(groupId, RANDOM_NUMBER, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getPayment_FriendWithoutPayments_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(103, "Payment not found")).toString();

        this.mockMvc.perform(get(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getPayment_FriendWithPayments_Ok() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        friend.setPayments(Collections.singleton(payment.getId()));
        friendRepository.save((FriendMember) friend);

        String result = new ApiResponse(new PaymentResponse(payment)).toString();

        this.mockMvc.perform(get(getPaymentEndpointUrl(groupId, friendId, payment.getId())))
                .andExpect(status().isOk())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deletePayment_GroupNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();
        this.mockMvc.perform(delete(getPaymentEndpointUrl(RANDOM_NUMBER, RANDOM_NUMBER, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deletePayment_FriendNotInGroup_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(delete(getPaymentEndpointUrl(groupId, RANDOM_NUMBER, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deletePayment_FriendNotFound_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(delete(getPaymentEndpointUrl(groupId, RANDOM_NUMBER, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deletePayment_PaymentNotFound_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(103, "Payment not found")).toString();

        this.mockMvc.perform(delete(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void deletePayment_FriendWithPayments_NoContent() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        friend.setPayments(Collections.singleton(payment.getId()));
        friendRepository.save((FriendMember) friend);

        this.mockMvc.perform(delete(getPaymentEndpointUrl(groupId, friendId, payment.getId())))
                .andExpect(status().isNoContent());

        assertEquals(1, groupRepository.count());
        assertEquals(1, friendRepository.count());
        assertTrue(friendRepository.findById(friendId).isPresent());
        assertTrue(friendRepository.findById(friendId).get().getPayments().isEmpty());
        assertEquals(0, paymentRepository.count());
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_EmptyBody_BadRequest() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_NullParameters_BadRequest() throws Exception {
        String body = new PaymentRequest().toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_NullName_BadRequest() throws Exception {
        String body = new PaymentRequest(null, "Test Description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_NullSurname_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, null, NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_NullPayments_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, "test description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_MissingName_BadRequest() throws Exception {
        String body = new PaymentRequest(null, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_MissingSurname_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, null, NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_MissingPayments_BadRequest() throws Exception {
        String body = new PaymentRequest(54.5f, "test description", null).toString();
        String result = new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the " +
                "request")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_MissingGroup_NotFound() throws Exception {
        String result = new ApiResponse(new ApiErrorResponse(101, "Group not found")).toString();
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(RANDOM_NUMBER, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_FriendNotInGroup_NotFound() throws Exception {
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, RANDOM_NUMBER, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_FriendNotFound_NotFound() throws Exception {
        group.setFriends(Collections.singleton(RANDOM_NUMBER));
        groupRepository.save((FriendGroup) group);
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(102, "Friend not found")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, RANDOM_NUMBER, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_MissingPayment_NotFound() throws Exception {
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(103, "Payment not found")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_PaymentNotFound_NotFound() throws Exception {
        friend.setPayments(Collections.singleton(RANDOM_NUMBER));
        friendRepository.save((FriendMember) friend);
        String body = new PaymentRequest(54.5f, "Test description", NOW).toString();
        String result = new ApiResponse(new ApiErrorResponse(103, "Payment not found")).toString();

        this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, RANDOM_NUMBER))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(result));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_FriendWithoutPayments_Created() throws Exception {
        Payment payment = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        friend.setPayments(Collections.singleton(payment.getId()));
        friendRepository.save((FriendMember) friend);

        String body = new PaymentRequest(8.56f, "Test description 1", NOW).toString();

        ResultActions result = this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, payment.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, paymentRepository.count());
        payment = paymentRepository.findAll().get(0);
        assertEquals(8.56f, payment.getAmount());
        assertEquals("Test description 1", payment.getDescription());
        assertEquals(NOW, payment.getDate());
        friend = friendRepository.findAll().get(0);
        assertEquals(1, friend.getPayments().size());

        result.andExpect(content().json(new ApiResponse(new PaymentResponse(payment)).toString()));
    }

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void updatePayment_FriendWithPayments_Created() throws Exception {
        Payment payment1 = paymentRepository.save(new FriendPayment(100.5f, "Test description", NOW));
        Payment payment2 = paymentRepository.save(new FriendPayment(54.5f, "Test description",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        friend.setPayments(new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId())));
        friendRepository.save((FriendMember) friend);

        String body = new PaymentRequest(345.6f, "Test description 3", NOW).toString();

        ResultActions result = this.mockMvc.perform(put(getPaymentEndpointUrl(groupId, friendId, payment1.getId()))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(2, paymentRepository.count());
        payment1 = paymentRepository.findAll().get(0);
        assertEquals(345.6f, payment1.getAmount());
        assertEquals("Test description 3", payment1.getDescription());
        assertEquals(NOW, payment1.getDate());
        friend = friendRepository.findAll().get(0);
        assertEquals(2, friend.getPayments().size());

        result.andExpect(content().json(new ApiResponse(new PaymentResponse(payment1)).toString()));
    }
}
