package ink.akto.converter.currency.android.presentation.presenters;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ink.akto.converter.currency.android.AndroidContracts.IEventBus;
import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.AndroidContracts.IMainView;
import ink.akto.converter.currency.android.AndroidContracts.IMainView.IValutaCharacteristics;
import ink.akto.converter.currency.android.AndroidContracts.IResourceManager;
import ink.akto.converter.currency.android.AndroidContracts.Listeners.StateChangeEvent;
import ink.akto.converter.currency.android.presentation.MalformedParametersException;
import ink.akto.converter.currency.android.presentation.ValutaCharacteristics;
import ink.akto.converter.currency.core.CoreContracts.IThreadsManager;
import ink.akto.converter.currency.core.domain.UseCasesContracts.IValutaConvertionUseCase;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.repo.RepoContracts.IValuta;

/**
 * Created by Ruben on 30.08.2017.
 */

public class MainPresenter implements IMainPresenter<IValutaCharacteristics>
{
    @NonNull private Map<String, WeakReference<IMainView>> views;
    @NonNull private IMainModel model;
    @NonNull private IResourceManager resourceManager;

    private final IThreadsManager threadsManager;
    @NonNull private IValutaConvertionUseCase<IValuta> useCase;

    @NonNull private List<IValuta> valutas;
    @NonNull private List<IValutaCharacteristics> valutasCharacteristics;

    public MainPresenter(@NonNull IMainView view,
                         @NonNull IEventBus eventBus,
                         @NonNull IMainModel model,
                         @NonNull IResourceManager resourceManager,
                         @NonNull IThreadsManager threadsManager,
                         @NonNull IValutaConvertionUseCase<IValuta> useCase)
    {
        views = new HashMap<>();
        views.put(view.getClass().getSimpleName(), new WeakReference<>(view));

        this.model = model;
        this.resourceManager = resourceManager;
        this.threadsManager = threadsManager;
        this.useCase = useCase;

        valutas = new ArrayList<>();
        valutasCharacteristics = new ArrayList<>();

        eventBus.register(getClass().getSimpleName(), this);
    }


    @NonNull
    public List<IValutaCharacteristics> getValutasCharacteristics()
    {
        if(valutasCharacteristics.isEmpty())
        {
            try
            {
                if(valutas.isEmpty()) valutas = model.getCashedValutas();
                valutasCharacteristics = adaptValutas(valutas);
            }
                catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return valutasCharacteristics;
    }

    @Override
    public double convertValuta(double number, @NonNull IValutaCharacteristics from, @NonNull IValutaCharacteristics to) throws MalformedParametersException
    {
        IValuta valutaFrom = null;
        IValuta valutaTo = null;

        for (IValuta valuta : valutas)
        {
            if(valuta.getCharCode().equals(from.getCharCode()))valutaFrom = valuta;
            if(valuta.getCharCode().equals(to.getCharCode()))valutaTo = valuta;
        }

        if(valutaFrom==null || valutaTo==null) throw new MalformedParametersException();

        return new BigDecimal(useCase.convertValuta(number, valutaFrom, valutaTo))
                .setScale(3, RoundingMode.HALF_EVEN).doubleValue();
    }

    @Override
    public void addView(@NonNull IMainView view)
    {
        views.put(view.getClass().getSimpleName(), new WeakReference<>(view));
    }

    private List<IValutaCharacteristics> adaptValutas(@NonNull List<IValuta> valutas)
    {
        List<IValutaCharacteristics> valutaCharacteristicsList = new ArrayList<>();
        for (int i = 0; i < valutas.size(); i++)
        {
            valutaCharacteristicsList.add(
                    new ValutaCharacteristics(valutas.get(i).getCharCode(), valutas.get(i).getName()));
        }

        Collections.sort(valutaCharacteristicsList, (first, second) -> first.getCharCode().compareTo(second.getCharCode()));

        return valutaCharacteristicsList;
    }

    @Override
    public void updateState()
    {
        threadsManager.executeInDemonThread(()->
        {
            try
            {
                valutas = model.updateValutasBlocking();
                enumerateViewsAndExecuteInMainThread(Execute.NOTIFY_STATE_CHANGED);
            }
                catch (Exception e)
            {
                e.printStackTrace();
                enumerateViewsAndExecuteInMainThread(Execute.NOTIFY_ERROR);
            }
        });
    }

    private enum Execute{NOTIFY_ERROR, NOTIFY_STATE_CHANGED}
    private void enumerateViewsAndExecuteInMainThread(@NonNull Execute execute)
    {
        for (Map.Entry<String, WeakReference<IMainView>> entry : views.entrySet())
        {
            WeakReference<IMainView> view = entry.getValue();

            threadsManager.getMainTreadExecutor().execute(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(view.get()!=null)
                        {
                            switch (execute)
                            {
                                case NOTIFY_STATE_CHANGED:
                                {
                                    if(!view.get().notifyStateChanged()) break;
                                    else return;
                                }
                                case NOTIFY_ERROR:
                                {
                                    if(!view.get().notifyError(resourceManager.getStringErrorDownloading()))break;
                                    else return;
                                }
                            }
                            threadsManager.getMainTreadExecutor().executeDelayed(this, 100);
                        }
                    }
                });
        }
    }

    @Override
    public void notifyStateChanged(StateChangeEvent event)
    {
        if(event == StateChangeEvent.MAIN_STATE_CHANGED)
        {
            enumerateViewsAndExecuteInMainThread(Execute.NOTIFY_STATE_CHANGED);
        }
    }
}