package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, String> {
    List<Referral> findByReferrerId(String referrerId);
    Optional<Referral> findByReferralCode(String referralCode);
    Optional<Referral> findByReferredUserId(String referredUserId);
}