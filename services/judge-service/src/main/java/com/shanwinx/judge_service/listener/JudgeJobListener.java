package com.shanwinx.judge_service.listener;

import com.shanwinx.judge_service.config.RabbitMQConfig;
import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JudgeJobListener {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.JOB_QUEUE)
    public void onJobReceived(SubmissionJobDTO job) {
        if (job == null || job.getId() == null) {
            log.warn("Received invalid judge job payload: {}", job);
            return;
        }

        log.info("Received judge job submissionId={} problemId={} language={}",
                job.getId(), job.getProblemId(), job.getLanguage());

        JudgeResultDTO result = JudgeResultDTO.builder()
            .submissionId(job.getId())
            .verdict(Verdict.AC)
            .timeUsed(10)
            .memoryUsed(1024)
            .errorDetails(null)
            .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.RESULT_QUEUE, result);
        log.info("Published judge result for submissionId={} to queue={}",
                result.getSubmissionId(), RabbitMQConfig.RESULT_QUEUE);
    }
}
