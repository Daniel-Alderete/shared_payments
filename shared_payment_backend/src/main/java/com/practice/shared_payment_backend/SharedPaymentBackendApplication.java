package com.practice.shared_payment_backend;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.friends.FriendMember;
import com.practice.shared_payment_backend.models.friends.FriendPayment;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Group;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.repository.FriendRepository;
import com.practice.shared_payment_backend.repository.GroupRepository;
import com.practice.shared_payment_backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

@RestController
@SpringBootApplication
public class SharedPaymentBackendApplication implements CommandLineRunner {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public static void main(String[] args) {
        SpringApplication.run(SharedPaymentBackendApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s", name);
    }

    @Override
    public void run(String... args) throws Exception {

        friendRepository.deleteAll();
        groupRepository.deleteAll();
        paymentRepository.deleteAll();
        Payment payment1 = paymentRepository.save(new FriendPayment(23.4f, "Pago de prueba 1",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment2 = paymentRepository.save(new FriendPayment(54.6f, "Pago de prueba 2",
                Instant.now(Clock.systemUTC()).getEpochSecond()));
        Payment payment3 = paymentRepository.save(new FriendPayment(105.743f, "Pago de prueba 3",
                Instant.now(Clock.systemUTC()).getEpochSecond()));

        Friend friend1 = friendRepository.save(new FriendMember("Adrián", "Álvarez Gonzalez",
                new HashSet<>(Arrays.asList(payment1.getId(), payment2.getId(), payment3.getId()))));
        Friend friend2 = friendRepository.save(new FriendMember("Alberto", "Valdueza de la Hera", new HashSet<>()));
        Friend friend3 = friendRepository.save(new FriendMember("Marcos", "López Rodríguez", new HashSet<>()));

        Group group = groupRepository.save(new FriendGroup("Grupo de amigos", "Son mis amigos",
                new HashSet<>(Arrays.asList(friend1.getId(), friend2.getId(), friend3.getId()))));

        System.out.println(group.getId());


        for (Friend friend : friendRepository.findAll()) {
            System.out.println(friend);
        }

    }
}
