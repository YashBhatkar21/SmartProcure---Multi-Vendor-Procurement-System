package com.smartprocure.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartSeriesDTO<T> {
    private String label;
    private T value;

    public ChartSeriesDTO(String label, java.math.BigDecimal value) {
        this.label = label;
        this.value = (T) value;
    }

    public ChartSeriesDTO(String label, Double value) {
        this.label = label;
        this.value = (T) value;
    }

    public ChartSeriesDTO(String label, Long value) {
        this.label = label;
        this.value = (T) value;
    }

    public ChartSeriesDTO(String label, String value) {
        this.label = label;
        this.value = (T) value;
    }

    public ChartSeriesDTO(Object label, Object value) {
        this.label = String.valueOf(label);
        this.value = (T) value;
    }
}
