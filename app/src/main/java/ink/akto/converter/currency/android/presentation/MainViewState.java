package ink.akto.converter.currency.android.presentation;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState;

/**
 * Created by Ruben on 04.09.2017.
 */

public class MainViewState implements IMainViewState
{
    private int fromPos;
    private int toPos;
    private double fromVal;
    private double toVal;
    @NonNull private List<IValutaCharacteristics> valutes;

    public MainViewState(@NonNull List<IValutaCharacteristics> valutes)
    {
        this.valutes = Collections.synchronizedList(valutes);
    }

    @Override
    public int getFromPos() {
        return fromPos;
    }

    @Override
    public void setFromPos(int pos) {
        fromPos = pos;
    }

    @Override
    public int getToPos() {
        return toPos;
    }

    @Override
    public void setToPos(int pos) {
        toPos = pos;
    }

    @Override
    public double getFromValue() {
        return fromVal;
    }

    @Override
    public void setFromValue(double val) {
        fromVal = val;
    }

    @Override
    public double getToValue() {
        return toVal;
    }

    @Override
    public void setToValue(double val) {
        toVal = val;
    }

    @NonNull
    @Override
    public List<IValutaCharacteristics> getValutasCharacteristics() {
        return valutes;
    }
}
