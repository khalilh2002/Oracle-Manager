package com.lsi.oracle.Controller;

import com.lsi.oracle.Service.RmanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RmanController {
  @Autowired
  RmanService rmanService;

  @GetMapping("/rman/test")
  public String test() {
    return rmanService.test();
  }
}
