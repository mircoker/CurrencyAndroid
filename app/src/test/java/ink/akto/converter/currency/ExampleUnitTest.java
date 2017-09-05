package ink.akto.converter.currency;

import android.support.annotation.NonNull;

import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import ink.akto.converter.currency.android.AndroidContracts;
import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.presentation.views.MainActivity;
import ink.akto.converter.currency.core.repo.CBRGetCourseStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts;
import ink.akto.converter.currency.core.repo.RepoContracts.IValuta;
import ink.akto.converter.currency.core.repo.SerializationSaveStrategy;
import ink.akto.converter.currency.core.repo.models.MainModel;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void addition_isCorrect() throws Exception
    {
        IMainPresenter presenter =
            MainActivity.createPresenter(
                new AndroidContracts.IMainView()
                {
                    @Override
                    public boolean notifyStateChanged() {
                        return false;
                    }

                    @Override
                    public boolean notifyError(@NonNull String error) {
                        return false;
                    }
                },
                new SerializationSaveStrategy(new File(".")),
                () -> "",
                new MainModel(new CBRGetCourseStrategy(), new RepoContracts.ISaveStrategy<List<IValuta>, String>()
                {
                    private SerializationSaveStrategy saveStrategy = new SerializationSaveStrategy(new File("."));

                    @Override
                    public void save(List<IValuta> valutas, String s) throws Exception {
                        saveStrategy.save((Serializable) valutas, s);
                    }

                    @Override
                    public List<IValuta> restore(String s) throws Exception {
                        return (List<IValuta>) saveStrategy.restore(s);
                    }
                }));

        presenter.updateState();
    }
}