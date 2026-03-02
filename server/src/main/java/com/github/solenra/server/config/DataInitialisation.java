package com.github.solenra.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.github.solenra.server.entity.*;
import com.github.solenra.server.repository.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitialisation {

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_INITIAL_PASSWORD}")
    private String adminInitialPassword;

    @Value("${ADMIN_INITIAL_EMAIL}")
    private String adminInitialEmail;

    @Value("${USE_SAMPLE_DATA:false}")
    private boolean useSampleData;

    private final ConfigRepository configRepository;
    private final EnergyPlanRepository energyPlanRepository;
    private final EnergyPlanRateRepository energyPlanRateRepository;
    private final EnergyPlanRatePeriodRepository energyPlanRatePeriodRepository;
    private final EnergyPlanRatePeriodDayRepository energyPlanRatePeriodDayRepository;
    private final EnergyPlanStatusRepository energyPlanStatusRepository;
    private final IdentityRepository identityRepository;
    private final IntegrationRepository integrationRepository;
    private final IntegrationAuthCredentialRepository integrationAuthCredentialRepository;
    private final SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository;

    public DataInitialisation(
            ConfigRepository configRepository,
            EnergyPlanRepository energyPlanRepository,
            EnergyPlanRateRepository energyPlanRateRepository,
            EnergyPlanRatePeriodRepository energyPlanRatePeriodRepository,
            EnergyPlanRatePeriodDayRepository energyPlanRatePeriodDayRepository,
            EnergyPlanStatusRepository energyPlanStatusRepository,
            IdentityRepository identityRepository,
            IntegrationRepository integrationRepository,
            IntegrationAuthCredentialRepository integrationAuthCredentialRepository,
            SolarSystemIntegrationStatusRepository solarSystemIntegrationStatusRepository
    ) {
        this.configRepository = configRepository;
        this.energyPlanRepository = energyPlanRepository;
        this.energyPlanRateRepository = energyPlanRateRepository;
        this.energyPlanRatePeriodRepository = energyPlanRatePeriodRepository;
        this.energyPlanRatePeriodDayRepository = energyPlanRatePeriodDayRepository;
        this.energyPlanStatusRepository = energyPlanStatusRepository;
        this.identityRepository = identityRepository;
        this.integrationRepository = integrationRepository;
        this.integrationAuthCredentialRepository = integrationAuthCredentialRepository;
        this.solarSystemIntegrationStatusRepository = solarSystemIntegrationStatusRepository;
    }

    @EventListener
    public void initialiseData(ApplicationReadyEvent event) {
        // TODO roles and permissions

        if (adminUsername != null && adminInitialPassword != null) {
            Identity adminIdentity = identityRepository.findByUsername(adminUsername);

            if (adminIdentity == null) {
                adminIdentity = new Identity();
                adminIdentity.setUsername(adminUsername);
                adminIdentity.setEmail(adminInitialEmail);
                adminIdentity.setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode(adminInitialPassword));
                identityRepository.save(adminIdentity);

                // TODO set admin role
            }
        }

        List<String> defaultConfigCodes = new ArrayList<>(Arrays.asList(
                Config.CODE_TERMS_OF_SERVICE_HTML,
                Config.CODE_PRIVACY_POLICY_HTML
        ));
        for (String configCode : defaultConfigCodes) {
            if (!configRepository.existsByCode(configCode)) {
                Config config = new Config();
                config.setCode(configCode);
                config.setValue(null);
                configRepository.save(config);
            }
        }

        List<String> energyPlanStatusCodes = new ArrayList<>(Arrays.asList(
                EnergyPlanStatus.CODE_DRAFT,
                EnergyPlanStatus.CODE_PUBLISHED
        ));
        List<EnergyPlanStatus> energyPlanStatuses = energyPlanStatusRepository.findAll();
        List<String> existingEnergyPlanStatusCodes = energyPlanStatuses.stream().map(EnergyPlanStatus::getCode).toList();
        energyPlanStatusCodes.removeAll(existingEnergyPlanStatusCodes);

        for (String statusCode : energyPlanStatusCodes) {
            String name = null;
            Long displayOrder = 0L;
            switch (statusCode) {
                case EnergyPlanStatus.CODE_DRAFT:
                    name = "Draft";
                    displayOrder = 1000L;
                    break;
                case EnergyPlanStatus.CODE_PUBLISHED:
                    name = "Published";
                    displayOrder = 2000L;
                    break;
            }

            EnergyPlanStatus energyPlanStatus = new EnergyPlanStatus();
            energyPlanStatus.setCode(statusCode);
            energyPlanStatus.setName(name);
            energyPlanStatus.setDisplayOrder(displayOrder);
            energyPlanStatus = energyPlanStatusRepository.save(energyPlanStatus);
        }

        List<String> solarSystemIntegrationStatusCodes = new ArrayList<>(Arrays.asList(
                SolarSystemIntegrationStatus.CODE_PENDING,
                SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_QUEUED,
                SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_PROCESSING,
                SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_TRANSIENT_ERROR,
                SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_ERROR,
                SolarSystemIntegrationStatus.CODE_UP_TO_DATE,
                SolarSystemIntegrationStatus.CODE_EXPIRED,
                SolarSystemIntegrationStatus.CODE_EXPIRED_CREDENTIALS,
                SolarSystemIntegrationStatus.CODE_SETUP,
                SolarSystemIntegrationStatus.CODE_DISABLED
        ));
        List<SolarSystemIntegrationStatus> solarSystemIntegrationStatuses = solarSystemIntegrationStatusRepository.findAll();
        List<String> existingSolarSystemIntegrationStatusCodes = solarSystemIntegrationStatuses.stream().map(SolarSystemIntegrationStatus::getCode).toList();
        solarSystemIntegrationStatusCodes.removeAll(existingSolarSystemIntegrationStatusCodes);

        for (String statusCode : solarSystemIntegrationStatusCodes) {
            String name = null;
            Long displayOrder = 0L;
            Boolean autoReload = false;
            switch (statusCode) {
                case SolarSystemIntegrationStatus.CODE_PENDING:
                    name = "Pending";
                    displayOrder = 1000L;
                    break;
                case SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_QUEUED:
                    name = "Queued for processing";
                    displayOrder = 2000L;
                    autoReload = true;
                    break;
                case SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_PROCESSING:
                    name = "Retrieving";
                    displayOrder = 3000L;
                    autoReload = true;
                    break;
                case SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_TRANSIENT_ERROR:
                    name = "Retrieving (transient error)";
                    displayOrder = 4000L;
                    autoReload = true;
                    break;
                case SolarSystemIntegrationStatus.CODE_LOADING_FROM_INTEGRATION_ERROR:
                    name = "Error loading data";
                    displayOrder = 5000L;
                    autoReload = true;
                    break;
                case SolarSystemIntegrationStatus.CODE_UP_TO_DATE:
                    name = "Connected";
                    displayOrder = 6000L;
                    break;
                case SolarSystemIntegrationStatus.CODE_EXPIRED:
                    name = "Disconnected";
                    displayOrder = 7000L;
                    break;
                case SolarSystemIntegrationStatus.CODE_EXPIRED_CREDENTIALS:
                    name = "Expired credentials";
                    displayOrder = 8000L;
                    break;
                case SolarSystemIntegrationStatus.CODE_SETUP:
                    name = "Setup required";
                    displayOrder = 9000L;
                    break;
                case SolarSystemIntegrationStatus.CODE_DISABLED:
                    name = "Disabled";
                    displayOrder = 100000L;
                    break;
            }

            SolarSystemIntegrationStatus solarSystemIntegrationStatus = new SolarSystemIntegrationStatus();
            solarSystemIntegrationStatus.setCode(statusCode);
            solarSystemIntegrationStatus.setName(name);
            solarSystemIntegrationStatus.setDisplayOrder(displayOrder);
            solarSystemIntegrationStatus.setAutoReload(autoReload);
            solarSystemIntegrationStatus = solarSystemIntegrationStatusRepository.save(solarSystemIntegrationStatus);
        }

        boolean solaredgeV1IntegrationExists = integrationRepository.existsByCode(Integration.CODE_SOLAREDGE_V1);
        if (!solaredgeV1IntegrationExists) {
            Integration solaredgeV1Integration = new Integration();
            solaredgeV1Integration.setCode(Integration.CODE_SOLAREDGE_V1);
            solaredgeV1Integration.setName("SolarEdge Site API Key");
            solaredgeV1Integration.setEnabled(true);
            solaredgeV1Integration = integrationRepository.save(solaredgeV1Integration);
        }

        boolean solaredgeV2IntegrationExists = integrationRepository.existsByCode(Integration.CODE_SOLAREDGE_V2);
        if (!solaredgeV2IntegrationExists) {
            Integration solaredgeV2Integration = new Integration();
            solaredgeV2Integration.setCode(Integration.CODE_SOLAREDGE_V2);
            solaredgeV2Integration.setName("SolarEdge Connect");
            solaredgeV2Integration.setEnabled(false);
            solaredgeV2Integration = integrationRepository.save(solaredgeV2Integration);

            IntegrationAuthCredential accountKey = new IntegrationAuthCredential();
            accountKey.setType(IntegrationAuthCredential.TYPE_ACCOUNT_KEY);
            accountKey.setIntegration(solaredgeV2Integration);
            accountKey = integrationAuthCredentialRepository.save(accountKey);
            IntegrationAuthCredential clientId = new IntegrationAuthCredential();
            clientId.setType(IntegrationAuthCredential.TYPE_CLIENT_ID);
            clientId.setIntegration(solaredgeV2Integration);
            clientId = integrationAuthCredentialRepository.save(clientId);
            IntegrationAuthCredential clientSecret = new IntegrationAuthCredential();
            clientSecret.setType(IntegrationAuthCredential.TYPE_CLIENT_SECRET);
            clientSecret.setIntegration(solaredgeV2Integration);
            clientSecret = integrationAuthCredentialRepository.save(clientSecret);
        }

        if (useSampleData) {
            // insert sample data
            EnergyPlanStatus publishedEnergyPlanStatus = energyPlanStatusRepository.findByCode(EnergyPlanStatus.CODE_PUBLISHED);

            List<EnergyPlan> energyPlans = energyPlanRepository.findAll();
            if (CollectionUtils.isEmpty(energyPlans)) {
                EnergyPlan energyPlan = new EnergyPlan();
                energyPlan.setName("Origin energy single rate");
                energyPlan.setExportRateValue(BigDecimal.ZERO);
                energyPlan.setSupplyRateValue(new BigDecimal("1.2575"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                EnergyPlanRate energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Single rate");
                energyPlanRate.setRateValue(new BigDecimal("0.2539"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                EnergyPlanRatePeriod energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Origin energy solar boost TOU 2020");
                energyPlan.setExportRateValue(new BigDecimal("0.21"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.5125"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.38159"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.35527"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.20706"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Origin energy solar boost TOU 2021");
                energyPlan.setExportRateValue(new BigDecimal("0.14"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.5539"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.3911"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.3643"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.2122"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Origin energy solar boost TOU 2022");
                energyPlan.setExportRateValue(new BigDecimal("0.14"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.75736"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.4422"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.41184"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.23991"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Powershop super solar 2022");
                energyPlan.setExportRateValue(new BigDecimal("0.13"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.4322"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.3795"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.3328"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.2496"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Powershop super solar 2023");
                energyPlan.setExportRateValue(new BigDecimal("0.12"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.9494"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.6079"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.4595"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.3309"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("OVO The One Plan 2023");
                energyPlan.setExportRateValue(new BigDecimal("0.07"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.5631"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.37532"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.34969"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.26026"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("OVO The One Plan 2024");
                energyPlan.setExportRateValue(new BigDecimal("0.03"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.903"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.4752"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.4268"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.3245"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Red Living Energy Saver 2024");
                energyPlan.setExportRateValue(new BigDecimal("0.05"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.45398"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.3795"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(9, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.3128"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(9, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.2448"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Red Living Energy Saver 2025");
                energyPlan.setExportRateValue(new BigDecimal("0.04"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.98"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.43945"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(9, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.38665"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(9, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.319"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlan = new EnergyPlan();
                energyPlan.setName("Red Living Energy Saver 2025 2");
                energyPlan.setExportRateValue(new BigDecimal("0.04"));
                energyPlan.setSupplyRateValue(new BigDecimal("1.45398"));
                energyPlan.setStatus(publishedEnergyPlanStatus);
                energyPlan = energyPlanRepository.save(energyPlan);

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Peak");
                energyPlanRate.setRateValue(new BigDecimal("0.41745"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(9, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Shoulder");
                energyPlanRate.setRateValue(new BigDecimal("0.33495"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(9, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(17, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(20, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }

                energyPlanRate = new EnergyPlanRate();
                energyPlanRate.setRateName("Off peak");
                energyPlanRate.setRateValue(new BigDecimal("0.26928"));
                energyPlanRate.setEnergyPlan(energyPlan);
                energyPlanRate = energyPlanRateRepository.save(energyPlanRate);
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(22, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(7, 0, 0));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
                energyPlanRatePeriod = new EnergyPlanRatePeriod();
                energyPlanRatePeriod.setStartTime(LocalTime.of(0, 0, 0));
                energyPlanRatePeriod.setEndTime(LocalTime.of(23, 59, 59));
                energyPlanRatePeriod.setEnergyPlanRate(energyPlanRate);
                energyPlanRatePeriod = energyPlanRatePeriodRepository.save(energyPlanRatePeriod);
                for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)) {
                    EnergyPlanRatePeriodDay energyPlanRatePeriodDay = new EnergyPlanRatePeriodDay();
                    energyPlanRatePeriodDay.setEnergyPlanRatePeriod(energyPlanRatePeriod);
                    energyPlanRatePeriodDay.setDayOfWeek(dayOfWeek);
                    energyPlanRatePeriodDay = energyPlanRatePeriodDayRepository.save(energyPlanRatePeriodDay);
                }
            }
        }

    }

}
