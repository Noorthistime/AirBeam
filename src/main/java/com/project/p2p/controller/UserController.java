package com.project.p2p.controller;

import com.project.p2p.model.PasswordChangeRequest;
import com.project.p2p.model.SignInRequest;
import com.project.p2p.model.UserAccount;
import com.project.p2p.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private FileService fileService;

    @PostMapping("/sign-in")
    public ResponseEntity<UserAccount> signIn(@RequestBody SignInRequest request) {
        UserAccount user = fileService.signIn(request.getUserId(), request.getDisplayName(), request.getPassword());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserAccount> login(@RequestBody SignInRequest request) {
        UserAccount user = fileService.login(request.getUserId(), request.getPassword());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody PasswordChangeRequest request) {
        fileService.changePassword(request.getUserId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserAccount>> getAllUsers(@RequestParam String adminId) {
        List<UserAccount> users = fileService.getAllUsers(adminId);
        return ResponseEntity.ok(users);
    }
}
