package com.zk.cabinet.callback;

import java.util.ArrayList;

public interface LightListener {
    void openLightResult(boolean openBoxResult);

    void checkLightState(int targetAddress, ArrayList<Integer> lightStateList);
}
