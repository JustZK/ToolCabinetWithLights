package com.zk.cabinet.callback;

import com.zk.cabinet.bean.User;

public interface FingerprintVerifyListener {
    void fingerprintVerify(boolean result, User user);
}

