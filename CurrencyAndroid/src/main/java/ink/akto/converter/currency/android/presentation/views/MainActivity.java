package ink.akto.converter.currency.android.presentation.views;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import ink.akto.converter.currency.android.AndroidContracts.IMainActivity;
import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.AndroidContracts.IMainView;
import ink.akto.converter.currency.android.AndroidContracts.IResourceManager;
import ink.akto.converter.currency.android.AndroidContracts.IUpdateBinder;
import ink.akto.converter.currency.android.AndroidContracts.IUpdateServiceConnection;
import ink.akto.converter.currency.android.ConverterApplication;
import ink.akto.converter.currency.android.R;
import ink.akto.converter.currency.android.ResourceManager;
import ink.akto.converter.currency.android.presentation.presenters.MainPresenter;
import ink.akto.converter.currency.android.repo.AndroidCBRGetCourceStrategy;
import ink.akto.converter.currency.android.services.UpdateService;
import ink.akto.converter.currency.core.domain.usecases.DefaultMainUseCase;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.repo.SerializationSaveStrategy;
import ink.akto.converter.currency.core.repo.models.MainModel;
import ink.akto.converter.currency.core.utils.CallbackWithEntity;

public class MainActivity extends Activity implements IMainActivity
{
    @NonNull private Intent updateServiceIntent;
    @NonNull private UpdateServiceConnection updateServiceConnection;
    @NonNull private IMainPresenter presenter;

    @NonNull Handler handler;

    @NonNull private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();

        spinner = findViewById(R.id.spinner);

        updateServiceConnection = new UpdateServiceConnection();

        presenter = createPresenter(
                this,
                new ResourceManager(getResources()),
                new MainModel(
                        new AndroidCBRGetCourceStrategy(updateServiceConnection),
                        new SerializationSaveStrategy(getCacheDir())));

        updateServiceIntent = new Intent(this, UpdateService.class);

        if(savedInstanceState==null)
        {
            startService(updateServiceIntent);
        }
            else
        {
            if(presenter.getCashedValutas().isEmpty())
            {
                presenter.updateState();
            }
                else
            {
                addCardFragment();
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

    @Override
    protected void onResume()
    {
        super.onResume();
        bindService(updateServiceIntent, updateServiceConnection, BIND_IMPORTANT);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unbindService(updateServiceConnection);
    }

    private void addCardFragment()
    {
        spinner.setVisibility(View.GONE);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.bot, R.animator.top)
                .replace(R.id.container, MainFragment.newInstance()).commit();
    }

    @Override
    public boolean notifyStateChanged()
    {
        if (!presenter.getCashedValutas().isEmpty())
        {
            addCardFragment();
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

    private static class UpdateServiceConnection implements IUpdateServiceConnection
    {
        @Nullable private IMainModel model;
        @Nullable private IUpdateBinder binder;
        @Nullable private CallbackWithEntity<IMainModel> callback;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder)
        {
            model = ((IUpdateBinder)binder).getModel();
            this.binder = (IUpdateBinder)binder;
            if(callback!=null)callback.onCallback(model);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }

        @Override
        public void onBindingDied(ComponentName name) {

        }

        @Override
        public void setModel(@NonNull IMainModel model) {
            this.model = model;
        }

        @Override
        public void setOnBindListener(CallbackWithEntity<IMainModel> callback)
        {
            this.callback = callback;
        }

        @Nullable
        @Override
        public IUpdateBinder getBinder() {
            return binder;
        }
    }
}