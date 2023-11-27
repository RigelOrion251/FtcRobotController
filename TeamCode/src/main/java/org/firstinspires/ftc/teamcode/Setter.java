package org.firstinspires.ftc.teamcode;

public class Setter {
    int position;
    Boolean resetCycleStart;
    int lastPosition;

    Setter(int _position, Boolean _resetCycleStart, int _lastPosition) {
        position = _position;
        resetCycleStart = _resetCycleStart;
        lastPosition = _lastPosition;
    }

    public void equalTo(Setter o) {
        o.set_Position(position);
        o.set_ResetCycleStart(resetCycleStart);
        o.set_LastPosition(lastPosition);
    }

    public int get_Position() {
        return position;
    }

    public Boolean get_ResetCycleStart() {
        return resetCycleStart;
    }

    public int get_LastPosition() {
        return lastPosition;
    }

    public void set_Position(int _position) {
        position = _position;
    }

    public void set_ResetCycleStart(Boolean _resetCycleStart) {
        resetCycleStart = _resetCycleStart;
    }

    public void set_LastPosition(int _lastPosition) {
        lastPosition = _lastPosition;
    }

}
