package com.maverick.delayedrmq.controller;

import com.maverick.delayedrmq.dto.UserDTO;
import com.maverick.delayedrmq.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@AllArgsConstructor
@Controller
@Slf4j
public class UsersController {

    private UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserByUserId(@PathVariable Integer userId) {
        log.info("Getting user by userId={}", userId);
        UserDTO userDTO = userService.getUserByUserId(userId);
        return ResponseEntity.ok().body(userDTO);
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<UserDTO> saveUserByUserId(@PathVariable Integer userId) {
        log.info("Saving user with userId={}", userId);
        userService.saveUser(UserDTO.builder().userId(userId).build());
        return ResponseEntity.ok().build();
    }
}
