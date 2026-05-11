package org.backend.projection;

import java.time.LocalTime;

public interface NearBySalonsProjection {

    Long getSalonId();
    Long getPartnerId();
    String getSalonName();

    Double getLatitude();
    Double getLongitude();

    String getAddressLine1();
    String getAddressLine2();
    String getLandmark();
    String getCity();
    String getState();
    String getZipCode();
    String getCountry();

    String getWorkingDays();
    LocalTime getWorkingHoursStart();
    LocalTime getWorkingHoursEnd();

    Double getDistanceKm();
}