package com.github.sidarth164.archiver.mojo;

import com.github.sidarth164.archiver.jacoco.JacocoArchiver;
import org.apache.maven.model.interpolation.MavenBuildTimestamp;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Archives the jacoco build reports (inside /target/site/jacoco) into a local repository
 */
@SuppressWarnings("unused")
@Mojo(name = "archive", defaultPhase = LifecyclePhase.INSTALL)
public class Archiver extends AbstractMojo {

  private static final String homeDir = System.getProperty("user.home");

  /**
   * Comma separated archive goals. For eg: 'jacoco,surefire'
   */
  @Parameter(property = "archiveGoals", defaultValue = "jacoco")
  private String archiveGoals;

  /**
   * The local directory where the archived reports reside
   */
  @Parameter(property = "local.dir", defaultValue = "archive/reports")
  private String localDir;

  /**
   * Maven Project using this plugin
   */
  @Parameter(property = "project", readonly = true)
  private MavenProject project;

  /**
   * Maven Build timestamp
   */
  @Parameter(property = "maven.build.timestamp", readonly = true)
  private MavenBuildTimestamp timestamp;

  /**
   * The method called when the plugin is executed
   */
  @Override
  public void execute() throws MojoExecutionException {
    Set<String> goals = new HashSet<>(Arrays.asList(archiveGoals.split(",")));
    StringBuilder undefinedGoals = new StringBuilder();
    Path localArchiveDir = getLocalArchiveDir();
    Path buildTargetDir = getBuildTargetDir();
    for(String goal: goals) {
      if ("jacoco".equals(goal)) {
        JacocoArchiver jacocoArchiver = new JacocoArchiver(localArchiveDir, buildTargetDir, getLog());
        jacocoArchiver.archiveJacocoReportsLocally();
      }
      else {
        // The archive goal provided is not defined
        if (undefinedGoals.length() == 0) {
          undefinedGoals.append(goal);
        }
        else {
          undefinedGoals.append(",").append(goal);
        }
      }
    }
    if(undefinedGoals.length() > 0) {
      throw new MojoExecutionException(String.format("Archive goals '%s' is/are not defined", undefinedGoals));
    }
  }

  /**
   * Formulate the path of the target build
   *
   * @return {@link Path}
   */
  private Path getBuildTargetDir() {
    return Paths.get(project.getBuild().getDirectory());
  }

  /**
   * Formulate the archive location path
   *
   * @return {@link Path}
   */
  private Path getLocalArchiveDir() {
    String groupId = project.getGroupId();
    String artifactId = project.getArtifactId();
    String version = project.getVersion();
    String buildTimestamp = timestamp.formattedTimestamp();
    String archiveDir = homeDir + "/" + localDir + "/" + groupId + "/" + artifactId + "/" + version + "/" + buildTimestamp;
    return Paths.get(archiveDir);
  }
}
