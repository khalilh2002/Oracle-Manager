package com.lsi.oracle.Controller;

import com.lsi.oracle.model.BackupDetails;
import com.lsi.oracle.Service.RmanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rman")

public class RmanController {
  @Autowired
  RmanService rmanService;

  @GetMapping("/api/test")
  public String index() {
    return "heelo you i test";
  }

  @GetMapping("/test")
  public String test() {
    return rmanService.test();
  }

  @GetMapping("/fullBackup")
  public String backup() {
    return rmanService.full_backup();
  }

  @GetMapping("/restore_recover")
  public String restore_recover() {
    return rmanService.restore_recover();
  }

  @GetMapping("/incrementalBackup")
  public String incrementalBackup() {
    return rmanService.incremental_backup();
  }

  @GetMapping("/restoreByDate")
  public String restoreByDate(@RequestParam String date) {
    return rmanService.restore_backup_date(date);
  }

  @GetMapping("/listBackups")
  public List<BackupDetails> listBackups() {
    return rmanService.listBackups();
  }

}
