package com.rental.camp.user.service;

import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    final UserRepository userRepository;

    @Override
    public void signIn(String _uuid) {
        UUID uuid = UUID.fromString(_uuid);
        User exsistingUser = userRepository.findByUuid(uuid);
        if (exsistingUser == null) {
            User newUser = User.builder()
                    .uuid(uuid)
                    .build();
            userRepository.save(newUser);
        } else {
            LocalDateTime date = LocalDateTime.now();

            exsistingUser.setUpdatedAt(date);
            userRepository.save(exsistingUser);
        }
    }


}
