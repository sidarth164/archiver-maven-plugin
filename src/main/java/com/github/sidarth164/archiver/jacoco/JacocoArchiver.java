package com.github.sidarth164.archiver.jacoco;

import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JacocoArchiver {
  private final Path localArchiveDir;
  private final Path jacocoDir;
  private final Log log;

  public JacocoArchiver(Path localArchiveDir, Path buildTargetDir, Log log) {
    this.localArchiveDir = localArchiveDir.resolve("jacoco");
    this.jacocoDir = buildTargetDir.resolve("site/jacoco");
    this.log = log;
  }

  /**
   * Core logic to archive the jacoco reports
   */
  public void archiveJacocoReportsLocally() {
    try {
      Files.walk(jacocoDir)
           .forEach(jacocoPath -> {
             Path archivePath = localArchiveDir.resolve(jacocoDir.relativize(jacocoPath));
             log.debug(String.format("Copying %s to %s", jacocoPath, archivePath));
             try {
               Files.createDirectories(archivePath.getParent());
               Files.copy(jacocoPath, archivePath, StandardCopyOption.REPLACE_EXISTING);
             }
             catch (IOException e) {
               log.warn(String.format("Error copying file %s to %s\n%s: %s", jacocoPath, archivePath,
                                      e.getClass().getName(), e.getMessage()));
             }
           });
    }
    catch (IOException e) {
      log.warn(String.format("Error in accessing directory %s\n%s: %s", jacocoDir, e.getClass().getName(),
                             e.getMessage()));
    }
  }
}
