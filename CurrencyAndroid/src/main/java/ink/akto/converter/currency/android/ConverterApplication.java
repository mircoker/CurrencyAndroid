package ink.akto.converter.currency.android;

import android.app.Application;
import android.os.Handler;
import android.support.annotation.NonNull;

import ink.akto.converter.currency.android.AndroidContracts.IEventBus;
import ink.akto.converter.currency.core.CoreContracts;
import ink.akto.converter.currency.core.CoreContracts.IThreadsManager;
import ink.akto.converter.currency.core.ThreadsManager;

/**
 * Created by Ruben on 30.08.2017.
 */

public class ConverterApplication extends Application
{
    @NonNull private static IEventBus eventBus;
    @NonNull private static IThreadsManager threadsManager;
    @NonNull private static Handler handler;

    static
    {
        handler = new Handler();

        eventBus = EventBus.init();
        threadsManager = ThreadsManager.init(new CoreContracts.IMainTreadExecutor()
        {
            @Override
            public void execute(@NonNull Runnable runnable) {
                handler.post(runnable);
            }

            @Override
            public void executeDelayed(@NonNull Runnable runnable, long l)
            {
                handler.postDelayed(runnable, l);
            }
        });
    }

    @NonNull
    public static IEventBus getEventBus() {
        return eventBus;
    }

    @NonNull
    public static IThreadsManager getThreadsManager() {
        return threadsManager;
    }
}
