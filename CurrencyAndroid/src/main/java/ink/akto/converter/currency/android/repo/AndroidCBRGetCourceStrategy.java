package ink.akto.converter.currency.android.repo;

import android.support.annotation.NonNull;

import java.util.List;

import ink.akto.converter.currency.android.AndroidContracts.IUpdateServiceConnection;
import ink.akto.converter.currency.core.repo.CBRGetCourseStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.IValuta;

/**
 * Created by Ruben on 01.09.2017.
 */

public class AndroidCBRGetCourceStrategy extends CBRGetCourseStrategy
{
    @NonNull private IUpdateServiceConnection serviceConnection;

    public AndroidCBRGetCourceStrategy(@NonNull IUpdateServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
    }

    @NonNull
    @Override
    public synchronized List<IValuta> getValutasBlocking() throws Exception
    {
        //ждём пока сервис подымется
        while(serviceConnection.getBinder()==null || serviceConnection.getBinder().getModel()==null) Thread.sleep(100);

        return serviceConnection.getBinder().getModel().updateValutasBlocking();
    }
}
