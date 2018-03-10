/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.maven.test;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Forks and controls a external JVM in which tests are actually run.
 *
 * <p>This allows us to handle and report failures on tests which timeout or manage to thoroughly 
 * crash the JVM.</p>
 */
public class ForkedTestController {

  public static final boolean DEBUG_FORKING = false;
  private final Log log;

  private long timeoutMillis = TimeUnit.MINUTES.toMillis(3);

  private Map<String, String> environmentVariables = new HashMap<>();

  private Process process;
  private DataOutputStream processChannel;
  private File testReportsDirectory;
  private TestReporter reporter;
  private String argLine;

  public ForkedTestController(Log log) {
    this.log = log;
  }

  public void setNamespaceUnderTest(String namespace) {
    this.environmentVariables.put(TestExecutor.NAMESPACE_UNDER_TEST, namespace);
  }

  /**
   * @param outputLimit the limit to place on test output, in bytes
   */
  public void setOutputLimit(int outputLimit) {
    this.environmentVariables.put(TestExecutor.OUTPUT_LIMIT, Integer.toString(outputLimit));
  }
  
  public void setClassPath(String classPath) {
    this.environmentVariables.put("CLASSPATH", classPath);
  }

  public void setTestReportDirectory(File testReportDirectory) {
    this.testReportsDirectory = testReportDirectory;
    this.environmentVariables.put(TestExecutor.TEST_REPORT_DIR, testReportDirectory.getAbsolutePath());
  }

  public void setDefaultPackages(List<String> packages) {
    if(packages != null) {
      this.environmentVariables.put(TestExecutor.DEFAULT_PACKAGES, Joiner.on(",").join(packages));
    }
  }

  public void setTimeout(long timeout, TimeUnit timeUnit) {
    timeoutMillis = timeUnit.toMillis(timeout);
  }

  public void executeTests(File testSourceDirectory) throws MojoExecutionException {

    log.info("Running tests in " + testSourceDirectory.getAbsolutePath());

    if(testSourceDirectory.isDirectory()) {
      File[] testFiles = testSourceDirectory.listFiles();
      if(testFiles != null) {
        for (File testFile : testFiles) {
          String testFileName = testFile.getName().toUpperCase();
          if(testFileName.endsWith(".R") || testFileName.endsWith(".RD")) {
            executeTest(testFile);
          }
        }
      }
    }
  }

  public void executeTest(File testFile) throws MojoExecutionException {

    if(reporter == null) {
      reporter = new TestReporter(testReportsDirectory);
      reporter.start();
    }

    if(process == null) {
      startFork();
    }

    reporter.startFile(testFile);

    try {
      // Send the command to run the test
      processChannel.writeUTF(testFile.getAbsolutePath());
      processChannel.flush();
    } catch (IOException e) {
      log.error("Failed to send command to fork", e);
    }

    // Listen for test results

    ResultListener listener = new ResultListener(process.getInputStream());
    Thread listeningThread = new Thread(listener);
    listeningThread.start();
    try {
      listeningThread.join(timeoutMillis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      reporter.testCaseInterrupted();
      reporter.fileComplete();
      return;
    }
    if(listeningThread.isAlive()) {
      // if we didn't succeed in joining, then it means we have timed out.
      reporter.timeout(timeoutMillis);
      destroyFork();
    }
    reporter.fileComplete();
  }

  private void startFork() throws MojoExecutionException {

    try {

      List<String> command = new ArrayList<>();
      command.add("java");
      if(argLine != null) {
        command.add(argLine);
      }
      command.add(TestExecutor.class.getName());

      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command(command);
      processBuilder.environment().putAll(environmentVariables);
      processBuilder.redirectErrorStream(true);
      process = processBuilder.start();
      processChannel = new DataOutputStream(process.getOutputStream());
    } catch (Exception e) {
      throw new MojoExecutionException("Could not start forked JVM", e);
    }
  }

  public void shutdown() {
    if(process != null) {
      try {
        processChannel.close();
        process.destroy();
      } catch (Exception e) {
        log.error("Error shutting down fork", e);
      } finally {
        process = null;
        processChannel = null;
      }
    }
  }

  private void destroyFork() {
    if(process != null) {
      try {
        process.destroy();
      } catch (Exception e) {
        log.error("Error destroying fork", e);
      }
      process = null;
      processChannel = null;
    }
  }

  public boolean allTestsSucceeded() {
    return reporter == null || reporter.allTestsSucceeded();
  }

  public void setArgLine(String argLine) {
    this.argLine = argLine;
  }

  private class ResultListener implements Runnable {

    private final BufferedReader reader;

    private boolean processRunning = true;

    public ResultListener(InputStream in) {
      this.reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
    }

    @Override
    public void run() {
      while(processRunning) {
        String line;
        try {
          line = reader.readLine();
        } catch (IOException e) {
          onErrorReadingFork(e);
          return;
        }
        if(line == null) {
          onEndOfInput();
          return;
        }

        onMessageReceived(line);
      }
    }

    private void onErrorReadingFork(IOException e) {
      // Not sure under what situation this could happen but consider it a test failure
      log.error("Error reading from forked test executor: " + e.getMessage(), e);
      reporter.testCaseFailed();
      destroyFork();
    }

    private void onEndOfInput() {
      // Process exited!!
      reporter.testCaseFailed();
      try {
        if(process != null) {
          log.error("Forked JVM exited with code " + process.waitFor());
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Interrupted while waiting for process to exit.");
      }
      destroyFork();
    }


    private void onMessageReceived(String line) {
      if(DEBUG_FORKING) {
        log.debug("[CHANNEL] " + line);
      }
      if (line.startsWith(TestExecutor.MESSAGE_PREFIX)) {
        String[] message = line.substring(TestExecutor.MESSAGE_PREFIX.length()).split(TestExecutor.MESSAGE_PREFIX);
        switch (message[0]) {
          case TestExecutor.START_MESSAGE:
            reporter.testCaseStarting(message[1]);
            break;

          case TestExecutor.FAIL_MESSAGE:
            reporter.testCaseFailed();
            break;

          case TestExecutor.PASS_MESSAGE:
            reporter.testCaseSucceeded();
            break;

          case TestExecutor.DONE_MESSAGE:
            processRunning = false;
            break;

          default:
            log.error("Unknown message: " + message[0]);
        }
      }
    }
  }
}
