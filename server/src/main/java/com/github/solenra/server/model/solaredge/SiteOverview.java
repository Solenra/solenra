package com.github.solenra.server.model.solaredge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteOverview {

    private Consumption consumption;
    private Performance performance;
    private Production production;
    private Long siteId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public Consumption getConsumption() {
        return consumption;
    }

    public void setConsumption(Consumption consumption) {
        this.consumption = consumption;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    @Override
    public String toString() {
        return "SiteOverview{" +
                "consumption=" + consumption +
                ", performance=" + performance +
                ", production=" + production +
                ", siteId=" + siteId +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Consumption {

        private BigDecimal fromGrid;
        private BigDecimal fromPv;
        private BigDecimal fromStorage;
        private BigDecimal total;
        private String unit;

        public BigDecimal getFromGrid() {
            return fromGrid;
        }

        public void setFromGrid(BigDecimal fromGrid) {
            this.fromGrid = fromGrid;
        }

        public BigDecimal getFromPv() {
            return fromPv;
        }

        public void setFromPv(BigDecimal fromPv) {
            this.fromPv = fromPv;
        }

        public BigDecimal getFromStorage() {
            return fromStorage;
        }

        public void setFromStorage(BigDecimal fromStorage) {
            this.fromStorage = fromStorage;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        @Override
        public String toString() {
            return "Consumption{" +
                    "fromGrid=" + fromGrid +
                    ", fromPv=" + fromPv +
                    ", fromStorage=" + fromStorage +
                    ", total=" + total +
                    ", unit='" + unit + '\'' +
                    '}';
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Performance {

        private BigDecimal performanceRatio;
        private BigDecimal specificYield;

        public BigDecimal getPerformanceRatio() {
            return performanceRatio;
        }

        public void setPerformanceRatio(BigDecimal performanceRatio) {
            this.performanceRatio = performanceRatio;
        }

        public BigDecimal getSpecificYield() {
            return specificYield;
        }

        public void setSpecificYield(BigDecimal specificYield) {
            this.specificYield = specificYield;
        }

        @Override
        public String toString() {
            return "Performance{" +
                    "performanceRatio=" + performanceRatio +
                    ", specificYield=" + specificYield +
                    '}';
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Production {

        private BigDecimal toGrid;
        private BigDecimal toSelfConsumption;
        private BigDecimal toStorage;
        private BigDecimal total;
        private String unit;

        public BigDecimal getToGrid() {
            return toGrid;
        }

        public void setToGrid(BigDecimal toGrid) {
            this.toGrid = toGrid;
        }

        public BigDecimal getToSelfConsumption() {
            return toSelfConsumption;
        }

        public void setToSelfConsumption(BigDecimal toSelfConsumption) {
            this.toSelfConsumption = toSelfConsumption;
        }

        public BigDecimal getToStorage() {
            return toStorage;
        }

        public void setToStorage(BigDecimal toStorage) {
            this.toStorage = toStorage;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        @Override
        public String toString() {
            return "Production{" +
                    "toGrid=" + toGrid +
                    ", toSelfConsumption=" + toSelfConsumption +
                    ", toStorage=" + toStorage +
                    ", total=" + total +
                    ", unit='" + unit + '\'' +
                    '}';
        }

    }

}