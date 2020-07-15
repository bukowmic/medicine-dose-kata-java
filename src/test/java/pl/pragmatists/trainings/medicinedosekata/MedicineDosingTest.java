package pl.pragmatists.trainings.medicinedosekata;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.pragmatists.trainings.medicinedosekata.dependencies.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineDosingTest {
    @Mock
    private HealthMonitor healthMonitor;
    @Mock
    private MedicinePump medicinePump;
    @Mock
    private AlertService alertService;
    @InjectMocks
    DoseController controller;

    @Test
    void should_dose_medicine_when_blood_pressure_below_90() {
        when(medicinePump.getTimeSinceLastDoseInMinutes(any())).thenReturn(31);
        when(healthMonitor.getSystolicBloodPressure()).thenReturn(89);

        controller.checkHealthAndApplyMedicine();

        verify(medicinePump).dose(Medicine.PRESSURE_RAISING_MEDICINE);
    }

    @Test
    void should_no_dose_pressure_raising_medicine_when_blood_pressure_above_90() {
        when(healthMonitor.getSystolicBloodPressure()).thenReturn(91);

        controller.checkHealthAndApplyMedicine();

        verify(medicinePump, never()).dose(Medicine.PRESSURE_RAISING_MEDICINE);
    }

    @Test
    void should_dose_two_pressure_raising_medicine_when_blood_pressure_below_60() {
        when(medicinePump.getTimeSinceLastDoseInMinutes(any())).thenReturn(31);
        when(healthMonitor.getSystolicBloodPressure()).thenReturn(59);

        controller.checkHealthAndApplyMedicine();

        verify(medicinePump, times(2)).dose(Medicine.PRESSURE_RAISING_MEDICINE);
    }

    @Test
    void should_dose_pressure_lowering_medicine_when_blood_pressure_above_150() {
        when(medicinePump.getTimeSinceLastDoseInMinutes(any())).thenReturn(31);
        when(healthMonitor.getSystolicBloodPressure()).thenReturn(151);

        controller.checkHealthAndApplyMedicine();

        verify(medicinePump).dose(Medicine.PRESSURE_LOWERING_MEDICINE);
    }

    @Test
    void should_try_another_dose_medicine_when_pump_is_not_working() {
        when(medicinePump.getTimeSinceLastDoseInMinutes(any())).thenReturn(31);
        when(healthMonitor.getSystolicBloodPressure()).thenReturn(89);
        doThrow(DoseUnsuccessfulException.class).doNothing().when(medicinePump).dose(any());

        controller.checkHealthAndApplyMedicine();

        verify(medicinePump, times(2)).dose(any());
    }

    @Test
    void should_no_dose_medicine_when_last_dose_was_earlier_than_30min_ago() {
        when(healthMonitor.getSystolicBloodPressure()).thenReturn(89);
        when(medicinePump.getTimeSinceLastDoseInMinutes(Medicine.PRESSURE_RAISING_MEDICINE)).thenReturn(20);

        controller.checkHealthAndApplyMedicine();

        verify(medicinePump, never()).dose(Medicine.PRESSURE_RAISING_MEDICINE);
    }

    @Test
    void should_send_alarm_and_dose_three_pressure_raising_medicine_when_blood_pressure_below_55() {
        when(medicinePump.getTimeSinceLastDoseInMinutes(any())).thenReturn(31);
        when(healthMonitor.getSystolicBloodPressure()).thenReturn(54);

        controller.checkHealthAndApplyMedicine();

        verify(alertService).notifyDoctor();
        verify(medicinePump, times(3)).dose(Medicine.PRESSURE_RAISING_MEDICINE);
    }
}
