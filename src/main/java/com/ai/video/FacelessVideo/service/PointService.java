package com.ai.video.FacelessVideo.service;

import com.ai.video.FacelessVideo.entity.UserWallet;
import com.ai.video.FacelessVideo.entity.PointTransaction;
import com.ai.video.FacelessVideo.repository.UserWalletRepository;
import com.ai.video.FacelessVideo.repository.PointTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PointService {

    @Autowired
    private UserWalletRepository userWalletRepository;
    
    @Autowired
    private PointTransactionRepository pointTransactionRepository;
    
    @Value("${app.points.video-generation-cost:10}")
    private int videoGenerationCost;
    
    @Value("${app.points.article-generation-cost:5}")
    private int articleGenerationCost;
    
    @Value("${app.points.daily-free-points:50}")
    private int dailyFreePoints;
    
    public Optional<UserWallet> getUserWallet(String userId) {
        return userWalletRepository.findByUserId(userId);
    }
    
    public boolean hasEnoughPoints(String userId, int requiredPoints) {
        Optional<UserWallet> wallet = getUserWallet(userId);
        return wallet.map(w -> w.getBalancePoints() >= requiredPoints).orElse(false);
    }
    
    public boolean deductPoints(String userId, int points, String reason) {
        Optional<UserWallet> walletOpt = getUserWallet(userId);
        if (walletOpt.isEmpty()) {
            return false;
        }
        
        UserWallet wallet = walletOpt.get();
        if (wallet.getBalancePoints() < points) {
            return false;
        }
        
        wallet.setBalancePoints(wallet.getBalancePoints() - points);
        userWalletRepository.save(wallet);
        
        // Record transaction
        PointTransaction transaction = new PointTransaction();
        transaction.setUser(wallet.getUser());
        transaction.setChange(-points);
        transaction.setType("DEDUCT");
        transaction.setDescription(reason);
        pointTransactionRepository.save(transaction);
        
        return true;
    }
    
    public void addPoints(String userId, int points, String reason) {
        Optional<UserWallet> walletOpt = getUserWallet(userId);
        if (walletOpt.isEmpty()) {
            return;
        }
        
        UserWallet wallet = walletOpt.get();
        wallet.setBalancePoints(wallet.getBalancePoints() + points);
        userWalletRepository.save(wallet);
        
        // Record transaction
        PointTransaction transaction = new PointTransaction();
        transaction.setUser(wallet.getUser());
        transaction.setChange(points);
        transaction.setType("ADD");
        transaction.setDescription(reason);
        pointTransactionRepository.save(transaction);
    }
    
    public int getVideoGenerationCost() {
        return videoGenerationCost;
    }
    
    public int getArticleGenerationCost() {
        return articleGenerationCost;
    }
}
