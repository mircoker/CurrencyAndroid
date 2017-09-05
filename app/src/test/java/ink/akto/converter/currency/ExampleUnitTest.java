package ink.akto.converter.currency;

import android.support.annotation.NonNull;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.AndroidContracts.IMainView;
import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState;
import ink.akto.converter.currency.android.presentation.MainViewState;
import ink.akto.converter.currency.android.presentation.views.MainActivity;
import ink.akto.converter.currency.core.CoreContracts.IMainTreadExecutor;
import ink.akto.converter.currency.core.ThreadsManager;
import ink.akto.converter.currency.core.domain.usecases.DefaultMainUseCase;
import ink.akto.converter.currency.core.repo.CBRGetCourseStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.ISaveStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.IValuta;
import ink.akto.converter.currency.core.repo.Valuta;
import ink.akto.converter.currency.core.repo.models.MainModel;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void testDefaultMainUseCase() throws Exception
    {
        DefaultMainUseCase useCase = new DefaultMainUseCase();

        IValuta mainValuta = new Valuta(0, "bitboxCoin", 1, "yo-yo-yo, it bitbox coin brother! Bumbfff!", 1, 0);
        IValuta firstValuta = new Valuta(1, "firstCoin", 1, "U can buy first sex on it", 5000, 0);
        IValuta secondValuta = new Valuta(2, "secondHandCoin", 1, "Remember to wash it before use", 5, 0);

        double result = useCase.convertValuta(1, firstValuta, secondValuta);
        Assert.assertTrue(String.valueOf(result), result==1000);
    }


    private volatile boolean stateChanged;
    @Test
    public void testMainPresenter() throws Exception
    {
        class Task
        {
            long time;
            Runnable runnable;

            Task(long time, Runnable runnable) {
                this.time = time;
                this.runnable = runnable;
            }
        }
        ArrayList<Task> pendingTasksQueue = new ArrayList<>();

        IMainView mainView = new IMainView() {
            @Override
            public boolean notifyStateChanged() {
                stateChanged = true;
                return true;
            }

            @Override
            public boolean notifyError(@NonNull String error) {
                return true;
            }
        };

        IMainPresenter presenter = MainActivity.createPresenter(
                mainView,
                new ISaveStrategy<IMainViewState, String>() {
                    private Map<String, IMainViewState> map = new HashMap<>();

                    @Override
                    public void save(IMainViewState o, String o2) throws Exception {
                        map.put(o2, o);
                    }

                    @NonNull
                    @Override
                    public IMainViewState restore(String o) throws Exception {
                        return map.get(o) != null ? map.get(o) : new MainViewState(new ArrayList<>());
                    }
                },
                () -> "",
                ThreadsManager.init(new IMainTreadExecutor() {
                    @Override
                    public void execute(@NonNull Runnable runnable) {
                        pendingTasksQueue.add(new Task(0L, runnable));
                    }

                    @Override
                    public void executeDelayed(@NonNull Runnable runnable, long l) {
                        pendingTasksQueue.add(new Task(System.nanoTime() + l, runnable));
                    }
                }),
                new MainModel(new CBRGetCourseStrategy(), new ISaveStrategy<List<IValuta>, String>() {
                    private Map<Object, Object> map = new HashMap();

                    @Override
                    public void save(List<IValuta> valutas, String s) throws Exception {
                        map.put(valutas, s);
                    }

                    @NonNull
                    @Override
                    public List<IValuta> restore(String s) throws Exception {
                        return map.get(s) != null ? (List<IValuta>) map.get(s) : new ArrayList<>();
                    }
                }));

        presenter.updateState();

        while(true)
        {
            for (int i = 0; i < pendingTasksQueue.size(); i++)
            {
                if(pendingTasksQueue.get(i).time<System.nanoTime())
                {
                    pendingTasksQueue.get(i).runnable.run();
                    pendingTasksQueue.remove(i);
                    i--;
                }
            }

            if(stateChanged && pendingTasksQueue.isEmpty()) break;
        }

        Assert.assertFalse(presenter.isNeedUpdate());
    }
}