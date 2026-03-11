package com.dd01xc.service.controller;

import com.dd01xc.service.model.ChartDataDTO;
import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/access")
@CrossOrigin(origins = "*")
public class AccessController {

    //const
    private static final String RANGE_1H = "1h";
    private static final String RANGE_24H = "24h";
    private static final String RANGE_7D = "7d";
    private static final String STATUS_SUCCESS = "SUCCESS";

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private UserRepository userRepository;

//mappings
    @GetMapping("/stat/hourly")
    public ChartDataDTO getHourlyStats(@RequestParam(defaultValue = RANGE_24H) String range) {
        // main chart data load
        List<Object[]> dbRows = loadRowsByRange(range);
        return mapRowsToChartData(dbRows);
    }
    @GetMapping("/stat/sla")
    public Map<String, Object> getSlaStatus() {
        long total = accessRepository.count();
        long success = accessRepository.countByStatus(STATUS_SUCCESS);
        int percentage = (total > 0) ? (int) ((double) success / total * 100) : 0;
        return Map.of("series", List.of(percentage));
    }

    @GetMapping("/stat/alert-lvls")
    public ChartDataDTO getAlertLevels(@RequestParam(defaultValue = RANGE_24H) String range) {
        List <Object[]> dbRows = loadRowsByRange(range);
        List<String> categories = new ArrayList<>();
        List <Integer> lowDanger = new ArrayList<>();
        List <Integer> mediumDanger = new ArrayList<>();
        List <Integer> highDanger = new ArrayList<>();
        List <Integer> criticalDanger = new ArrayList<>();

        for (Object[] row : dbRows) {
            categories.add(extractCategory(row));

            int failedCount = extractNumber(row, 2);
            lowDanger.add(isBetween(failedCount, 1, 2) ? failedCount : 0);
            mediumDanger.add(isBetween(failedCount, 3, 4) ? failedCount : 0);
            highDanger.add(isBetween(failedCount, 5, 6) ? failedCount : 0);
            criticalDanger.add(failedCount >= 7 ? failedCount : 0);
        }
        return new ChartDataDTO(categories, List.of(
            new ChartDataDTO.Series("Low", lowDanger),
            new ChartDataDTO.Series("Medium", mediumDanger),
            new ChartDataDTO.Series("High", highDanger),
            new ChartDataDTO.Series("Critical", criticalDanger)
        ));
    }
    

    @GetMapping("/stat/top-failed")
    public ChartDataDTO getTopFailedAccounts() {
        // top failed accounts for card
        List<Object[]> dbRows = accessRepository.getTopFailedAccountsLast24Hours();

        //empty if failed
        if (dbRows.isEmpty()) {
            return new ChartDataDTO(
                List.of("No failed logins"),
                List.of(new ChartDataDTO.Series("Failed attempts", List.of(0)))
            );
        }

        List<String> categories = new ArrayList<>();
        List<Integer> failedCounts = new ArrayList<>();

        for (Object[] row : dbRows) {
        categories.add(extractAccount(row));
        failedCounts.add(extractNumber(row, 1));
        }

        return new ChartDataDTO(
            categories,
            List.of(new ChartDataDTO.Series("Failed attempts", failedCounts))
        );
    }
    @GetMapping("/stat/agent-status")
    public Map<String, Object> getAgent() {
        List<Object[]> rows = userRepository.usersByStat();
        List<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < rows.size(); i++) {
        Object[] row = rows.get(i);
        String status;
        if (row[0] != null) {
            status = row[0].toString();
        } else {
            status = "UNKNOWN";
        }
        int count = 0;
        if (row[1] != null) {
            count = ((Number) row[1]).intValue();
        }
        Map<String, Object> point = new HashMap<>();
        point.put("x", status);
        point.put("y", count);
        points.add(point);
        }
        if (points.size() == 0) {
            Map<String, Object> noData = new HashMap<>();
            noData.put("x", "NO DATA");
            noData.put("y", 0);
            points.add(noData);
        }
        Map<String, Object> seriesData = new HashMap<>();
        seriesData.put("name", "Agents");
        seriesData.put("data", points);
        List<Map<String, Object>> seriesList = new ArrayList<>();
        seriesList.add(seriesData);
        Map<String, Object> response = new HashMap<>();
        response.put("series", seriesList);
        return response;
    }

    @GetMapping("/stat/health")
        public List<Map<String, Object>> checkDB(@RequestParam(required = false) String param) {
        List<Map<String, Object>> health = new ArrayList<>();
        Map<String, Object> userDb = new HashMap<>();
        userDb.put("x", "User DB");
        userDb.put("y", isRepoUp(userRepository) ? 1 : 0);
        userDb.put("status", isRepoUp(userRepository) ? "UP" : "DOWN");
        health.add(userDb);
        Map<String, Object> accessDb = new HashMap<>();
        accessDb.put("x", "Access DB");
        accessDb.put("y", isRepoUp(accessRepository) ? 1 : 0);
        userDb.put("status", isRepoUp(userRepository) ? "UP" : "DOWN");
        health.add(accessDb);
        return health;
}
    
    private boolean isRepoUp(JpaRepository<?, ?> repo) {
        try {
            repo.count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<Object[]> loadRowsByRange(String range) {
        return switch (range.toLowerCase()) {
            case RANGE_1H -> accessRepository.getStatsLastHourByMinute();
            case RANGE_7D -> accessRepository.getStatsLast7DaysByDay();
            case RANGE_24H -> accessRepository.getStatsLast24HoursByHour();
            default -> accessRepository.getStatsLast24HoursByHour();
        };
    }
    //success-failed-data-to-rows
    private ChartDataDTO mapRowsToChartData(List<Object[]> dbRows) {
        // convert db rows -> chart dto format
        List<String> categories = new ArrayList<>();
        List<Integer> successfulSeries = new ArrayList<>();
        List<Integer> failedSeries = new ArrayList<>();
        for (Object[] row : dbRows) {
        categories.add(extractCategory(row));
        successfulSeries.add(extractNumber(row, 1));
        failedSeries.add(extractNumber(row, 2));
        }
        return new ChartDataDTO(
                categories,
                List.of(
                    new ChartDataDTO.Series("Successful", successfulSeries),
                        new ChartDataDTO.Series("Failed", failedSeries)
                )
        );
    }
    //secure if graph is empty!
    private String extractCategory(Object[] row) {
        if (row.length == 0 || row[0] == null) {
            return "00:00";
        }
        return row[0].toString();
    }
    private int extractNumber(Object[] row, int index) {
    if (row.length <= index || !(row[index] instanceof Number number)) {
            return 0;
    }
        return number.intValue();
    }

    private String extractAccount(Object[] row) {
        if (row.length == 0 || row[0] == null) {
        return "unknown";
        }
        return row[0].toString();
    }

     private boolean isBetween(int value, int minInclusive, int maxInclusive) {
        return value >= minInclusive && value <= maxInclusive;
    }
}
