package com.shawnix.submission.listener;

import com.shawnix.submission.config.RabbitMQConfig;
import com.shawnix.submission.dto.JudgeResultDTO;
import com.shawnix.submission.model.Submission;
import com.shawnix.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionListener {

    private final SubmissionRepository submissionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_SUBMISSION_PREFIX = "submission:";

    @RabbitListener(queues = RabbitMQConfig.RESULT_QUEUE)
    @Transactional
    public void onReceiveResult(JudgeResultDTO result) {
        log.info("Nhận kết quả chấm cho Submission: {}", result.getSubmissionId());
        
        Optional<Submission> submissionOpt = submissionRepository.findById(result.getSubmissionId());
        if (submissionOpt.isEmpty()) {
            log.error("Không tìm thấy Submission ID: {}", result.getSubmissionId());
            return;
        }

        Submission submission = submissionOpt.get();
        
        // 1. Idempotency Check (Production-grade)
        if (Submission.Status.FINISHED.equals(submission.getStatus())) {
            log.warn("Bài nộp ID: {} đã có kết quả rồi (FINISHED). Bỏ qua xử lý trùng lặp.", submission.getId());
            return;
        }

        // 2. Update results
        submission.setStatus(Submission.Status.FINISHED);
        submission.setVerdict(result.getVerdict());
        submission.setTimeUsed(result.getTimeUsed());
        submission.setMemoryUsed(result.getMemoryUsed());
        submission.setErrorDetails(result.getErrorDetails());
        submission.setFinishedAt(LocalDateTime.now());

        // 1. Save to DB
        submissionRepository.save(submission);

        // 2. Invalidate Cache — xóa cache PENDING cũ, polling tiếp theo sẽ đọc từ DB
        redisTemplate.delete(REDIS_SUBMISSION_PREFIX + submission.getId());
        
        log.info("Cập nhật kết quả thành công cho Submission ID: {}", submission.getId());
    }
}
