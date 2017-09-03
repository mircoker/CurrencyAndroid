package ink.akto.converter.currency.android.presentation.views;

import android.app.Activity;
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
import ink.akto.converter.currency.android.presentation.repo.BundleCashStrategy;
import ink.akto.converter.currency.core.domain.usecases.DefaultMainUseCase;
import ink.akto.converter.currency.core.repo.CBRGetCourseStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.repo.SerializationSaveStrategy;
import ink.akto.converter.currency.core.repo.models.MainModel;

public class MainActivity extends Activity implements IMainActivity
{
    @NonNull private IMainPresenter presenter;
    @NonNull private Bundle bundle;
    @NonNull private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);

        if(savedInstanceState==null)
        {
            bundle = new Bundle();
        }
            else
        {
            bundle = savedInstanceState;
        }

        presenter = createPresenter(
                this,
                new ResourceManager(getResources()),
                new MainModel(
                        new CBRGetCourseStrategy(),
                        new SerializationSaveStrategy(getCacheDir()),
                        new BundleCashStrategy(bundle)));

        if(savedInstanceState==null)
        {
            presenter.updateState();
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
                .setCustomAnimations(R.animator.bot, R.animator.top)
                .replace(R.id.container, MainFragment.newInstance())
                .commit();
    }

    @Override
    public boolean notifyStateChanged()
    {
        if (!presenter.getCashedValutas().isEmpty())
        {
            replaceCardFragment();
        }
            else
        {
            spinner.setVisibility(View.VISIBLE);
        }

        return true;
    }

    @Override
    public boolean notifyError(@NonNull String error)
    {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        return true;
    }

    @NonNull
    @Override
    public IMainPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putAll(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        bundle.putAll(savedInstanceState);
    }
}