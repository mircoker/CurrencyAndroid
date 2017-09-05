package ink.akto.converter.currency.android;

import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState;
import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState.IValutaCharacteristics;
import ink.akto.converter.currency.android.AndroidContracts.Listeners.IStateChangedListener;
import ink.akto.converter.currency.android.presentation.MalformedParametersException;
import ink.akto.converter.currency.core.CoreContracts;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.utils.CallbackWithEntity;

/**
 * Created by Ruben on 30.08.2017.
 */

public interface AndroidContracts
{
    interface IMVPView {}
    interface IMVPPresenter{}
    interface IViewState extends Serializable {}

    interface IMainView extends IMVPView
    {
        interface IMainViewState extends IViewState
        {
            int getFromPos();
            void setFromPos(int pos);
            int getToPos();
            void setToPos(int pos);
            double getFromValue();
            void setFromValue(double val);
            double getToValue();
            void setToValue(double val);
            @NonNull List<IValutaCharacteristics> getValutasCharacteristics();

            interface IValutaCharacteristics extends Serializable
            {
                @NonNull String getCharCode();
                @NonNull String getName();
            }
        }

        boolean notifyStateChanged();
        boolean notifyError(@NonNull String error);
    }

    interface IMainPresenter extends IMVPPresenter
    {
        void saveState();
        @NonNull IMainViewState getState();
        double convertValuta(double number,
                             @NonNull IValutaCharacteristics from,
                             @NonNull IValutaCharacteristics to) throws MalformedParametersException;
        void addView(@NonNull IMainView view);
        void updateState();
        boolean isNeedUpdate();
    }

    interface IResourceManager extends CoreContracts.IManager
    {
        @NonNull String getStringErrorDownloading();
    }

    interface IEventBus extends IStateChangedListener
    {
        void register(String key, Listeners.IEventBusListener listener);
        void unregister(String key);
    }

    interface Listeners
    {
        interface IEventBusListener{}

        enum StateChangeEvent {MAIN_STATE_CHANGED}
        interface IStateChangedListener extends IEventBusListener
        {
            void notifyStateChanged(StateChangeEvent event);
        }
    }

    interface IUpdateBinder extends IBinder
    {
        @Nullable IMainModel getModel();
    }

    interface IUpdateServiceConnection extends ServiceConnection
    {
        void setModel(@NonNull IMainModel model);
        void setOnBindListener(CallbackWithEntity<IMainModel> callback);
        @Nullable IUpdateBinder getBinder();
    }

    interface IMainActivity extends IMainView
    {
        @NonNull IMainPresenter getPresenter();
    }
}