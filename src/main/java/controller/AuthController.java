package com.campus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final Pattern FOSU_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@fosu\\.edu\\.cn$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VERIFY_CODE_PATTERN = Pattern.compile("^[0-9]{6}$");
    private static final int MAX_EMAIL_LENGTH = 100;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int SEND_CODE_COOLDOWN = 60; // 秒

    @Autowired
    private EmailVerifyService emailVerifyService;
    
    @Autowired
    private UserService userService;

    /**
     * 发送邮箱验证码接口
     * 1. 校验邮箱格式（@fosu.edu.cn）
     * 2. 检查发送频率（60秒内不重复发送）
     * 3. 检查用户是否已认证（已认证不能再申请）
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        // 1. 参数校验 - 邮箱为空
        if (email == null || email.trim().isEmpty()) {
            logger.warn("发送验证码失败：邮箱为空");
            return buildResponse(false, "邮箱地址不能为空", 400);
        }
        
        email = email.trim().toLowerCase();
        
        // 2. 邮箱长度校验
        if (email.length() > MAX_EMAIL_LENGTH) {
            logger.warn("发送验证码失败：邮箱长度超限 - {}", email);
            return buildResponse(false, "邮箱地址格式错误", 400);
        }
        
        // 3. 邮箱格式校验
        if (!FOSU_EMAIL_PATTERN.matcher(email).matches()) {
            logger.warn("发送验证码失败：非本校邮箱 - {}", maskEmail(email));
            return buildResponse(false, "仅允许 @fosu.edu.cn 邮箱", 400);
        }
        
        // 4. 检查用户是否已认证
        if (userService.isUserVerified(email)) {
            logger.warn("用户已认证，禁止重复申请: {}", maskEmail(email));
            return buildResponse(false, "您已完成校园认证，无需重复申请", 400);
        }
        
        // 5. 检查发送频率（防止频繁请求）
        if (!emailVerifyService.canSendCode(email, SEND_CODE_COOLDOWN)) {
            logger.warn("验证码发送过于频繁: {}", maskEmail(email));
            return buildResponse(false, "验证码已发送，请稍后再试", 429);
        }
        
        try {
            emailVerifyService.generateAndSendCode(email);
            logger.info("验证码已发送至邮箱: {}", maskEmail(email));
            return buildResponse(true, "验证码已发送至邮箱", 200);
        } catch (Exception e) {
            logger.error("邮件发送失败 - {}", maskEmail(email), e);
            return buildResponse(false, "邮件发送失败，请稍后重试", 500);
        }
    }

    /**
     * 校验验证码并更新认证状态
     * 1. 校验邮箱和验证码格式
     * 2. 防暴力破解（限制验证尝试次数）
     * 3. 校验验证码正确性
     * 4. 更新用户认证状态
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyStatus(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        // 1. 参数校验 - 邮箱为空
        if (email == null || email.trim().isEmpty()) {
            logger.warn("校验验证码失败：邮箱为空");
            return buildResponse(false, "邮箱地址不能为空", 400);
        }
        
        email = email.trim().toLowerCase();
        
        // 2. 参数校验 - 验证码为空
        if (code == null || code.trim().isEmpty()) {
            logger.warn("校验验证码失败：验证码为空 - {}", maskEmail(email));
            return buildResponse(false, "验证码不能为空", 400);
        }
        
        code = code.trim();
        
        // 3. 邮箱格式校验
        if (!FOSU_EMAIL_PATTERN.matcher(email).matches()) {
            logger.warn("校验验证码失败：非本校邮箱 - {}", maskEmail(email));
            return buildResponse(false, "邮箱格式错误", 400);
        }
        
        // 4. 验证码格式校验（6位数字）
        if (!VERIFY_CODE_PATTERN.matcher(code).matches()) {
            logger.warn("校验验证码失败：验证码格式错误 - {}", maskEmail(email));
            return buildResponse(false, "验证码格式错误，应为6位数字", 400);
        }
        
        // 5. 检查用户是否已认证
        if (userService.isUserVerified(email)) {
            logger.warn("用户已认证: {}", maskEmail(email));
            return buildResponse(false, "您已完成校园认证", 400);
        }
        
        // 6. 防暴力破解 - 检查验证失败次数
        if (emailVerifyService.getFailedAttempts(email) >= MAX_VERIFY_ATTEMPTS) {
            logger.warn("验证失败次数过多，已锁定: {}", maskEmail(email));
            return buildResponse(false, "验证失败次数过多，请1小时后重试", 429);
        }
        
        // 7. 校验验证码正确性
        boolean isValid = emailVerifyService.checkVerifyCode(email, code);
        
        if (isValid) {
            try {
                // 清除失败次数计数
                emailVerifyService.clearFailedAttempts(email);
                
                // 更新用户认证状态
                userService.updateVerifyStatus(email, 1);
                
                // 清除验证码
                emailVerifyService.deleteVerifyCode(email);
                
                logger.info("用户认证成功: {}", maskEmail(email));
                return buildResponse(true, "校园身份认证成功", 200);
            } catch (Exception e) {
                logger.error("更新认证状态失败 - {}", maskEmail(email), e);
                return buildResponse(false, "认证失败，请联系管理员", 500);
            }
        } else {
            // 记录验证失败次数
            emailVerifyService.recordFailedAttempt(email);
            logger.warn("验证码验证失败: {}", maskEmail(email));
            return buildResponse(false, "验证码错误或已过期", 400);
        }
    }
    
    /**
     * 辅助方法：构建统一响应格式
     */
    private ResponseEntity<Map<String, Object>> buildResponse(boolean success, String message, int statusCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(statusCode).body(response);
    }
    
    /**
     * 辅助方法：邮箱脱敏处理（用于日志）
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***";
        }
        String local = parts[0];
        String masked = local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + parts[1];
        return masked;
    }