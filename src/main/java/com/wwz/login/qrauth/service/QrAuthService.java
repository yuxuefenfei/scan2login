package com.wwz.login.qrauth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wwz.login.qrauth.entity.QrCodeInfo;
import com.wwz.login.qrauth.entity.ScanStatus;
import com.wwz.login.qrauth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class QrAuthService {

    // 模拟用户数据库
    private final Map<String, User> userDatabase = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    public QrAuthService() {
        // 初始化测试用户
        Consumer<User> putter = user -> userDatabase.put(user.getUsername(), user);
        putter.accept(new User("1000001", "zhangsan", "13800138000"));
        putter.accept(new User("1000002", "lisi", "13900139000"));
    }

    public String generateQrCode() {
        var qrId = UUID.randomUUID().toString();
        var status = new QrCodeInfo(qrId);

        // 存储到Redis，初始过期时间2分钟
        redisTemplate.opsForValue().set(buildRedisKey(qrId), status, Duration.ofMinutes(2));
        return qrId;
    }

    public QrCodeInfo getQrStatus(String qrId) {
        var data = redisTemplate.opsForValue().get(buildRedisKey(qrId));
        return objectMapper.convertValue(data, QrCodeInfo.class);
    }

    public boolean scanQrCode(String qrId, String appToken) {
        var redisKey = buildRedisKey(qrId);
        var status = getQrStatus(qrId);

        if (status == null || status.getScanStatus() != ScanStatus.INIT) {
            return false;
        }

        // 验证App Token并获取用户信息
        var userId = jwtTokenService.validateTokenAndGetUserId(appToken);
        var username = jwtTokenService.validateTokenAndGetUsername(appToken);

        if (userId == null) {
            return false;
        }

        // 更新状态
        status.setScanStatus(ScanStatus.SCANNED);
        status.setAppToken(appToken);
        status.setUserId(userId);
        status.setUsername(username);
        status.setScanTime(Instant.now());

        // 重新设置过期时间为30秒
        redisTemplate.opsForValue().set(redisKey, status, Duration.ofSeconds(30));
        return true;
    }

    public String confirmLogin(String qrId) {
        var redisKey = buildRedisKey(qrId);
        var status = getQrStatus(qrId);

        if (status == null || status.getScanStatus() != ScanStatus.SCANNED) {
            return null;
        }

        // 获取用户信息
        var user = userDatabase.get(status.getUsername());
        if (user == null) {
            return null;
        }

        // 生成PC端Token
        var pcToken = jwtTokenService.generateToken(user.getId(), user.getUsername());
        status.setScanStatus(ScanStatus.CONFIRMED);
        status.setPcToken(pcToken);
        status.setConfirmTime(Instant.now());

        // 确认后10秒过期，让PC端有时间获取
        redisTemplate.opsForValue().set(redisKey, status, Duration.ofSeconds(10));

        return pcToken;
    }

    public User getUserByAppToken(String appToken) {
        var username = jwtTokenService.validateTokenAndGetUsername(appToken);
        return userDatabase.get(username);
    }

    public User getUserByName(String username) {
        return userDatabase.get(username);
    }

    private String buildRedisKey(String qrId) {
        return "QR_AUTH:" + qrId;
    }
}
