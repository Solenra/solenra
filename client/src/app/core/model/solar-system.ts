export interface SolarSystem {

  id: number;
  name: string;
  notes: string;
  outlayCost: number;
  cumulativeSupplyCost: number;
  cumulativeImportCost: number;
  cumulativeExportRevenue: number;
  cumulativeSelfConsumptionSavings: number;
  calculatedSavings: number;
  calculatedAt: Date;
  roiToDate: number;
  roiAnnualised: number;
  paybackPeriod: number;
  breakEvenDate: Date;
  energyPlans: EnergyPlan[];
  solarSystemIntegrations: SolarSystemIntegration[];

}

export interface SolarSystemIntegration {

  id: number;
  enabled: boolean;
  integration: Integration;
  systemDetails: SystemDetails;
  systemEnergyDetails: SystemEnergyDetails[];
  status: Status
  latestLoadedDate: Date;

}

export interface Integration {

  id: number;
  code: string;
  name: string;
  enabled: boolean;
  credentials: IntegrationCredential[];

}

export interface IntegrationCredential {

  type: string;
  value: string;

}

export interface SystemDetails {

  id: number;
  status: string;
  peakPower: string;
  lastUpdateTime: Date;
  installationDate: Date;
  notes: string;
  apiKey: string;
  systemId: string;

}

export interface SystemEnergyDetails {

  id: number;
  processingStatus: string;
  timeUnit: string;
  unit: string;
  startDate: Date;
  endDate: Date;
  productionValue: number;
  importValue: number;
  exportValue: number;
  consumptionValue: number;
  selfConsumptionValue: number;

}

export interface Status {
  code: string;
  name: string;
  description: string;
  displayOrder: number | null;
  autoReload: boolean;
}

export interface EnergyPlan {
  id: number
  energyPlan: EnergyPlan2
  startDate: string
  endDate?: string
  includeInRevenueCalculation: boolean
  cumulativeSupplyCost: number
  cumulativeImportCost: number
  cumulativeExportRevenue: number
  cumulativeSelfConsumptionSavings: number
  calculatedSavings: number
  roiAnnualised: number
  calculatedAt: any
}

export interface EnergyPlan2 {
  id: number
  name: string
  notes: any
  shared: any
  supplyRateValue: number
  exportRateValue: number
  energyPlanRates: EnergyPlanRate[]
}

export interface EnergyPlanRate {
  id: number
  rateName: string
  rateValue: number
  comparativeRateValue: any
  energyPlanRatePeriods: EnergyPlanRatePeriod[]
}

export interface EnergyPlanRatePeriod {
  id: number
  daysOfWeek: DaysOfWeek[]
  startTime: string
  endTime: string
}

export interface DaysOfWeek {
  id: number
  dayOfWeek: string
}
