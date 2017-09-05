package ink.akto.converter.currency.android.presentation.views;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ink.akto.converter.currency.android.AndroidContracts.IMainActivity;
import ink.akto.converter.currency.android.AndroidContracts.IMainPresenter;
import ink.akto.converter.currency.android.AndroidContracts.IMainView;
import ink.akto.converter.currency.android.AndroidContracts.IMainView.IMainViewState.IValutaCharacteristics;
import ink.akto.converter.currency.android.R;
import ink.akto.converter.currency.android.presentation.MalformedParametersException;

/**
 * Created by Ruben on 31.08.2017.
 */

public class MainFragment extends Fragment implements IMainView
{
    @Nullable private IMainPresenter presenter;
    @Nullable private View rootView;
    @Nullable private NumberPicker numberPickerFrom;
    @Nullable private NumberPicker numberPickerTo;
    @Nullable private TextView nameFrom;
    @Nullable private TextView nameTo;
    @Nullable private EditText nameFromValue;
    @Nullable private EditText nameToValue;

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
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        presenter = ((IMainActivity)getActivity()).getPresenter();
        presenter.addView(this);
        if(savedInstanceState==null)initPickers(presenter.getState());
    }

    private void initPickers(@NonNull IMainViewState state)
    {
        List<IValutaCharacteristics> valutas = state.getValutasCharacteristics();
        if(!valutas.isEmpty())
        {
            numberPickerFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            numberPickerTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


            List<String> charsCodes = new ArrayList<>();

            for (int i = 0; i < valutas.size(); i++)
            {
                charsCodes.add(valutas.get(i).getCharCode());
            }

            numberPickerFrom.setMaxValue(charsCodes.size()-1);
            numberPickerFrom.setMinValue(0);
            numberPickerFrom.setDisplayedValues(charsCodes.toArray(new String[]{}));
            numberPickerFrom.setValue(state.getFromPos());
            numberPickerFrom.setWrapSelectorWheel(true);
            numberPickerFrom.setOnValueChangedListener((numberPicker, i, i1) ->
            {
                nameFrom.setText(valutas.get(i1).getName());
                nameToValue.setText(String.valueOf(calculateValue(state)));
                commitState(state);
            });
            nameFrom.setText(valutas.get(state.getFromPos()).getName());
            nameFromValue.setText(String.valueOf(state.getFromValue()));
            nameFromValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable)
                {
                    if(!editable.toString().isEmpty())
                    {
                        nameToValue.setText(String.valueOf(calculateValue(state)));
                        commitState(state);
                    }
                }
            });

            numberPickerTo.setMaxValue(charsCodes.size()-1);
            numberPickerTo.setMinValue(0);
            numberPickerTo.setDisplayedValues(charsCodes.toArray(new String[]{}));
            numberPickerTo.setValue(state.getToPos());
            numberPickerTo.setWrapSelectorWheel(true);
            numberPickerTo.setOnValueChangedListener((numberPicker, i, i1) ->
            {
                nameTo.setText(valutas.get(i1).getName());
                nameToValue.setText(String.valueOf(calculateValue(state)));
                commitState(state);
            });
            nameTo.setText(valutas.get(state.getToPos()).getName());
            nameToValue.setText(String.valueOf(calculateValue(state)));
            nameToValue.setKeyListener(null);
        }
    }

    private void commitState(@NonNull IMainViewState state)
    {
        state.setFromPos(numberPickerFrom.getValue());
        state.setToPos(numberPickerTo.getValue());
        state.setFromValue(Double.valueOf(nameFromValue.getText().toString()));
        state.setToValue(Double.valueOf(nameToValue.getText().toString()));
    }

    private double calculateValue(@NonNull IMainViewState state)
    {
        try
        {
            return presenter.convertValuta(Double.valueOf(nameFromValue.getText().toString()),
                    state.getValutasCharacteristics().get(numberPickerFrom.getValue()),
                    state.getValutasCharacteristics().get(numberPickerTo.getValue()));

        }
            catch (MalformedParametersException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView==null)
        {
            rootView = inflater.inflate(R.layout.fragment_main, null);

            numberPickerFrom = rootView.findViewById(R.id.picker_from);
            numberPickerTo = rootView.findViewById(R.id.picker_to);

            nameFrom = rootView.findViewById(R.id.tw_from_name);
            nameTo = rootView.findViewById(R.id.tw_to_name);

            nameFromValue = rootView.findViewById(R.id.tw_from_value);
            nameToValue = rootView.findViewById(R.id.tw_to_value);
        }

        return rootView;
    }

    @Override
    public boolean notifyStateChanged()
    {
        if(presenter!=null && nameToValue!=null)
        {
            initPickers(presenter.getState());
            return true;
        }

        return false;
    }

    @Override
    public boolean notifyError(@NonNull String error) {
        return true;
    }
}
