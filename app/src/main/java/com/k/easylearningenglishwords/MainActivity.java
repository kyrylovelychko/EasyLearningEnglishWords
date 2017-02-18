package com.k.easylearningenglishwords;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.k.easylearningenglishwords.fragments.AddEditWordFragment;
import com.k.easylearningenglishwords.fragments.DictionariesListFragment;
import com.k.easylearningenglishwords.fragments.DictionaryFragment;
import com.k.easylearningenglishwords.fragments.WordDetailsFragment;

public class MainActivity
        extends AppCompatActivity
        implements DictionariesListFragment.DictionariesListFragmentListener,
        DictionaryFragment.DictionaryFragmentListener,
        WordDetailsFragment.WordDetailsFragmentListener,
        AddEditWordFragment.AddEditWordFragmentListener{

    // Ключ для сохранения Uri словаря в переданном объекте Bundle
    public static final String DICTIONARY_URI = "dictionary_uri";
    // Ключ для сохранения Uri слова в переданном объекте Bundle
    public static final String WORD_URI = "word_uri";

    // Вывод списка контактов
    private DictionariesListFragment dictionariesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Мои словари");

        if (savedInstanceState != null) {
            dictionariesListFragment = new DictionariesListFragment();

            // Добавление фрагмента в FrameLayout
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, dictionariesListFragment);
            transaction.commit(); // Вывод ContactsFragment
        }
    }


    @Override
    public void onDictionarySelected(Uri dictionaryUri) {
        DictionaryFragment dictionaryFragment = new DictionaryFragment();

        // Передача URI словаря в аргументе dictionaryFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(DICTIONARY_URI, dictionaryUri);
        dictionaryFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, dictionaryFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // Приводит к отображению DictionaryFragment
        setTitle("Словарь");
    }

    @Override
    public void onAddDictionary() {

    }

    @Override
    public void onWordSelected(Uri wordUri) {
        WordDetailsFragment wordDetailsFragment = new WordDetailsFragment();

        // Передача URI словаря в аргументе wordDetailsFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        wordDetailsFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentDictionary, wordDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // Приводит к отображению DictionaryFragment
        setTitle("Слово");
    }

    @Override
    public void onAddWord() {
        displayAddEditWordFragment(null);
    }

    @Override
    public void onWordEdited(Uri wordUri) {
        displayAddEditWordFragment(wordUri);
    }

    @Override
    public void onWordDeleted() {

    }

    private void displayAddEditWordFragment (Uri wordUri){
        AddEditWordFragment addEditWordFragment = new AddEditWordFragment();

        // Передача URI словаря в аргументе addEditWordFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(WORD_URI, wordUri);
        addEditWordFragment.setArguments(arguments);

        // Использование FragmentTransaction для отображения
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentWordDetails, addEditWordFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // Приводит к отображению DictionaryFragment
    }

    @Override
    public void onAddEditCompleted(Uri wordUri) {

    }
}
