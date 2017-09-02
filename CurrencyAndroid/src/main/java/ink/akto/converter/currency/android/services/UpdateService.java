package ink.akto.converter.currency.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import ink.akto.converter.currency.android.AndroidContracts.IUpdateBinder;
import ink.akto.converter.currency.android.ConverterApplication;
import ink.akto.converter.currency.android.R;
import ink.akto.converter.currency.core.repo.CBRGetCourseStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.repo.SerializationSaveStrategy;
import ink.akto.converter.currency.core.repo.models.MainModel;

import static ink.akto.converter.currency.android.AndroidContracts.Listeners.StateChangeEvent.MAIN_STATE_CHANGED;

/**
 * Created by Ruben on 30.08.2017.
 */

public class UpdateService extends Service
{
    @NonNull private IMainModel model;
    @NonNull private Handler handler;

    @Override
    public void onCreate()
    {
        super.onCreate();
        handler = new Handler();
        model = new MainModel(
                new CBRGetCourseStrategy(),
                new SerializationSaveStrategy(getCacheDir()));
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new UpdateBinder(model);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        ConverterApplication.getThreadsManager().executeInDemonThread(() ->
        {
            try
            {
                model.updateValutasBlocking();
                ConverterApplication.getThreadsManager().getMainTreadExecutor()
                        .execute(()->ConverterApplication.getEventBus().notifyStateChanged(MAIN_STATE_CHANGED));
            }
                catch (Exception e)
            {
                e.printStackTrace();
                ConverterApplication.getThreadsManager().getMainTreadExecutor()
                        .execute(()->Toast.makeText(this, R.string.download_error, Toast.LENGTH_SHORT).show());
            }
        });

        return START_STICKY;
    }

    private static class UpdateBinder extends Binder implements IUpdateBinder
    {
        @NonNull private IMainModel model;

        private UpdateBinder(@NonNull IMainModel model) {
            this.model = model;
        }

        @NonNull
        @Override
        public IMainModel getModel() {
            return model;
        }
    }
}
