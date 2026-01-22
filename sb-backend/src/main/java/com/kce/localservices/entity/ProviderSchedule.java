package com.kce.localservices.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Time;

@Data
@Entity
@Table(name = "provider_schedules")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    @JsonProperty("provider_id")
    private Integer providerId;

    @Column(name = "day_of_week", nullable = false)
    @JsonProperty("day_of_week")
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    @JsonProperty("start_time")
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    @JsonProperty("end_time")
    private Time endTime;

    @Column(name = "is_available")
    @JsonProperty("is_available")
    private Boolean isAvailable = true;
}
