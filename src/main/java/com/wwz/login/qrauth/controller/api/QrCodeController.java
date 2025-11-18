package com.wwz.login.qrauth.controller.api;

import com.wwz.login.qrauth.entity.ScanStatus;
import com.wwz.login.qrauth.service.QrAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
public class QrCodeController {

    @Autowired
    private QrAuthService qrAuthService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQrCode() {
        var qrId = qrAuthService.generateQrCode();
        var response = new HashMap<String, Object>();
        response.put("qrId", qrId);
        response.put("qrContent", "/app/confirm?qrId=" + qrId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{qrId}")
    public ResponseEntity<Map<String, Object>> getQrStatus(@PathVariable String qrId) {
        var response = new HashMap<String, Object>();

        var status = qrAuthService.getQrStatus(qrId);
        if (status == null) {
            response.put("status", ScanStatus.EXPIRED);
            return ResponseEntity.ok(response);
        }

        response.put("status", status.getScanStatus());
        response.put("pcToken", status.getPcToken());

        // 如果是已扫描状态，返回用户信息
        if (status.getScanStatus() == ScanStatus.SCANNED) {
            response.put("user", qrAuthService.getUserByName(status.getUsername()));
        }

        return ResponseEntity.ok(response);
    }
}