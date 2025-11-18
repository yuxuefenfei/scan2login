package com.wwz.login.qrauth.controller.api;

import com.wwz.login.qrauth.service.JwtTokenService;
import com.wwz.login.qrauth.service.QrAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private QrAuthService qrAuthService;

    @Autowired
    private JwtTokenService jwtTokenService;

    // 模拟手机App登录，获取App Token
    @PostMapping("/app-login")
    public ResponseEntity<Map<String, Object>> appLogin(@RequestBody Map<String, String> loginRequest) {
        var response = new HashMap<String, Object>();

        var username = loginRequest.get("username");

        // 模拟用户验证
        var user = qrAuthService.getUserByName(username);

        if (user != null) {
            var appToken = jwtTokenService.generateToken(user.getId(), user.getUsername());
            response.put("appToken", appToken);
            response.put("user", user.getUsername());
            return ResponseEntity.ok(response);
        }

        response.put("message", "登录失败");
        return ResponseEntity.badRequest().body(response);
    }

    // 手机App扫描二维码
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanQrCode(@RequestHeader("Authorization") String authorization,
                                                          @RequestBody Map<String, String> request) {
        var response = new HashMap<String, Object>();

        var qrId = request.get("qrId");
        var appToken = authorization.replace("Bearer ", "");

        var success = qrAuthService.scanQrCode(qrId, appToken);
        if (success) {
            var user = qrAuthService.getUserByAppToken(appToken);
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "扫描失败，二维码可能已过期");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 手机App确认登录
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmLogin(@RequestHeader("Authorization") String authorization,
                                                            @RequestBody Map<String, String> request) {
        var response = new HashMap<String, Object>();

        var qrId = request.get("qrId");
        var appToken = authorization.replace("Bearer ", "");

        // 验证Token
        var user = qrAuthService.getUserByAppToken(appToken);
        if (user == null) {
            response.put("message", "Token无效");
            return ResponseEntity.badRequest().body(response);
        }

        var pcToken = qrAuthService.confirmLogin(qrId);
        if (pcToken != null) {
            response.put("success", true);
            response.put("pcToken", pcToken);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "确认失败，二维码可能已过期");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 验证PC端Token
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        var response = new HashMap<String, Object>();

        var username = jwtTokenService.validateTokenAndGetUsername(token);
        if (username != null) {
            response.put("valid", true);
            response.put("username", username);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Token无效");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
