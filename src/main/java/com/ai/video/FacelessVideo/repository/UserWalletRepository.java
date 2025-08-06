package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, String> {
    Optional<UserWallet> findByUserId(String userId);
}