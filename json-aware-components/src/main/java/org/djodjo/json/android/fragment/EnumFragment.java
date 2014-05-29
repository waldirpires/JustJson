/*
 * Copyright (C) 2014 Kalin Maldzhanski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.djodjo.json.android.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.djodjo.json.JsonElement;
import org.djodjo.json.JsonString;
import org.djodjo.json.android.R;

import java.util.ArrayList;

//TODO accept only strings now
public class EnumFragment extends BasePropertyFragment {

    public final static int LAYOUT_ENUM_RADIO = R.layout.fragment_enum_radio;
    public final static int LAYOUT_ENUM_SPINNER = R.layout.fragment_enum_spinner;
    public final static int LAYOUT_ENUM_LISTVIEW = R.layout.fragment_enum_listview;

    public static final String ARG_OPTIONS = "options";
    public static final String ARG_IS_CONTROLLER = "isController";

    public static final String ARG_TRANSLATE_OPTIONS = "translateOptions";
    public static final String ARG_TRANSLATE_OPTIONS_PREFIX = "translateOptionsPrefix";

    protected ArrayList<String> options;

    protected boolean isController = false;
    protected boolean translateOptions = false;
    protected String translateOptionsPrefix = "";


    protected ControllerCallback controllerCallback = null;

    RadioGroup enumRadioGroup = null;
    Spinner enumSpinner = null;
    ListView enumListView = null;

    public EnumFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getLayoutId() {
        int currLayoutId = globalLayouts.get(ARG_GLOBAL_ENUM_LAYOUT);

        if(currLayoutId==0) {
            if (options.size() > 3)
                currLayoutId = LAYOUT_ENUM_SPINNER;
                //return LAYOUT_ENUM_LISTVIEW;
            else
                currLayoutId = LAYOUT_ENUM_RADIO;
        }

        return currLayoutId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            options = getArguments().getStringArrayList(ARG_OPTIONS);
            isController = getArguments().getBoolean(ARG_IS_CONTROLLER, false);
            translateOptions = getArguments().getBoolean(ARG_TRANSLATE_OPTIONS, false);
            translateOptionsPrefix = getArguments().getString(ARG_TRANSLATE_OPTIONS_PREFIX, "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(isController) {
            try {
                controllerCallback = (ControllerCallback) getFragmentManager().findFragmentByTag("oneOf");
            } catch(Exception ex) {
            }
        }
        enumRadioGroup = (RadioGroup) v.findViewById(R.id.enumRadioGroup);
        enumSpinner = (Spinner) v.findViewById(R.id.enumSpinner);
        enumListView = (ListView) v.findViewById(R.id.enumListView);
        ArrayList<String> inOptions = options;
        if(translateOptions) {
            inOptions = translateOptions(options, translateOptionsPrefix);
        }
        if(enumRadioGroup!=null) {

            boolean checked = false;
            for(final String option:options) {
                RadioButton button = (RadioButton)inflater.inflate(R.layout.radio_button, enumRadioGroup, false);
                button.setText(inOptions.get(options.indexOf(option)));
                button.setTextAppearance(getActivity(), styleValue);
                if(buttonSelector!=0) {
                    button.setBackgroundResource(buttonSelector);
                } else if (customButtonSelectors!= null && customButtonSelectors.get(ARG_GLOBAL_RADIOBUTTON_SELECTOR) != 0)
                {
                    button.setBackgroundResource(customButtonSelectors.get(ARG_GLOBAL_RADIOBUTTON_SELECTOR));
                }
                if(controllerCallback !=null) {
                    button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked) {
                                controllerCallback.onValueChanged(label, options.indexOf(option));
                            }
                        }
                    });
                }
                enumRadioGroup.addView(button);
                if(!checked) {
                    button.setChecked(true);
                    checked = true;
                }
            }
        }
        else if(enumSpinner!=null) {


            SpinnerAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, inOptions);
            enumSpinner.setAdapter(adapter);
            if(controllerCallback !=null) {
                enumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        controllerCallback.onValueChanged(label, position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

        }
        else if(enumListView != null) {

            ArrayAdapter<String>  adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, inOptions);
            enumListView.setAdapter(adapter);
            enumListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            //if in scroll view we need to measure height ourselves
            int totalHeight = 0;
            for (int i = 0; i < adapter.getCount(); i++) {
                View listItem = adapter.getView(i, null, enumListView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = enumListView.getLayoutParams();
            params.height = totalHeight + (enumListView.getDividerHeight() * (adapter.getCount() - 1));
            enumListView.setLayoutParams(params);
            enumListView.requestLayout();
            if(controllerCallback !=null) {
                enumListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        controllerCallback.onValueChanged(label, position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

        }

        return v;
    }

    @Override
    public JsonElement getJsonElement() {
        String value = null;
        JsonString res= null;

        if(enumRadioGroup!=null && enumRadioGroup.getCheckedRadioButtonId()!=-1) {
            int id= enumRadioGroup.getCheckedRadioButtonId();
            View radioButton = enumRadioGroup.findViewById(id);
            int radioId = enumRadioGroup.indexOfChild(radioButton);
            res = new JsonString(options.get(radioId));
        } else if(enumSpinner!=null) {
            res = new JsonString(options.get(enumSpinner.getSelectedItemPosition()));
        }
        else if(enumListView != null) {
            res = new JsonString(options.get(enumListView.getSelectedItemPosition()));
        }
        if(value!=null) {
            res = new JsonString(value);
        }
        return res;
    }


    private ArrayList<String> translateOptions(ArrayList<String> in, String prefix) {
        if(prefix==null) prefix = "";
        ArrayList<String> out = new ArrayList<String>();
        String packageName = getActivity().getPackageName();
        for(String entry:in) {
            try {
                int resId = getResources().getIdentifier(prefix + entry, "string", packageName);
                if (resId != 0) {
                    String newStr = getString(resId);
                    if (newStr != null && !newStr.trim().isEmpty()) {
                        out.add(newStr);
                    }
                } else {
                    out.add(entry);
                    Log.d("EnumFragment", "not found resource for enum: " + entry);
                }
            } catch (Exception ex) {
                out.add(entry);
                Log.d("EnumFragment", "not found resource for enum: " + entry);
            }
        }

        return out;
    }



}
