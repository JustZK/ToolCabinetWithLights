package com.zk.cabinet.callback;

import java.util.ArrayList;

public interface DoorListener {
    void openBoxResult(boolean openBoxResult);

    void checkBoxDoorState(int targetAddress, ArrayList<Integer> boxStateList);
}
