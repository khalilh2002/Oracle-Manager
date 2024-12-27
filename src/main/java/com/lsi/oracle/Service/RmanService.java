package com.lsi.oracle.Service;

import com.lsi.oracle.Service.ResourceClass.Rman.RmanExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class RmanService {
  //@Value("${project.env.rman.docker-container}")
  private static String dockerContainer = "oracle-db-yaml";

  //@Value("${project.env.rman.is-docker}")
  private static Boolean isDocker = true;

  public String backup(){
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




}
