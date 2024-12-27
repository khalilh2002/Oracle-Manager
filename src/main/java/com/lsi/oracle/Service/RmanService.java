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


  private String command;
  public RmanService(){
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    command = "RUN {\n" +
      "    ALLOCATE CHANNEL ch1 DEVICE TYPE DISK;\n" +
      "    BACKUP SPFILE;\n" +
      "    BACKUP CURRENT CONTROLFILE;\n" +
      "    BACKUP DATABASE PLUS ARCHIVELOG DELETE INPUT;\n" + // Added missing semicolon
      "    RELEASE CHANNEL ch1;\n" +                         // This command needs to be after the backup
      "}";
  }
  public String test(){

    return     RmanExecutor.executeRmanScript(command,dockerContainer,isDocker);
  }

}
