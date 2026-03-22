package com.kce.localservices.service;

import com.kce.localservices.entity.Booking;
import com.kce.localservices.entity.ProviderSchedule;
import com.kce.localservices.entity.User;
import com.kce.localservices.repository.BookingRepository;
import com.kce.localservices.repository.ProviderScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private ProviderScheduleRepository scheduleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    public List<ProviderSchedule> getSchedule() {
        User user = userService.getCurrentUser();
        return scheduleRepository.findByProviderId(user.getId());
    }

    @Transactional
    public void updateSchedule(List<ProviderSchedule> schedules) {
        User user = userService.getCurrentUser();

        // Delete existing schedules and flush to ensure deletion completes
        scheduleRepository.deleteByProviderId(user.getId());
        scheduleRepository.flush();

        // Insert new schedules
        for (ProviderSchedule s : schedules) {
            if (Boolean.TRUE.equals(s.getIsAvailable())) {
                s.setProviderId(user.getId());
                // Ensure ID is null so it creates new
                s.setId(null);
                scheduleRepository.save(s);
            }
        }
    }

    public List<String> getAvailableSlots(Integer providerId, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // Java Calendar Sunday=1, JS Date Sunday=0. Adjust if
                                                           // needed.
        // JS getDay(): 0=Sunday, 1=Monday.
        // Java Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday.
        // So Java = JS + 1. Or JS = Java - 1.
        // DB stores JS convention likely if inserted from JS.
        // Let's assume stored as 0-6.

        List<ProviderSchedule> schedules = scheduleRepository.findByProviderIdAndDayOfWeekAndIsAvailableTrue(providerId,
                dayOfWeek);

        if (schedules.isEmpty()) {
            return new ArrayList<>();
        }

        ProviderSchedule schedule = schedules.get(0);

        // Fetch existing bookings for that date
        // Need to query bookings by DATE part.
        // We will assume 'date' param is at 00:00:00 correctly set by controller or
        // formatted string.

        // Create range for query
        Calendar startOfDay = (Calendar) cal.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);

        Calendar endOfDay = (Calendar) cal.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);

        List<Booking> bookings = bookingRepository.findByProviderIdAndBookingStartTimeBetween(providerId,
                startOfDay.getTime(), endOfDay.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat slotFormat = new SimpleDateFormat("HH:mm");

        Set<String> bookedTimes = bookings.stream()
                .map(b -> timeFormat.format(b.getBookingStartTime()))
                .collect(Collectors.toSet());

        List<String> availableSlots = new ArrayList<>();
        int serviceDurationHours = 1;

        // Parse start and end times
        // schedule.getStartTime() returns java.sql.Time.
        // Combine date with time.

        Calendar currentSlot = (Calendar) cal.clone();
        Date startTimeTime = schedule.getStartTime();
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startTimeTime);
        currentSlot.set(Calendar.HOUR_OF_DAY, startCal.get(Calendar.HOUR_OF_DAY));
        currentSlot.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
        currentSlot.set(Calendar.SECOND, startCal.get(Calendar.SECOND));

        Date endTimeTime = schedule.getEndTime();
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endTimeTime);
        Calendar endTimeSlot = (Calendar) cal.clone();
        endTimeSlot.set(Calendar.HOUR_OF_DAY, endCal.get(Calendar.HOUR_OF_DAY));
        endTimeSlot.set(Calendar.MINUTE, endCal.get(Calendar.MINUTE));
        endTimeSlot.set(Calendar.SECOND, endCal.get(Calendar.SECOND));

        while (currentSlot.getTime().before(endTimeSlot.getTime())) {
            String timeStr = timeFormat.format(currentSlot.getTime());
            if (!bookedTimes.contains(timeStr)) {
                availableSlots.add(slotFormat.format(currentSlot.getTime()));
            }
            currentSlot.add(Calendar.HOUR_OF_DAY, serviceDurationHours);
        }

        return availableSlots;
    }
}
