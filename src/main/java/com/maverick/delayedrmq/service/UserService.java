package com.maverick.delayedrmq.service;

import com.maverick.delayedrmq.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private static List<UserDTO> users = new ArrayList<>();

    public UserDTO getUserByUserId(Integer userId) {
        return users.stream().filter(userDTO -> userId.equals(userDTO.getUserId())).findAny().orElse(null);
    }

    public void saveUser(UserDTO userDTO) {
        userDTO.setExternalUserId(UUID.randomUUID().toString());
        userDTO.setUserName("TEST_NAME");
        users.add(userDTO);
    }

}
