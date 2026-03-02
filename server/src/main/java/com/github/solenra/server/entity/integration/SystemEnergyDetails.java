package com.github.solenra.server.entity.integration;

import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

import com.github.solenra.server.entity.SolarSystemIntegration;
import com.github.solenra.server.entity.SystemEnergyDetailsRevenue;
import com.github.solenra.server.exceptions.ApplicationException;
import com.github.solenra.server.model.solaredge.EnergyDetails;
import com.github.solenra.server.model.solaredge.SiteOverview;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "SYSTEM_ENERGY_DETAILS")
public class SystemEnergyDetails implements Serializable {

    public static final String TIME_UNIT_HOUR = "HOUR";
    public static final String UNIT_WH = "Wh";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SOLAR_SYSTEM_INTEGRATION_ID", referencedColumnName = "ID")
    private SolarSystemIntegration solarSystemIntegration;

    @OneToMany(mappedBy = "systemEnergyDetails")
    private List<SystemEnergyDetailsRevenue> systemEnergyDetailsRevenues;

    @Column(name = "TIME_UNIT")
    private String timeUnit;

    private String unit;

    @Column(name = "START_DATE")
    private ZonedDateTime startDate;

    @Column(name = "END_DATE")
    private ZonedDateTime endDate;

    @Column(name = "PRODUCTION_VALUE")
    private BigDecimal productionValue;

    @Column(name = "IMPORT_VALUE")
    private BigDecimal importValue;

    @Column(name = "EXPORT_VALUE")
    private BigDecimal exportValue;

    @Column(name = "CONSUMPTION_VALUE")
    private BigDecimal consumptionValue;

    @Column(name = "SELF_CONSUMPTION_VALUE")
    private BigDecimal selfConsumptionValue;

    public static SystemEnergyDetails convertToEntity(EnergyDetails.Root detailsRoot) {
        if (detailsRoot == null) {
            return null;
        }

        SystemEnergyDetails systemEnergyDetails = new SystemEnergyDetails();
        systemEnergyDetails.setTimeUnit(detailsRoot.energyDetails.timeUnit);
        systemEnergyDetails.setUnit(detailsRoot.energyDetails.unit);

        if (detailsRoot.energyDetails.meters != null) {
            for (EnergyDetails.Meter meter : detailsRoot.energyDetails.meters) {
                if (meter.values.size() != 1) {
                    // TODO log error
                    return null;
                }
                switch (meter.type) {
                    case "FeedIn":
                        systemEnergyDetails.setExportValue(meter.values.getFirst().value);
                        break;
                    case "SelfConsumption":
                        systemEnergyDetails.setSelfConsumptionValue(meter.values.getFirst().value);
                        break;
                    case "Purchased":
                        systemEnergyDetails.setImportValue(meter.values.getFirst().value);
                        break;
                    case "Production":
                        systemEnergyDetails.setProductionValue(meter.values.getFirst().value);
                        break;
                    case "Consumption":
                        systemEnergyDetails.setConsumptionValue(meter.values.getFirst().value);
                        break;
                }
            }
        }

        return systemEnergyDetails;
    }

    public static SystemEnergyDetails convertToEntity(SiteOverview siteOverview) {
        if (siteOverview == null) {
            return null;
        }

        SystemEnergyDetails systemEnergyDetails = new SystemEnergyDetails();

        systemEnergyDetails.setTimeUnit("hour");
        systemEnergyDetails.setUnit("WH");

        systemEnergyDetails.setExportValue(getEnergyValue(siteOverview.getProduction().getUnit(), siteOverview.getProduction().getToGrid()));
        systemEnergyDetails.setSelfConsumptionValue(getEnergyValue(siteOverview.getProduction().getUnit(), siteOverview.getProduction().getToSelfConsumption()));
        systemEnergyDetails.setImportValue(getEnergyValue(siteOverview.getConsumption().getUnit(), siteOverview.getConsumption().getFromGrid()));
        systemEnergyDetails.setProductionValue(getEnergyValue(siteOverview.getProduction().getUnit(), siteOverview.getProduction().getTotal()));
        systemEnergyDetails.setConsumptionValue(getEnergyValue(siteOverview.getConsumption().getUnit(), siteOverview.getConsumption().getFromGrid()));

        return systemEnergyDetails;
    }

    private static BigDecimal getEnergyValue(String unit, BigDecimal givenValue) {
        BigDecimal value = null;

        if (givenValue != null) {
            switch (unit.toUpperCase()) {
                case "WH":
                    value = givenValue;
                    break;
                case "KWH":
                    value = givenValue.multiply(BigDecimal.valueOf(1000));
                    break;
                case "MWH":
                    value = givenValue.multiply(BigDecimal.valueOf(1000000));
                    break;
                default:
                    throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected unit from SolarEdge V2 API");
            }
        }

        return value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public SolarSystemIntegration getSolarSystemIntegration() {
        return solarSystemIntegration;
    }

    public void setSolarSystemIntegration(SolarSystemIntegration solarSystemIntegration) {
        this.solarSystemIntegration = solarSystemIntegration;
    }

    public List<SystemEnergyDetailsRevenue> getSystemEnergyDetailsRevenues() {
        return systemEnergyDetailsRevenues;
    }

    public void setSystemEnergyDetailsRevenues(List<SystemEnergyDetailsRevenue> systemEnergyDetailsRevenues) {
        this.systemEnergyDetailsRevenues = systemEnergyDetailsRevenues;
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

}
