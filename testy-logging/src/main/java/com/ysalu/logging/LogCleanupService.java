package com.ysalu.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LogCleanupService {

    private final ApplicationLogService applicationLogService;
    private final String basePath;
    private final boolean enabled;
    private final long retentionDays;

    public LogCleanupService(
            ApplicationLogService applicationLogService,
            @Value("${testy.logging.base-path:${user.dir}/logs}") String basePath,
            @Value("${testy.logging.cleanup.enabled:true}") boolean enabled,
            @Value("${testy.logging.cleanup.retention-days:14}") long retentionDays
    ) {
        this.applicationLogService = applicationLogService;
        this.basePath = basePath;
        this.enabled = enabled;
        this.retentionDays = retentionDays;
    }

    @Scheduled(
            fixedDelayString = "${testy.logging.cleanup.interval-ms:86400000}",
            initialDelayString = "${testy.logging.cleanup.initial-delay-ms:300000}"
    )
    public void scheduledCleanup() {
        if (!enabled) {
            return;
        }
        cleanupExpiredLogs();
    }

    int cleanupExpiredLogs() {
        if (retentionDays < 1L) {
            applicationLogService.warn(
                    "LOG_CLEANUP_SKIPPED",
                    "Log cleanup skipped because retention-days is less than 1.",
                    "basePath", basePath,
                    "retentionDays", retentionDays
            );
            return 0;
        }

        Path logDirectory = Paths.get(basePath).normalize();
        if (!Files.exists(logDirectory) || !Files.isDirectory(logDirectory)) {
            return 0;
        }

        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        int deletedFiles = 0;
        try (Stream<Path> stream = Files.walk(logDirectory)) {
            deletedFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> shouldDelete(path, cutoff))
                    .sorted(Comparator.reverseOrder())
                    .mapToInt(this::deleteFile)
                    .sum();
        } catch (IOException exception) {
            applicationLogService.error(
                    "LOG_CLEANUP_FAILED",
                    "Failed to scan log directory for cleanup.",
                    exception,
                    "basePath", logDirectory
            );
            return 0;
        }

        deleteEmptyDirectories(logDirectory);
        if (deletedFiles > 0) {
            applicationLogService.info(
                    "LOG_CLEANUP_COMPLETED",
                    "Expired log files were deleted.",
                    "basePath", logDirectory,
                    "deletedFiles", deletedFiles,
                    "retentionDays", retentionDays
            );
        }
        return deletedFiles;
    }

    private boolean shouldDelete(Path path, Instant cutoff) {
        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            return lastModifiedTime.toInstant().isBefore(cutoff);
        } catch (IOException exception) {
            applicationLogService.error(
                    "LOG_CLEANUP_FAILED",
                    "Failed to read log file timestamp.",
                    exception,
                    "path", path
            );
            return false;
        }
    }

    private int deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
            return 1;
        } catch (IOException exception) {
            applicationLogService.error(
                    "LOG_CLEANUP_FAILED",
                    "Failed to delete expired log file.",
                    exception,
                    "path", path
            );
            return 0;
        }
    }

    private void deleteEmptyDirectories(Path logDirectory) {
        try (Stream<Path> stream = Files.walk(logDirectory)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(logDirectory))
                    .sorted(Comparator.reverseOrder())
                    .forEach(this::deleteDirectoryIfEmpty);
        } catch (IOException exception) {
            applicationLogService.error(
                    "LOG_CLEANUP_FAILED",
                    "Failed to scan directories after log cleanup.",
                    exception,
                    "basePath", logDirectory
            );
        }
    }

    private void deleteDirectoryIfEmpty(Path directory) {
        try (Stream<Path> children = Files.list(directory)) {
            if (!children.findAny().isPresent()) {
                Files.deleteIfExists(directory);
            }
        } catch (IOException exception) {
            applicationLogService.error(
                    "LOG_CLEANUP_FAILED",
                    "Failed to delete empty log directory.",
                    exception,
                    "path", directory
            );
        }
    }
}
