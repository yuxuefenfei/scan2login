package com.wwz.login.qrauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/qr-scan")
    public String qrScanPage() {
        return "qr-scan";
    }

    @GetMapping("/success")
    public String successPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "success";
    }

    // 模拟手机App页面
    @GetMapping("/app/login")
    public String appLoginPage() {
        return "app-login";
    }

    @GetMapping("/app/scan")
    public String appScanPage() {
        return "app-scan";
    }
}
