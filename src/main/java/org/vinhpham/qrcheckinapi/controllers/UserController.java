package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vinhpham.qrcheckinapi.dtos.Failure;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.entities.User;
import org.vinhpham.qrcheckinapi.services.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    final private UserService userService;
    final private MessageSource messageSource;

    @GetMapping
    public ResponseEntity<?> getUsers() {
        Optional<List<User>> userListOptional = userService.getAllUser();
        return Success.ok(userListOptional.orElse(Collections.emptyList()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);

        if (user.isEmpty()) {
            return Failure.response(messageSource, "error.username.unexists", HttpStatus.NOT_FOUND, username);
        } else {
            return Success.ok(user.get());
        }
    }

}
