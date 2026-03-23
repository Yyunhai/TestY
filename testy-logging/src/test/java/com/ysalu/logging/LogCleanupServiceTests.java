package com.ysalu.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogCleanupServiceTests {

    @Test
    void cleanupDeletesExpiredFilesAndKeepsRecentFiles() throws IOException {
        Path directory = Files.createTempDirectory("testy-log-cleanup");
        Path expiredFile = Files.createFile(directory.resolve("expired.log"));
        Path recentFile = Files.createFile(directory.resolve("recent.log"));
        Path archiveDirectory = Files.createDirectories(directory.resolve("archive"));
        Path expiredArchive = Files.createFile(archiveDirectory.resolve("expired.gz"));

        Files.setLastModifiedTime(expiredFile, FileTime.from(Instant.now().minusSeconds(20L * 24L * 60L * 60L)));
        Files.setLastModifiedTime(expiredArchive, FileTime.from(Instant.now().minusSeconds(30L * 24L * 60L * 60L)));
        Files.setLastModifiedTime(recentFile, FileTime.from(Instant.now()));

        LogCleanupService service = new LogCleanupService(
                new ApplicationLogService(),
                directory.toString(),
                true,
                14L
        );

        int deletedFiles = service.cleanupExpiredLogs();

        assertEquals(2, deletedFiles);
        assertFalse(Files.exists(expiredFile));
        assertFalse(Files.exists(expiredArchive));
        assertTrue(Files.exists(recentFile));
        assertFalse(Files.exists(archiveDirectory));
    }
}
