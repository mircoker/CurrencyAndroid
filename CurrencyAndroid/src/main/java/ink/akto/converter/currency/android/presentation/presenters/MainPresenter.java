package ink.akto.converter.currency.android.presentation.presenters;

import android.support.annotation.NonNull;

import java.util.ArrayList;
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
    @NonNull private Map<String, IMainView> views;
    @NonNull private IMainModel model;
    @NonNull private IResourceManager resourceManager;

    private final IThreadsManager threadsManager;
    @NonNull private IValutaConvertionUseCase<IValuta> useCase;

    @NonNull private List<IValuta> valutas;

    public MainPresenter(@NonNull IMainView view,
                         @NonNull IEventBus eventBus,
                         @NonNull IMainModel model,
                         @NonNull IResourceManager resourceManager,
                         @NonNull IThreadsManager threadsManager,
                         @NonNull IValutaConvertionUseCase<IValuta> useCase)
    {
        views = new HashMap<>();
        views.put(view.getClass().getSimpleName(), view);

        this.model = model;
        this.resourceManager = resourceManager;
        this.threadsManager = threadsManager;
        this.useCase = useCase;

        valutas = new ArrayList<>();

        eventBus.register(getClass().getSimpleName(), this);
    }


    @NonNull
    @Override
    public List<IValutaCharacteristics> getCashedValutas()
    {
        return adaptValutas(model.getRuntimeCashingValutas());
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

        return useCase.convertValuta(number, valutaFrom, valutaTo);
    }

    @Override
    public void addView(@NonNull IMainView view)
    {
        views.put(view.getClass().getSimpleName(), view);
    }

    private List<IValutaCharacteristics> adaptValutas(@NonNull List<IValuta> valutas)
    {
        List<IValutaCharacteristics> valutaCharacteristicsList = new ArrayList<>();
        for (int i = 0; i < valutas.size(); i++)
        {
            valutaCharacteristicsList.add(
                    new ValutaCharacteristics(valutas.get(i).getCharCode(), valutas.get(i).getName()));
        }
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
            }
            catch (Exception e)
            {
                e.printStackTrace();
                for (int i = 0; i < views.size(); i++)
                {
                    views.get(i).notifyError(resourceManager.getStringErrorDownloading());
                }
            }
        });
    }

    @Override
    public void notifyStateChanged(StateChangeEvent event)
    {
        if(event == StateChangeEvent.MAIN_STATE_CHANGED)
        {
            for (Map.Entry<String, IMainView> entry : views.entrySet())
            {
                IMainView view = entry.getValue();

                if(!view.notifyStateChanged())
                {
                    threadsManager.getMainTreadExecutor().execute(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if(!view.notifyStateChanged())
                                    {
                                        threadsManager.getMainTreadExecutor().executeDelayed(this, 100);
                                    }
                                }
                            });
                }
            }
        }
    }
}