package ink.akto.converter.currency.android.repo;

import android.support.annotation.NonNull;

import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState;
import ink.akto.converter.currency.core.repo.RepoContracts.ISaveStrategy;
import ink.akto.converter.currency.core.repo.SerializationSaveStrategy;

/**
 * Created by Ruben on 05.09.2017.
 */

public class MainViewStateSerializationSaveStrategy implements ISaveStrategy<IMainViewState, String>
{
    private @NonNull SerializationSaveStrategy strategy;

    public MainViewStateSerializationSaveStrategy(@NonNull SerializationSaveStrategy strategy)
    {
        this.strategy = strategy;
    }

    @Override
    public void save(IMainViewState state, String s) throws Exception {
        strategy.save(state, s);
    }

    @Override
    public IMainViewState restore(String s) throws Exception {
        return (IMainViewState) strategy.restore(s);
    }
}
