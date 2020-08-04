package com.github.sidarth164.archiver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

/**
 *
 */
@Mojo(name = "version", defaultPhase = LifecyclePhase.INITIALIZE)
public class GitVersionMojo extends AbstractMojo {

  @Parameter(property = "git.command", defaultValue = "git rev-parse --short HEAD")
  private String command;

  @Parameter(property = "project", readonly = true)
  private MavenProject project;

  public void execute() throws MojoExecutionException, MojoFailureException {
    String version = gitVersion(command);
    project.getProperties().put("gitVersion", version);
    getLog().info("Git hash: " + version);
  }

  public String gitVersion(String command) throws MojoExecutionException {
    try {
      StringBuilder builder = new StringBuilder();

      Process process = Runtime.getRuntime().exec(command);
      Executors.newSingleThreadExecutor()
               .submit(() -> new BufferedReader(new InputStreamReader(process.getInputStream()))
                   .lines()
                   .forEach(builder::append));
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new MojoExecutionException("Execution of command '" + command + "' failed with exit code: " + exitCode);
      }

      return builder.toString();
    }
    catch (IOException | InterruptedException e) {
      throw new MojoExecutionException("Execution of command '" + command + "' failed", e);
    }
  }
}
