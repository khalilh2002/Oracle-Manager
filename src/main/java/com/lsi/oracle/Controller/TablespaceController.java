package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.TablespaceRequest;
import com.lsi.oracle.Service.TablespaceService;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;

@RestController
public class TablespaceController {
  private TablespaceService tablespaceService;

  TablespaceController(TablespaceService tablespaceService) {
    this.tablespaceService = tablespaceService;
  }

  @PostMapping("/tablespace/create")
  public String createTablespace(@RequestBody TablespaceRequest tablespaceRequest){
    try {
      tablespaceService.createTablespace(tablespaceRequest);
    }catch (SQLException e){
      return "SQL EXCEPTION::"+e.getMessage();
    }catch (Exception e){
      return e.getMessage();
    }
    return "Tablespace created";
  }

  @DeleteMapping("/tablespace/delete")
  public String deleteTablespace(@RequestBody TablespaceRequest tablespaceRequest){
    try {
      tablespaceService.deleteTablespace(tablespaceRequest);
    }catch (SQLException e){
      return "SQL EXCEPTION::"+e.getMessage();
    }catch (Exception e){
      return e.getMessage();
    }
    return "Tablespace"+tablespaceRequest.name()+" deleted";
  }
}
