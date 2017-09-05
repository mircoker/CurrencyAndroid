package ink.akto.converter.currency.android.presentation.views;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import ink.akto.converter.currency.android.AndroidContracts.IMainActivity;
import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.AndroidContracts.IMainView;
import ink.akto.converter.currency.android.AndroidContracts.IResourceManager;
import ink.akto.converter.currency.android.ConverterApplication;
import ink.akto.converter.currency.android.R;
import ink.akto.converter.currency.android.ResourceManager;
import ink.akto.converter.currency.android.presentation.presenters.MainPresenter;
import ink.akto.converter.currency.android.repo.MainViewStateSerializationSaveStrategy;
import ink.akto.converter.currency.core.CoreContracts.IThreadsManager;
import ink.akto.converter.currency.core.domain.usecases.DefaultMainUseCase;
import ink.akto.converter.currency.core.repo.CBRGetCourseStrategy;
import ink.akto.converter.currency.core.repo.ListIValutasSerializationSaveStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.repo.RepoContracts.ISaveStrategy;
import ink.akto.converter.currency.core.repo.SerializationSaveStrategy;
import ink.akto.converter.currency.core.repo.models.MainModel;

public class MainActivity extends Activity implements IMainActivity
{
    @NonNull private IMainPresenter presenter;
    @Nullable private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);

        presenter = createPresenter(
                this,
                new MainViewStateSerializationSaveStrategy(new SerializationSaveStrategy(getCodeCacheDir())),
                new ResourceManager(getResources()),
                ConverterApplication.getThreadsManager(),
                new MainModel(
                        new CBRGetCourseStrategy(),
                        new ListIValutasSerializationSaveStrategy(new SerializationSaveStrategy(getCacheDir()))));

        if(savedInstanceState==null || presenter.isNeedUpdate())
        {
            presenter.updateState();
        }
            else
        {
            spinner.setVisibility(View.GONE);
        }
    }

    /**
     * Создан исключительно ради тестов. Чтобы всегда получать относительно конфигурацию презентера для инициализации.
     * @return IMainPresenter
     */
    public static IMainPresenter createPresenter(@NonNull IMainView view,
                                                 @NonNull ISaveStrategy<IMainViewState, String> saveStateStrategy,
                                                 @NonNull IResourceManager resourceManager,
                                                 @NonNull IThreadsManager threadsManager,
                                                 @NonNull IMainModel model)
    {
        return new MainPresenter(
            view,
            saveStateStrategy,
            model,
            resourceManager,
            threadsManager,
            new DefaultMainUseCase());
    }

    private void replaceCardFragment()
    {
        spinner.setVisibility(View.GONE);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitAllowingStateLoss();
    }

    @Override
    public boolean notifyStateChanged()
    {
        if (spinner!=null && !presenter.getState().getValutasCharacteristics().isEmpty())
        {
            replaceCardFragment();
            spinner.setVisibility(View.GONE);
            MediaPlayer.create(this, R.raw.light).start();
        }

        return true;
    }

    @Override
    public boolean notifyError(@NonNull String error)
    {
        if(spinner!=null)
        {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            MediaPlayer.create(this, R.raw.sad).start();
            spinner.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public IMainPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.saveState();
    }
}