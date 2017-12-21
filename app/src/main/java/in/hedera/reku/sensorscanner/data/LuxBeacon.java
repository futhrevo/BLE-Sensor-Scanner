package in.hedera.reku.sensorscanner.data;

import org.altbeacon.beacon.Beacon;

import java.util.List;

/**
 * Created by rakeshkalyankar on 17/12/17.
 */

public class LuxBeacon extends Beacon {

    private Long lux;

    public Long getLux() {
        return lux;
    }

    public void setLux(Long lux) {
        this.lux = lux;
    }

    public Long getTemperature() {
        return temperature;
    }

    public void setTemperature(Long temperature) {
        this.temperature = temperature;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public Long getAttime() {
        return attime;
    }

    public void setAttime(Long attime) {
        this.attime = attime;
    }

    public boolean isActive(){
        return System.currentTimeMillis() - attime < 1000;
    }

    private Long temperature;
    private Beacon beacon;
    private Long attime;

    public LuxBeacon(Beacon beacon) {
        this.beacon = beacon;
        List<Long> data = beacon.getDataFields();
        lux = data.get(0);
        temperature = data.get(1);
        attime = System.currentTimeMillis();
    }

}
