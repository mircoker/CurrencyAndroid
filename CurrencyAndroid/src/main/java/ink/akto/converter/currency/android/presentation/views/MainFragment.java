package ink.akto.converter.currency.android.presentation.views;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import ink.akto.converter.currency.android.AndroidContracts.IMainActivity;
import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.AndroidContracts.IMainView;
import ink.akto.converter.currency.android.R;

/**
 * Created by Ruben on 31.08.2017.
 */

public class MainFragment extends Fragment implements IMainView
{
    @Nullable private IMainPresenter<IValutaCharacteristics> presenter;
    @Nullable private NumberPicker numberPicker;
    @NonNull Handler handler;

    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        presenter = ((IMainActivity)getActivity()).getPresenter();
        presenter.addView(this);
        initPickers(presenter);
    }

    /**
     * Presenter бывает null, по этому лучше передавать его аргументом чтобы следить за этим в моменты вызова
     * @param presenter
     */
    private void initPickers(@NonNull IMainPresenter<IValutaCharacteristics> presenter)
    {
        List<IValutaCharacteristics> valutas = presenter.getCashedValutas();
        if(!valutas.isEmpty())
        {
            numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            List<String> charsCodes = new ArrayList<>();

            for (int i = 0; i < valutas.size(); i++)
            {
                charsCodes.add(valutas.get(i).getCharCode());
            }


            numberPicker.setMaxValue(charsCodes.size());
            numberPicker.setMinValue(1);
//            numberPicker.setMaxValue(4);
//            numberPicker.setMinValue(1);

            numberPicker.setDisplayedValues(charsCodes.toArray(new String[]{}));
            numberPicker.setWrapSelectorWheel(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, null);

        numberPicker = rootView.findViewById(R.id.picker);

        return rootView;
    }

    @Override
    public boolean notifyStateChanged()
    {
        if(presenter!=null)
        {
            initPickers(presenter);
            return true;
        }

        return false;
    }

    @Override
    public boolean notifyError(@NonNull String error) {
        return true;
    }
}
