package com.rental.camp.coupon.service;

import com.rental.camp.coupon.repository.CouponRepository;
import com.rental.camp.coupon.repository.UserCouponRepository;
import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class CouponServiceConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponRepository userCouponRepository;


    void setUp() {
        for (int i = 0; i < 120; i++) {
            User user = User.builder()
                    .uuid(UUID.randomUUID())
                    .username("User" + i)
                    .email("user" + i + "@example.com")
                    .phone("010-1234-567" + i % 10)
                    .address("Some address")
                    .nickname("Nickname" + i)
                    .imageUrl(null)
                    .provider("local")
                    .isDeleted(false)
                    .role("USER")
                    .build();
            userRepository.save(user);
        }
    }
}
