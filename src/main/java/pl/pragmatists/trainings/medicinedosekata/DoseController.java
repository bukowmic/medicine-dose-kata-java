package pl.pragmatists.trainings.medicinedosekata;

import pl.pragmatists.trainings.medicinedosekata.dependencies.*;

public class DoseController {

    private final HealthMonitor healthMonitor;
    private final MedicinePump medicinePump;
    private final AlertService alertService;

    public DoseController(HealthMonitor healthMonitor, MedicinePump medicinePump, AlertService alertService) {
        this.healthMonitor = healthMonitor;
        this.medicinePump = medicinePump;
        this.alertService = alertService;
    }

    public void checkHealthAndApplyMedicine() {
        int systolicBloodPressure = healthMonitor.getSystolicBloodPressure();
        if (alarmingLowPressure(systolicBloodPressure)) {
            alertService.notifyDoctor();
            raisePressure();
        }
        if (veryLow(systolicBloodPressure)) {
            raisePressure();
        }
        if (lowPressure(systolicBloodPressure)) {
            raisePressure();
        }
        if (highPressure(systolicBloodPressure)) {
            lowPressure();
        }
    }

    private boolean alarmingLowPressure(int systolicBloodPressure) {
        return systolicBloodPressure < 55;
    }

    private boolean highPressure(int systolicBloodPressure) {
        return systolicBloodPressure > 150;
    }

    private boolean lowPressure(int systolicBloodPressure) {
        return systolicBloodPressure < 90;
    }

    private boolean veryLow(int systolicBloodPressure) {
        return systolicBloodPressure < 60;
    }

    private void lowPressure() {
        tryDose(Medicine.PRESSURE_LOWERING_MEDICINE);
    }

    private void raisePressure() {
        tryDose(Medicine.PRESSURE_RAISING_MEDICINE);
    }

    private void tryDose(Medicine medicine) {
        if (canDoseMedicine(medicine)) {
            try {
                medicinePump.dose(medicine);
            } catch (DoseUnsuccessfulException exception) {
                medicinePump.dose(medicine);
            }
        }
    }

    private boolean canDoseMedicine(Medicine medicine) {
        int timeSinceLastDoseInMinutes = medicinePump.getTimeSinceLastDoseInMinutes(medicine);
        return timeSinceLastDoseInMinutes > 30;
    }
}
