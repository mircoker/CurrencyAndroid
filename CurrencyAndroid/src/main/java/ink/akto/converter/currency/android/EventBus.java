package ink.akto.converter.currency.android;

import java.util.HashMap;
import java.util.Map;

import ink.akto.converter.currency.android.AndroidContracts.Listeners.IEventBusListener;
import ink.akto.converter.currency.android.AndroidContracts.Listeners.IStateChangedListener;
import ink.akto.converter.currency.android.AndroidContracts.Listeners.StateChangeEvent;

/**
 * Created by Ruben on 29.08.2017.
 */
public class EventBus implements AndroidContracts.IEventBus
{
    private static final EventBus instance = new EventBus();

    private HashMap<String, IEventBusListener> listeners;

    /**
     * Use this method just for init when program starts, then use dependency injection
     * @return EventBus
     */
    public static EventBus init() {
        return instance;
    }

    private EventBus()
    {
        listeners = new HashMap<>();
    }

    @Override
    public void register(String key, IEventBusListener listener)
    {
        listeners.put(key, listener);
    }

    @Override
    public void unregister(String key)
    {
        listeners.remove(key);
    }

    @Override
    public void notifyStateChanged(StateChangeEvent event)
    {
        for (Map.Entry<String, IEventBusListener> entry : listeners.entrySet())
        {
            if(entry.getValue() instanceof IStateChangedListener)
                ((IStateChangedListener) entry.getValue()).notifyStateChanged(event);
        }
    }
}
