package com.lsi.oracle.Service.ResourceClass.Rman;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class RmanExecutor {

  public static String executeRmanScript(String rmanCommands ,String dockerContainer , Boolean isDocker) {
    StringBuilder output = new StringBuilder();

    try {
      // Define the command to run the script
      String[] command ;
      if (isDocker) {
        command = new String[]{
          "docker",
          "exec",
          "-i",
          dockerContainer,
          "/bin/bash",
          "-c",
          "echo " + escapeString(rmanCommands) + " | rman target /"
        };
      } else { // If Oracle is local
        command = new String[]{
          "sh", "-c", "echo \"" + rmanCommands + "\" | rman target /"
        };
      }

      // Execute the command
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      // Capture the output
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }

      // Wait for the process to finish
      process.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
      return "Error executing RMAN: " + e.getMessage();
    }

    return output.toString();
  }
  private static String escapeString(String input) {
    // Escape single quotes and wrap the entire string in single quotes
    return "'" + input.replace("'", "'\\''") + "'";
  }
}
