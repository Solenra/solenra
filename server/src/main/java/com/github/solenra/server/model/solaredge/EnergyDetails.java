package com.github.solenra.server.model.solaredge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnergyDetails {

    public String timeUnit;
    public String unit;
    public ArrayList<Meter> meters;

    @Override
    public String toString() {
        return "EnergyDetails{" +
                "timeUnit='" + timeUnit + '\'' +
                ", unit='" + unit + '\'' +
                ", meters=" + meters +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meter {
        public String type;
        public ArrayList<Value> values;

        @Override
        public String toString() {
            return "Meter{" +
                    "type='" + type + '\'' +
                    ", values=" + values +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Root {
        public EnergyDetails energyDetails;

        @Override
        public String toString() {
            return "Root{" +
                    "energyDetails=" + energyDetails +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        public String date;

        public BigDecimal value;

        @Override
        public String toString() {
            return "Value{" +
                    "date='" + date + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

}

