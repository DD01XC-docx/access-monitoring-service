package com.dd01xc.service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.dd01xc.service.model.ChartDataDTO;
import com.dd01xc.service.model.AccessEvent;
import com.dd01xc.service.model.ChartDataDTO.LatencyData;
import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;

@Service
public class AccessStatService {
    //const
    private static final String RANGE_1H = "1h";
    private static final String RANGE_24H = "24h";
    private static final String RANGE_7D = "7d";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private final AccessRepository accessRepository;
    private final UserRepository userRepository;

    public AccessStatService(AccessRepository accessRepository, UserRepository userRepository) {
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }
    
    //getHourlyStats

    public ChartDataDTO getHourlyStats(String range) {
        // main chart data load
        List<Object[]> dbRows = loadRowsByRange(range);
        return mapRowsToChartData(dbRows);
    }
    private List<Object[]> loadRowsByRange(String range) {
        return switch (range.toLowerCase()) {
            case RANGE_1H -> accessRepository.getStatsLastHourByMinute();
            case RANGE_7D -> accessRepository.getStatsLast7DaysByDay();
            case RANGE_24H -> accessRepository.getStatsLast24HoursByHour();
            default -> accessRepository.getStatsLast24HoursByHour();
        };
    }

    private ChartDataDTO mapRowsToChartData(List<Object[]> dbRows) {
        // convert db rows to chart dto format
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

    //getSlaStatus

    public Map<String, Object> getSlaStatus() {

        long success = accessRepository.countByStatus(STATUS_SUCCESS);
        long failed = accessRepository.countFailedForExistingAccounts();
        long total = success + failed;
        int percentage = (total > 0) ? (int) ((double) success / total * 100) : 0;

        return Map.of("series", List.of(percentage));
    }

    //getAlertLevels

     public ChartDataDTO getAlertLevels(String range) {

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
    
    //getTopFailedAccounts

   public ChartDataDTO getTopFailedAccounts() {
        // top failed accounts for card
        List<Object[]> dbRows = accessRepository.getTopFailedAccountsLast24Hours();

        List<String> categories = new ArrayList<>();
        List<Integer> failedCounts = new ArrayList<>();

        //empty if failed
        if (dbRows.isEmpty()) {
            return new ChartDataDTO(
                List.of("No failed logins"),
                List.of(new ChartDataDTO.Series("Failed attempts", List.of(0)))
            );
        }

        for (Object[] row : dbRows) {
            String account = extractAccount(row);
            int failedCount = extractNumber(row, 1);
            categories.add(account);
            failedCounts.add(failedCount);
        }

        return new ChartDataDTO(
            categories,
            List.of(new ChartDataDTO.Series("Failed attempts", failedCounts))
        );
    }

    //getAgent

    public Map<String, Object> getAgent() {

        List<Object[]> rows = userRepository.usersByStat();
        List<Map<String, Object>> points = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < rows.size(); i++) {
        Object[] row = rows.get(i);
        String status;

        if (row[0] != null) {
            status = row[0].toString();
        } else {status = "UNKNOWN";}

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

    //checkDB

    public List<Map<String, Object>> checkDB() {
        List<Map<String, Object>> health = new ArrayList<>();
        Map<String, Object> userDb = new HashMap<>();

        boolean userDbUp = isRepoUp(userRepository);
        boolean accessDbUp = isRepoUp(accessRepository);

        userDb.put("x", "User DB");
        userDb.put("y", userDbUp ? 1 : 0);
        userDb.put("status", userDbUp ? "UP" : "DOWN");
        health.add(userDb);

        Map<String, Object> accessDb = new HashMap<>();

        accessDb.put("x", "Access DB");
        accessDb.put("y", accessDbUp ? 1 : 0);
        accessDb.put("status", accessDbUp ? "UP" : "DOWN");
        health.add(accessDb);

        return health;
    }

    //getResponceTimeDistribution

    public Map<String, Object> getResponceTimeDistribution() {
        List<Object[]> rawData = accessRepository.getResponceTime();
        List<LatencyData> result = new ArrayList<>();

        for (Object[] row : rawData) {
            result.add(new LatencyData(
                row[0].toString(),
                ((Number) row[1]).doubleValue(),
                ((Number) row[2]).doubleValue(),
                ((Number) row[3]).doubleValue(),
                ((Number) row[4]).doubleValue(),
                ((Number) row[5]).doubleValue()
            ));
        }

        Map<String, Object> seriesData = new HashMap<>();
        seriesData.put("name", "Response time");
        seriesData.put("data", result);

        return Map.of("series", List.of(seriesData));
    }

    public List<AccessEvent> getRecentLogs() {
        return accessRepository.findTop10ByOrderByCreatedAtDesc();
    }

    //extra-help-functions
    private static String extractCategory(Object[] row) {
            if (row.length == 0 || row[0] == null) {return "00:00";}
            return row[0].toString();
        }
    
    private static int extractNumber(Object[] row, int index) {
        if (row.length <= index || !(row[index] instanceof Number number)) {return 0;}
        return number.intValue();
    }

    private static String extractAccount(Object[] row) {
        if (row.length == 0 || row[0] == null) {return "unknown";}
        return row[0].toString();
    }

     private static boolean isBetween(int value, int minInclusive, int maxInclusive) {
        return value >= minInclusive && value <= maxInclusive;
    }

    private static boolean isRepoUp(JpaRepository<?, ?> repo) {
        try {
            repo.count();
            return true;
        } catch (Exception e) {return false;}
    }
}
