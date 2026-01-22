package com.kce.localservices.repository;

import com.kce.localservices.entity.ProviderSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProviderScheduleRepository extends JpaRepository<ProviderSchedule, Long> {
    List<ProviderSchedule> findByProviderId(Integer providerId);

    void deleteByProviderId(Integer providerId);

    List<ProviderSchedule> findByProviderIdAndDayOfWeekAndIsAvailableTrue(Integer providerId, Integer dayOfWeek);
}
