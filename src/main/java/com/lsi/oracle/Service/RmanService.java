package com.lsi.oracle.Service;

import com.lsi.oracle.Service.ResourceClass.Rman.RmanExecutor;
import com.lsi.oracle.model.BackupDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class RmanService {
  @Value("${project.env.rman.docker-container}")
  private  String dockerContainer ;

  @Value("${project.env.rman.is-docker}")
  private  Boolean isDocker ;

  public String test(){
    return "docker container "+dockerContainer+" is docker "+isDocker;
  }

  public String full_backup(){
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String command = "RUN {\n" +
      " ALLOCATE CHANNEL ch1 DEVICE TYPE DISK;\n" +
      " BACKUP SPFILE TAG='SPFILE_" + timestamp + "';\n" +
      " BACKUP CURRENT CONTROLFILE TAG='CTRL_" + timestamp + "';\n" +
      " BACKUP DATABASE PLUS ARCHIVELOG DELETE INPUT TAG='FULL_" + timestamp + "';\n" +
      " RELEASE CHANNEL ch1;\n" +
      "}";

    return     RmanExecutor.executeRmanScript(command,dockerContainer,isDocker);
  }

  public String incremental_backup() {
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String command = "RUN {\n" +
      " ALLOCATE CHANNEL ch1 DEVICE TYPE DISK;\n" +
      " BACKUP INCREMENTAL LEVEL 1 DATABASE TAG='INCR_" + timestamp + "';\n" +
      " BACKUP ARCHIVELOG ALL TAG='ARCHIVELOG_" + timestamp + "';\n" +
      " RELEASE CHANNEL ch1;\n" +
      "}";

    return RmanExecutor.executeRmanScript(command, dockerContainer, isDocker);
  }

  public String restore_recover(){

    String command = """
      RUN {
             # First shut down the database
             SHUTDOWN IMMEDIATE;
            \s
             # Start the database in mount mode for recovery
             STARTUP MOUNT;
            \s
             # Set all datafiles offline to ensure clean recovery
             # SQL "ALTER DATABASE DATAFILE ALL OFFLINE";
            \s
             # Restore the entire database
             RESTORE DATABASE;
            \s
             # Bring datafiles back online
             # SQL "ALTER DATABASE DATAFILE ALL ONLINE";
            \s
             # Perform complete recovery
             RECOVER DATABASE;
            \s
             # Open the database for normal operation
             SQL "ALTER DATABASE OPEN";
            \s
             # Optional: Reset logs if needed after recovery
             # SQL "ALTER DATABASE OPEN RESETLOGS";
         }
      """;
    return     RmanExecutor.executeRmanScript(command,dockerContainer,isDocker);
  }


  // 2024-12-29 20:24:30 time format
  public String restore_backup_date(String date) {
    String command = "RUN {\n" +
      " SHUTDOWN IMMEDIATE;\n" + // Ensure the database is properly shut down
      " STARTUP MOUNT;\n" + // Mount the database for restore
      " SET UNTIL TIME \"TO_DATE('" + date + "', 'YYYY-MM-DD HH24:MI:SS')\";\n" +
      " ALLOCATE CHANNEL ch1 DEVICE TYPE DISK;\n" +
      " RESTORE DATABASE;\n" +
      " RECOVER DATABASE;\n" +
      " RELEASE CHANNEL ch1;\n" +
      " ALTER DATABASE OPEN RESETLOGS;\n" + // Open the database with resetlogs after recovery
      "}";

    return RmanExecutor.executeRmanScript(command, dockerContainer, isDocker);
  }


  public List<BackupDetails> listBackups() {
    String command = "LIST BACKUP SUMMARY;";
    String rmanOutput = RmanExecutor.executeRmanScript(command, dockerContainer, isDocker);
    return BackupDetails.parseFromRmanOutput(rmanOutput);
  }

}
