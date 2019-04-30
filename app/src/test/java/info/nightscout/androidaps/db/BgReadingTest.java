package info.nightscout.androidaps.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.logging.Logger;

import info.AAPSMocker;
import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.logging.L;
import info.nightscout.androidaps.plugins.iob.iobCobCalculator.GlucoseStatus;
import info.nightscout.androidaps.utils.SP;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.doReturn;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MainApp.class, Logger.class, L.class, SP.class})
public class BgReadingTest {
    private BgReading bgReading = new BgReading();
    @Mock
    GlucoseStatus glucoseStatus;

    @Test
    public void valueToUnits() {
        bgReading.value = 18;
        assertEquals(18, bgReading.valueToUnits(Constants.MGDL)*1, 0.01d);
        assertEquals(1, bgReading.valueToUnits(Constants.MMOL)*1, 0.01d);
    }

    @Test
    public void directionToSymbol() {
        bgReading = new BgReading();
        bgReading.direction = "DoubleDown";
        assertEquals("\u21ca", bgReading.directionToSymbol());
        bgReading.direction = "SingleDown";
        assertEquals("\u2193", bgReading.directionToSymbol());
        bgReading.direction = "FortyFiveDown";
        assertEquals("\u2198", bgReading.directionToSymbol());
        bgReading.direction = "Flat";
        assertEquals("\u2192", bgReading.directionToSymbol());
        bgReading.direction = "FortyFiveUp";
        assertEquals("\u2197", bgReading.directionToSymbol());
        bgReading.direction = "SingleUp";
        assertEquals("\u2191", bgReading.directionToSymbol());
        bgReading.direction = "DoubleUp";
        assertEquals("\u21c8", bgReading.directionToSymbol());
        bgReading.direction = "OUT OF RANGE";
        assertEquals("??", bgReading.directionToSymbol());

    }

    @Test
    public void dateTest(){
        bgReading = new BgReading();
        long now = System.currentTimeMillis();
        bgReading.date = now;
        Date nowDate = new Date(now);
        assertEquals(now, bgReading.date(now).date);
        assertEquals(now, bgReading.date(nowDate).date);
    }

    @Test
    public void valueTest(){
        bgReading = new BgReading();
        double valueToSet = 81; // 4.5 mmol
        assertEquals(81d, bgReading.value(valueToSet).value, 0.01d);
    }

    @Test
    public void copyFromTest(){
        bgReading = new BgReading();
        BgReading copy = new BgReading();
        bgReading.value = 81;
        long now = System.currentTimeMillis();
        bgReading.date = now;
        copy.date = now;

        copy.copyFrom(bgReading);

        assertEquals(81, copy.value, 0.1d);
        assertEquals(now, copy.date);
        assertEquals(bgReading.directionToSymbol(), copy.directionToSymbol());
    }

    @Test
    public void isEqualTest(){
        bgReading = new BgReading();
        BgReading copy = new BgReading();
        bgReading.value = 81;
        long now = System.currentTimeMillis();
        bgReading.date = now;
        copy.date = now;

        copy.copyFrom(bgReading);

        assertEquals(true, copy.isEqual(bgReading));
        assertEquals(false, copy.isEqual(new BgReading()));
    }

    @Test
    public void calculateDirection() throws Exception {
        assertEquals("??", bgReading.calculateDirection());

        bgReading = new BgReading();
        glucoseStatus = new GlucoseStatus();
        glucoseStatus.glucose = 0;
        glucoseStatus.prev_glucose = 0;
        glucoseStatus.date = 1000L * 60 * 12;;
        glucoseStatus.previous_date = 1000L * 60 * 6;
        BgReading newReading = Mockito.spy(new BgReading());
        doReturn(glucoseStatus).when(newReading).getGlucoseStatus();
        assertEquals("??", newReading.calculateDirection());
        glucoseStatus.glucose = 72;
        glucoseStatus.prev_glucose = 10;
        assertEquals("DoubleUp", newReading.calculateDirection());
        glucoseStatus.glucose = 72;
        glucoseStatus.prev_glucose = 55;
        assertEquals("SingleUp", newReading.calculateDirection());
        glucoseStatus.glucose = 72;
        glucoseStatus.prev_glucose = 65;
        assertEquals("FortyFiveUp", newReading.calculateDirection());
        glucoseStatus.glucose = 72;
        glucoseStatus.prev_glucose = 70;
        assertEquals("Flat", newReading.calculateDirection());
        glucoseStatus.glucose = 10;
        glucoseStatus.prev_glucose = 72;
        assertEquals("DoubleDown", newReading.calculateDirection());
        glucoseStatus.glucose = 55;
        glucoseStatus.prev_glucose = 72;
        assertEquals("SingleDown", newReading.calculateDirection());
        glucoseStatus.glucose = 65;
        glucoseStatus.prev_glucose = 72;
        assertEquals("FortyFiveDown", newReading.calculateDirection());



    }

    @Before
    public void prepareMock() {
        AAPSMocker.mockMainApp();
        AAPSMocker.mockApplicationContext();
        AAPSMocker.mockSP();
        AAPSMocker.mockL();
    }
}