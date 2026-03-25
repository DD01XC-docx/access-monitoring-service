package com.dd01xc.service.controller;

import com.dd01xc.service.model.ChartDataDTO;

import com.dd01xc.service.service.AccessStatService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access")
@CrossOrigin(origins = "*")
public class AccessController {

    private final AccessStatService accessStatService;

    //const
    private static final String DEFAULT_TIME_RANGE = "24h";
    
    public AccessController(AccessStatService accessStatService) {
        this.accessStatService = accessStatService;
    }

//mappings

    @GetMapping("/stat/hourly")
    public ChartDataDTO getHourlyStats(@RequestParam(defaultValue = DEFAULT_TIME_RANGE) String range) {
        return accessStatService.getHourlyStats(range);
    }

    @GetMapping("/stat/sla")
    public Map<String, Object> getSlaStatus() {
        return accessStatService.getSlaStatus(); 
    }

    @GetMapping("/stat/alert-lvls")
    public ChartDataDTO getAlertLevels(@RequestParam(defaultValue = DEFAULT_TIME_RANGE) String range) {
        return accessStatService.getAlertLevels(range);
    }
    
    @GetMapping("/stat/top-failed")
    public ChartDataDTO getTopFailedAccounts() {
        return accessStatService.getTopFailedAccounts();
    }

    @GetMapping("/stat/agent-status")
    public Map<String, Object> getAgent() {
        return accessStatService.getAgent();
    }

    @GetMapping("/stat/health")
    public List<Map<String, Object>> checkDB() {
        return accessStatService.checkDB();
    }

    @GetMapping("/stat/responce-time")
    public Map<String, Object> getResponceTimeDistribution() {
        return accessStatService.getResponceTimeDistribution();
    }
}
