package com.lsi.oracle.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupDetails {
  private String bsKey;
  private String type;
  private String level;
  private String status;
  private String deviceType;
  private LocalDateTime completionTime;
  private String pieces;
  private String copies;
  private boolean compressed;
  private String tag;

  public static List<BackupDetails> parseFromRmanOutput(String rmanOutput) {
    List<BackupDetails> backups = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new StringReader(rmanOutput))) {
      String line;
      boolean startParsing = false;

      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Skip until we find the header line
        if (line.startsWith("Key")) {
          startParsing = true;
          continue;
        }

        // Skip the separator line
        if (line.startsWith("---")) {
          continue;
        }

        // Parse backup entries
        if (startParsing && !line.isEmpty() && Character.isDigit(line.charAt(0))) {
          String[] parts = line.trim().split("\\s+");
          if (parts.length >= 9) {
            BackupDetails backup = new BackupDetails();
            backup.setBsKey(parts[0]);
            backup.setType(parts[1]);
            backup.setLevel(parts[2]);
            backup.setStatus(parts[3]);
            backup.setDeviceType(parts[4]);
            backup.setCompletionTime(parseDateTime(parts[5]));
            backup.setPieces(parts[6]);
            backup.setCopies(parts[7]);
            backup.setCompressed("YES".equalsIgnoreCase(parts[8]));

            // Handle tag which might contain spaces
            if (parts.length > 9) {
              StringBuilder tag = new StringBuilder(parts[9]);
              for (int i = 10; i < parts.length; i++) {
                tag.append(" ").append(parts[i]);
              }
              backup.setTag(tag.toString());
            }

            backups.add(backup);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error parsing RMAN output", e);
    }
    return backups;
  }

  private static LocalDateTime parseDateTime(String date) {
    if (date == null || date.trim().isEmpty()) return null;
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy");
      return LocalDateTime.of(
        LocalDateTime.parse(date.trim(), formatter).toLocalDate(),
        LocalDateTime.now().toLocalTime()
      );
    } catch (Exception e) {
      return null;
    }
  }
}
