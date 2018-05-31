package com.mintfrost.weatherstation;

import java.util.List;

interface DateFetchListener {
    void notifyStart();
    void notifyComplete(List<ConditionSnapshot> result);
    void notifyError(String errorReason);
}
