package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.UserRequest;
import com.lsi.oracle.Service.UserService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
public class UserController {
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/create")
    public String createUser(@RequestBody UserRequest userRequest) {
        try {
            userService.createUser(userRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "User " + userRequest.username() + " created.";
    }

    @PutMapping("/user/modify")
    public String modifyUser(@RequestBody UserRequest userRequest) {
        try {
            userService.modifyUser(userRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "User " + userRequest.username() + " modified.";
    }

    @DeleteMapping("/user/delete")
    public String deleteUser(@RequestBody UserRequest userRequest) {
        try {
            userService.deleteUser(userRequest.username());
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "User " + userRequest.username() + " deleted.";
    }
}
