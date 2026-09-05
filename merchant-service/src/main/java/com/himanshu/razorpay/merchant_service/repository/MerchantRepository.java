package com.himanshu.razorpay.merchant_service.repository;


import com.himanshu.razorpay.common_library.enums.MerchantStatus;
import com.himanshu.razorpay.merchant_service.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

//@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    boolean existsByEmail(String email);

    List<Merchant> findByStatus(MerchantStatus merchantStatus);
}
