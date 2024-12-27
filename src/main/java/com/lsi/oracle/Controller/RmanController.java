package com.lsi.oracle.Controller;

import com.lsi.oracle.Service.RmanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RmanController {
  @Autowired
  RmanService rmanService;


  @GetMapping("/rman/backup")
  public String backup() {
    return rmanService.backup();
  }

  @GetMapping("/rman/restore_recover")
  public String restore_recover() {
    return rmanService.restore_recover();
  }
}
