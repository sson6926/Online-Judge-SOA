package com.shawnix.submission.service;

import com.shawnix.submission.config.RabbitMQConfig;
import com.shawnix.submission.dto.SubmitRequest;
import com.shawnix.submission.model.Submission;
import com.shawnix.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_SUBMISSION_PREFIX = "submission:";

    @Override
    @Transactional
    public Submission submit(SubmitRequest request) {
        // Map DTO → Entity, chỉ lấy field client được phép gửi
        Submission submission = Submission.builder()
                .problemId(request.getProblemId())
                .userId(request.getUserId())
                .code(request.getCode())
                .language(request.getLanguage())
                .status(Submission.Status.PENDING)
                .build();
        
        // 2. Save to DB
        Submission saved = submissionRepository.save(submission);
        
        // 3. Save to Redis for polling (10 minutes TTL)
        redisTemplate.opsForValue().set(REDIS_SUBMISSION_PREFIX + saved.getId(), saved, 10, TimeUnit.MINUTES);
        
        // 4. Send to MQ for the Judge Worker
        rabbitTemplate.convertAndSend(RabbitMQConfig.JOB_QUEUE, saved);
        
        log.info("Nộp bài thành công (ID: {}), gửi sang Queue.", saved.getId());
        return saved;
    }

    @Override
    public Submission getSubmissionById(UUID id) {
        // 1. Check Redis first
        Submission cached = (Submission) redisTemplate.opsForValue().get(REDIS_SUBMISSION_PREFIX + id);
        if (cached != null) {
            return cached;
        }
        
        // 2. Fallback to DB
        Submission submission = submissionRepository.findById(id).orElse(null);
        
        // 3. Write back to Cache (Cache-Aside pattern)
        if (submission != null) {
            redisTemplate.opsForValue().set(REDIS_SUBMISSION_PREFIX + id, submission, 10, java.util.concurrent.TimeUnit.MINUTES);
        }
        
        return submission;
    }

    @Override
    public List<Submission> getAll() {
        return submissionRepository.findAllByOrderBySubmittedAtDesc();
    }

    @Override
    public List<Submission> getByProblemId(UUID problemId) {
        return submissionRepository.findByProblemIdOrderBySubmittedAtDesc(problemId);
    }
}
