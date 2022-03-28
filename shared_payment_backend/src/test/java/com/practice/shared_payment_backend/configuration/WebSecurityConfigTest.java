package com.practice.shared_payment_backend.configuration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static com.practice.shared_payment_backend.restservice.FriendControllerTest.RANDOM_NUMBER;
import static com.practice.shared_payment_backend.restservice.FriendControllerTest.getFriendsEndpointUrl;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.MethodName.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WebSecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test-client", password = "test-password", roles = "USER")
    public void getAllFriends_ValidCredentials_Ok() throws Exception {
        this.mockMvc.perform(get(getFriendsEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllFriends_NonValidCredentials_Unauthorized() throws Exception {
        this.mockMvc.perform(get(getFriendsEndpointUrl(RANDOM_NUMBER)))
                .andExpect(status().isUnauthorized());
    }
}