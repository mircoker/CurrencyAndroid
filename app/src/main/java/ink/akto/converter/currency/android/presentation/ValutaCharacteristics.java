package ink.akto.converter.currency.android.presentation;

import android.support.annotation.NonNull;

import ink.akto.converter.currency.android.AndroidContracts.IMainView.IValutaCharacteristics;


/**
 * Created by Ruben on 31.08.2017.
 */

public class ValutaCharacteristics implements IValutaCharacteristics
{
    @NonNull private String charCode;
    @NonNull private String name;

    public ValutaCharacteristics(@NonNull String charCode, @NonNull String name)
    {
        this.charCode = charCode;
        this.name = name;
    }

    @NonNull
    @Override
    public String getCharCode() {
        return charCode;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
