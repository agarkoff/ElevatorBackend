package com.example.elevatorbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
public class ElevatorController {

    private final RestTemplate restTemplate;

    @Value("${relay.ip}")
    private String relayIp;

    private long timestamp = 0;

    private int getCooldownSeconds() {
        try {
            String cooldown = FileUtils.readFileToString(new File("cooldown.txt"), "UTF-8").trim();
            return Integer.parseInt(cooldown);
        } catch (Exception e) {
            log.warn("Не удалось прочитать cooldown.txt, используется значение по умолчанию 60 секунд: {}", e.getMessage());
            return 60; // значение по умолчанию
        }
    }

    @PostMapping("post")
    public Long post(@RequestParam("code") String code, @RequestParam("relay") String relay) throws IOException {
        log.info("call elevator with code: {}, relay={}", code, relay);
        if (!checkCode(code)) {
            log.error("invalid code: {}", code);
            return 1L;
        }
        long now = System.currentTimeMillis() / 1000;
        long delta = now - timestamp;
        int cooldownSeconds = getCooldownSeconds();
        if (delta < cooldownSeconds) {
            log.info("нельзя вызывать лифт чаще, чем один раз в {} секунд, прошло только {} секунд", cooldownSeconds, delta);
            return 300 + (cooldownSeconds - delta);
        }
        String url = String.format("http://%s/relay_cgi.cgi?type={type}&relay={relay}&on={on}&time={time}&pwd={pwd}", relayIp);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, Map.of(
                "type", "1",
                "relay", relay,
                "on", "1",
                "time", "5",
                "pwd", "3789"
        ));
        log.info("httpCode={}", responseEntity.getStatusCode());
        timestamp = now;
        return 2L;
    }

    private boolean checkCode(String code) throws IOException {
        List<String> codes = FileUtils.readLines(new File("ids.txt"), "UTF-8");
        return codes.contains(code);
    }
}