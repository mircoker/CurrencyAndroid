package ink.akto.converter.currency.android.presentation.views;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import ink.akto.converter.currency.core.domain.usecases.DefaultMainUseCase;
import ink.akto.converter.currency.core.repo.CBRGetCourseStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.repo.SerializationSaveStrategy;
import ink.akto.converter.currency.core.repo.models.MainModel;

public class MainActivity extends Activity implements IMainActivity
{
    @NonNull private IMainPresenter presenter;
    @NonNull private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);

        presenter = createPresenter(
                this,
                new ResourceManager(getResources()),
                new MainModel(
                        new CBRGetCourseStrategy(),
                        new SerializationSaveStrategy(getCacheDir())));

        if(savedInstanceState==null)
        {
            presenter.updateState();
        }
            else
        {
            if(presenter.getValutasCharacteristics().isEmpty())
            {
                presenter.updateState();
            }
                else
            {
                spinner.setVisibility(View.GONE);
//                replaceCardFragment();
            }
        }
    }

    /**
     * Создан исключительно ради тестов. Чтобы всегда получать относительно точную конфигурацию презентера.
     * @return IMainPresenter
     */
    public static IMainPresenter createPresenter(@NonNull IMainView view,
                                                 @NonNull IResourceManager resourceManager,
                                                 @NonNull IMainModel model)
    {
        return new MainPresenter(
            view,
            ConverterApplication.getEventBus(),
            model,
            resourceManager,
            ConverterApplication.getThreadsManager(),
            new DefaultMainUseCase());
    }

    private void replaceCardFragment()
    {
        spinner.setVisibility(View.GONE);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commit();
    }

    @Override
    public boolean notifyStateChanged()
    {
        if (!presenter.getValutasCharacteristics().isEmpty())
        {
            replaceCardFragment();
            MediaPlayer.create(this, R.raw.light).start();
        }

        return true;
    }

    @Override
    public boolean notifyError(@NonNull String error)
    {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        MediaPlayer.create(this, R.raw.sad).start();
        return true;
    }

    @NonNull
    @Override
    public IMainPresenter getPresenter() {
        return presenter;
    }
}