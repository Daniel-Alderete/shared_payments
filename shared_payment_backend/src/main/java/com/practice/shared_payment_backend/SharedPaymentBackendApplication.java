package com.practice.shared_payment_backend;

import com.practice.shared_payment_backend.models.friends.FriendMember;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;

@RestController
@SpringBootApplication
public class SharedPaymentBackendApplication implements CommandLineRunner {

    @Autowired
    private FriendRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(SharedPaymentBackendApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s", name);
    }

    @Override
    public void run(String... args) throws Exception {

        repository.deleteAll();
        repository.save(new FriendMember("Adrián", "Álvarez Gonzalez", new HashSet<>()));
        repository.save(new FriendMember("Alberto", "Valdueza de la Hera", new HashSet<>()));
        repository.save(new FriendMember("Marcos", "López Rodríguez", new HashSet<>()));

        for (Friend friend : repository.findAll()) {
            System.out.println(friend);
        }

        Friend friend = repository.findByNameAndSurname("Alberto", "Valdueza de la Hera");
        System.out.println(friend);

        repository.delete((FriendMember) friend);

    }
}
