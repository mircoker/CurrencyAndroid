package ink.akto.converter.currency.android.presentation.presenters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.AndroidContracts.IMainView;
import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState;
import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState.IValutaCharacteristics;
import ink.akto.converter.currency.android.AndroidContracts.IResourceManager;
import ink.akto.converter.currency.android.presentation.MainViewState;
import ink.akto.converter.currency.android.presentation.MalformedParametersException;
import ink.akto.converter.currency.android.presentation.ValutaCharacteristics;
import ink.akto.converter.currency.core.CoreContracts.IThreadsManager;
import ink.akto.converter.currency.core.domain.UseCasesContracts.IValutaConvertionUseCase;
import ink.akto.converter.currency.core.repo.RepoContracts.IMainModel;
import ink.akto.converter.currency.core.repo.RepoContracts.ISaveStrategy;
import ink.akto.converter.currency.core.repo.RepoContracts.IValuta;

/**
 * Created by Ruben on 30.08.2017.
 */

public class MainPresenter implements IMainPresenter
{
    @NonNull private static Map<String, WeakReference<IMainView>> views;
    @NonNull private IMainModel model;
    @NonNull private IResourceManager resourceManager;
    @NonNull private ISaveStrategy saveStateStrategy;
    @NonNull private final IThreadsManager threadsManager;
    @NonNull private IValutaConvertionUseCase<IValuta> useCase;

    @NonNull private static volatile List<IValuta> valutas;
    @Nullable private static volatile IMainViewState state;

    public MainPresenter(@NonNull IMainView view,
                         @NonNull ISaveStrategy saveStateStrategy,
                         @NonNull IMainModel model,
                         @NonNull IResourceManager resourceManager,
                         @NonNull IThreadsManager threadsManager,
                         @NonNull IValutaConvertionUseCase<IValuta> useCase)
    {
        if(views==null)views = new HashMap<>();
        views.put(view.getClass().getSimpleName(), new WeakReference<>(view));

        this.saveStateStrategy = saveStateStrategy;
        this.model = model;
        this.resourceManager = resourceManager;
        this.threadsManager = threadsManager;
        this.useCase = useCase;

        try
        {
            valutas = model.getCashedValutas();
        }
        catch (Exception e) {
            e.printStackTrace();
            valutas = new ArrayList<>();
        }
    }



    @Override
    public void saveState()
    {
        try
        {
            saveStateStrategy.save(getState(), getClass().getSimpleName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public IMainViewState getState()
    {
        if(state==null)
        {
            try
            {
                state = (IMainViewState) saveStateStrategy.restore(getClass().getSimpleName());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if(state==null)
            {
                if(state==null)state = new MainViewState(new ArrayList<>());
                try
                {
                    if(valutas.isEmpty()) valutas = model.getCashedValutas();
                    state.getValutasCharacteristics().addAll(adaptValutas(valutas));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return state;
    }

    @Override
    public double convertValuta(double number,
                                @NonNull IValutaCharacteristics from,
                                @NonNull IValutaCharacteristics to) throws MalformedParametersException
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
                threadsManager.getMainTreadExecutor().execute(()->
                {
                    getState().getValutasCharacteristics().clear();
                    getState().getValutasCharacteristics().addAll(adaptValutas(valutas));
                });
                enumerateViews(Execute.NOTIFY_STATE_CHANGED);
            }
                catch (Exception e)
            {
                e.printStackTrace();
                enumerateViews(Execute.NOTIFY_ERROR);
            }
        });
    }

    @Override
    public boolean isNeedUpdate() {
        return valutas.isEmpty() || getState().getValutasCharacteristics().isEmpty();
    }

    private enum Execute{NOTIFY_ERROR, NOTIFY_STATE_CHANGED}
    private void enumerateViews(@NonNull Execute execute)
    {
        for (Map.Entry<String, WeakReference<IMainView>> entry : views.entrySet())
        {
            threadsManager.getMainTreadExecutor().execute(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(entry.getValue().get()!=null)
                        {
                            switch (execute)
                            {
                                case NOTIFY_STATE_CHANGED:
                                {

                                    if(!entry.getValue().get().notifyStateChanged()) break;
                                    else return;
                                }
                                case NOTIFY_ERROR:
                                {
                                    if(!entry.getValue().get().notifyError(resourceManager.getStringErrorDownloading()))break;
                                    else return;
                                }
                            }
                            threadsManager.getMainTreadExecutor().executeDelayed(this, 100);
                        }
                    }
                });
        }
    }
}