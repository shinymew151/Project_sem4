package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.TTSAudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TTSAudioRepository extends JpaRepository<TTSAudio, String> {
    List<TTSAudio> findByArticleId(String articleId);
    List<TTSAudio> findByStatus(String status);
    List<TTSAudio> findByVoiceModel(String voiceModel);
}