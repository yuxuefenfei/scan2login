package com.wwz.login.qrauth.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class QrCodeInfo {

    private String qrId;

    private ScanStatus scanStatus;

    private String userId;

    private String username;

    private String pcToken;

    private String appToken;

    private Instant createTime;

    private Instant scanTime;

    private Instant confirmTime;

    public QrCodeInfo() {
    }

    public QrCodeInfo(String qrId) {
        this.qrId = qrId;
        this.scanStatus = ScanStatus.INIT;
        this.createTime = Instant.now();
    }
}