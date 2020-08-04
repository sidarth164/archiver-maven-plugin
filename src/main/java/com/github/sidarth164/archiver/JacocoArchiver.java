package com.github.sidarth164.archiver;

import org.apache.maven.model.interpolation.MavenBuildTimestamp;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 *
 */
@SuppressWarnings("unused")
@Mojo(name = "jarchive", defaultPhase = LifecyclePhase.INITIALIZE)
public class JacocoArchiver extends AbstractMojo {

  private static final String homeDir = System.getProperty("user.home");

  @Parameter(property = "local.dir", defaultValue = "archiver/reports/jacoco")
  private String localDir;

  @Parameter(property = "project", readonly = true)
  private MavenProject project;

  @Parameter(property = "maven.build.timestamp", readonly = true)
  private MavenBuildTimestamp timestamp;

  public void execute() {
    archiveJacocoReportsLocally();
  }

  private void archiveJacocoReportsLocally() {
    Path archiveDir = Paths.get(getLocalArchiveDir());
    Path jacocoDir = Paths.get(getJacocoDir());

    try {
      Files.walk(jacocoDir)
           .forEach(jacocoPath -> {
             Path archivePath = archiveDir.resolve(jacocoDir.relativize(jacocoPath));
             getLog().debug(String.format("Copying %s to %s", jacocoPath, archivePath));
             try {
               Files.createDirectories(archivePath.getParent());
               Files.copy(jacocoPath, archivePath, StandardCopyOption.REPLACE_EXISTING);
             }
             catch (IOException e) {
               getLog().warn(String.format("Error copying file %s to %s\n%s: %s", jacocoPath, archivePath,
                                           e.getClass().getName(), e.getMessage()));
             }
           });
    }
    catch (IOException e) {
      getLog().warn(String.format("Error in accessing directory %s\n%s: %s", jacocoDir, e.getClass().getName(),
                                  e.getMessage()));
    }
  }

  private String getJacocoDir() {
    String targetDir = project.getBuild().getDirectory();
    return targetDir + "/site/jacoco";
  }

  private String getLocalArchiveDir() {
    String groupId = project.getGroupId();
    String artifactId = project.getArtifactId();
    String version = project.getVersion();
    String buildTimestamp = timestamp.formattedTimestamp();

    return homeDir + "/" + localDir + "/" + groupId + "/" + artifactId + "/" + version + "/" + buildTimestamp;
  }
}
