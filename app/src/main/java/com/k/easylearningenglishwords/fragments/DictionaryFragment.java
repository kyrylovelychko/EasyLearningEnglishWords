package com.k.easylearningenglishwords.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.k.easylearningenglishwords.R;

public class DictionaryFragment extends Fragment {

    public interface DictionaryFragmentListener {

        //Вызывается при выборе слова
        void onWordSelected(Uri wordUri);

        //Вызывается при нажатии кнопки добавления нового слова
        void onAddWord();
    }

    public DictionaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dictionary, container, false);
    }

}
