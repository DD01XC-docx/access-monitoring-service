package com.dd01xc.service.model;

import java.util.List;
//mainChartDTO
public class ChartDataDTO {

    private List<String> categories;
    private List<Series> series;

    public ChartDataDTO() {
    }

    public ChartDataDTO(List<String> categories, List<Series> series) {
        this.categories = categories;
        this.series = series;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }

    //series
    public static class Series {

        private String name;
        private List<Integer> data;

        public Series() {
        }

        public Series(String name, List<Integer> data) {
            this.name = name;
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Integer> getData() {
            return data;
        }

        public void setData(List<Integer> data) {
            this.data = data;
        }
    }

    //latency
    public static class LatencyData {
        public String x;
        public Double[] y;

        public LatencyData(String x, Double min, Double val1, Double median, Double val3, Double max) {
            this.x = x;
            this.y = new Double[]{min, val1, median, val3, max};
        }
    }
}
