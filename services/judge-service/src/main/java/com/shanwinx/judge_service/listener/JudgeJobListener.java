package com.shanwinx.judge_service.listener;

import com.shanwinx.judge_service.config.RabbitMQConfig;
import com.shanwinx.judge_service.dto.JudgeResultDTO;
import com.shanwinx.judge_service.dto.SubmissionJobDTO;
import com.shanwinx.judge_service.dto.Verdict;
import com.shanwinx.judge_service.exception.RetryableJudgeException;
import com.shanwinx.judge_service.service.JudgeExecutionService;
import com.shanwinx.judge_service.service.JobWorkspaceService;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JudgeJobListener {

    private final RabbitTemplate rabbitTemplate;
    private final JobWorkspaceService jobWorkspaceService;
    private final JudgeExecutionService judgeExecutionService;

    @Value("${judge.workspace.cleanup-enabled:true}")
    private boolean cleanupWorkspaceEnabled;

    @RabbitListener(queues = RabbitMQConfig.JOB_QUEUE)
    public void onJobReceived(
            SubmissionJobDTO job,
            @Header(value = AmqpHeaders.RECEIVED_ROUTING_KEY, required = false) String queueName,
            @Header(value = "x-retry-count", required = false) Integer retryCountHeader) {
        if (job == null || job.getId() == null) {
            log.warn("Received invalid judge job payload: {}", job);
            return;
        }
        int retryCount = retryCountHeader == null ? 0 : retryCountHeader;

        log.info("Received judge job submissionId={} problemId={} language={}",
                job.getId(), job.getProblemId(), job.getLanguage());

        Path workspacePath = null;
        try {
            workspacePath = jobWorkspaceService.createWorkspace(job.getId());
            log.info("Created workspace for submissionId={} at {}", job.getId(), workspacePath);

            JudgeResultDTO result = judgeExecutionService.execute(job, workspacePath);

            rabbitTemplate.convertAndSend(RabbitMQConfig.RESULT_QUEUE, result);
            log.info("Published judge result for submissionId={} to queue={}",
                    result.getSubmissionId(), RabbitMQConfig.RESULT_QUEUE);
        } catch (RetryableJudgeException ex) {
            routeRetry(job, retryCount, queueName, ex);
        } catch (RuntimeException ex) {
            log.error("Judge execution failed for submissionId={}", job.getId(), ex);

            JudgeResultDTO failed = JudgeResultDTO.builder()
                    .submissionId(job.getId())
                    .verdict(Verdict.SE)
                    .timeUsed(0)
                    .memoryUsed(0)
                    .errorDetails("Judge execution failed: " + ex.getMessage())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.RESULT_QUEUE, failed);
        } finally {
            if (!cleanupWorkspaceEnabled || workspacePath == null) {
                return;
            }
            try {
                jobWorkspaceService.cleanupWorkspace(workspacePath);
            } catch (RuntimeException cleanupEx) {
                log.warn("Cannot cleanup workspace for submissionId={} workspace={}",
                        job.getId(), workspacePath, cleanupEx);
            }
        }
    }

    private void routeRetry(SubmissionJobDTO job, int retryCount, String queueName, RetryableJudgeException ex) {
        int nextRetry = retryCount + 1;
        String targetQueue = switch (nextRetry) {
            case 1 -> RabbitMQConfig.JOB_RETRY_30S_QUEUE;
            case 2 -> RabbitMQConfig.JOB_RETRY_2M_QUEUE;
            case 3 -> RabbitMQConfig.JOB_RETRY_10M_QUEUE;
            default -> RabbitMQConfig.JOB_DLQ;
        };

        if (RabbitMQConfig.JOB_DLQ.equals(targetQueue)) {
            log.error(
                    "Retry exhausted for submissionId={} retries={} sourceQueue={} -> DLQ",
                    job.getId(),
                    retryCount,
                    queueName,
                    ex);
            rabbitTemplate.convertAndSend(targetQueue, job, message -> {
                message.getMessageProperties().setHeader("x-retry-count", retryCount);
                message.getMessageProperties().setHeader("x-last-error", ex.getMessage());
                return message;
            });
            return;
        }

        log.warn(
                "Transient failure for submissionId={} retry={} sourceQueue={} -> {}",
                job.getId(),
                nextRetry,
                queueName,
                targetQueue,
                ex);
        rabbitTemplate.convertAndSend(targetQueue, job, message -> {
            message.getMessageProperties().setHeader("x-retry-count", nextRetry);
            message.getMessageProperties().setHeader("x-last-error", ex.getMessage());
            return message;
        });
    }
}
