package ink.akto.converter.currency.android.presentation.repo;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ink.akto.converter.currency.core.repo.RepoContracts;
import ink.akto.converter.currency.core.repo.RepoContracts.IValuta;

/**
 * Created by Ruben on 03.09.2017.
 */

public class BundleCashStrategy implements RepoContracts.IRuntimeCashStrategy<List<IValuta>, String>
{
    @NonNull private Bundle bundle;

    public BundleCashStrategy(@NonNull Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public void save(List<IValuta> valutas, String s)
    {
        bundle.putSerializable(s, (Serializable) valutas);
        System.out.println("SAVED IN BUNDLE");
    }

    @NonNull
    @Override
    public List<IValuta> restore(@NonNull String s)
    {
        List<IValuta> valutas = (List<IValuta>) bundle.getSerializable(s);
        if(valutas==null)valutas = new ArrayList<>();
        System.out.println("RESTORED FROM BUNDLE");
        return valutas;
    }
}
