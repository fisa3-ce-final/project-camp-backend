package com.rental.camp.rental.repository;

import com.rental.camp.rental.model.IpTracking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpTrackingRepository extends JpaRepository<IpTracking, Long> {
    Boolean existsByIp(String ip);
}
