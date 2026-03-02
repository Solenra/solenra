package com.github.solenra.server.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.github.solenra.server.entity.SystemEnergyDetailsRevenue;
import com.github.solenra.server.entity.integration.SystemEnergyDetails;

public class SystemEnergyDetailsDto {

    private Long id;
    private SolarSystemIntegrationDto solarSystemIntegration;
    private String timeUnit;
    private String unit;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private BigDecimal productionValue;
    private BigDecimal importValue;
    private BigDecimal exportValue;
    private BigDecimal consumptionValue;
    private BigDecimal selfConsumptionValue;
    private List<SystemEnergyDetailsRevenueDto> systemEnergyDetailsRevenues = new ArrayList<>();

    public SystemEnergyDetailsDto() {
    }

    public SystemEnergyDetailsDto(SystemEnergyDetails systemEnergyDetails) {
        if (systemEnergyDetails != null) {
            this.id = systemEnergyDetails.getId();
            this.solarSystemIntegration = new SolarSystemIntegrationDto(systemEnergyDetails.getSolarSystemIntegration());
            this.timeUnit = systemEnergyDetails.getTimeUnit();
            this.unit = systemEnergyDetails.getUnit();
            this.startDate = systemEnergyDetails.getStartDate();
            this.endDate = systemEnergyDetails.getEndDate();
            this.productionValue = systemEnergyDetails.getProductionValue();
            this.importValue = systemEnergyDetails.getImportValue();
            this.exportValue = systemEnergyDetails.getExportValue();
            this.consumptionValue = systemEnergyDetails.getConsumptionValue();
            this.selfConsumptionValue = systemEnergyDetails.getSelfConsumptionValue();

            if (systemEnergyDetails.getSystemEnergyDetailsRevenues() != null) {
                for (SystemEnergyDetailsRevenue systemEnergyDetailsRevenue : systemEnergyDetails.getSystemEnergyDetailsRevenues()) {
                    systemEnergyDetailsRevenues.add(new SystemEnergyDetailsRevenueDto(systemEnergyDetailsRevenue, false));
                }
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SolarSystemIntegrationDto getSolarSystemIntegration() {
        return solarSystemIntegration;
    }

    public void setSolarSystemIntegration(SolarSystemIntegrationDto solarSystemIntegration) {
        this.solarSystemIntegration = solarSystemIntegration;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getProductionValue() {
        return productionValue;
    }

    public void setProductionValue(BigDecimal productionValue) {
        this.productionValue = productionValue;
    }

    public BigDecimal getImportValue() {
        return importValue;
    }

    public void setImportValue(BigDecimal importValue) {
        this.importValue = importValue;
    }

    public BigDecimal getExportValue() {
        return exportValue;
    }

    public void setExportValue(BigDecimal exportValue) {
        this.exportValue = exportValue;
    }

    public BigDecimal getConsumptionValue() {
        return consumptionValue;
    }

    public void setConsumptionValue(BigDecimal consumptionValue) {
        this.consumptionValue = consumptionValue;
    }

    public BigDecimal getSelfConsumptionValue() {
        return selfConsumptionValue;
    }

    public void setSelfConsumptionValue(BigDecimal selfConsumptionValue) {
        this.selfConsumptionValue = selfConsumptionValue;
    }

    public List<SystemEnergyDetailsRevenueDto> getSystemEnergyDetailsRevenues() {
        return systemEnergyDetailsRevenues;
    }

    public void setSystemEnergyDetailsRevenues(List<SystemEnergyDetailsRevenueDto> systemEnergyDetailsRevenues) {
        this.systemEnergyDetailsRevenues = systemEnergyDetailsRevenues;
    }

}
