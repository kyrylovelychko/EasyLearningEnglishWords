package com.velychko.kyrylo.mydictionaries.ui.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.velychko.kyrylo.mydictionaries.R;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseDescription.Dictionaries;
import com.velychko.kyrylo.mydictionaries.data.sqlite.DatabaseMaster;
import com.velychko.kyrylo.mydictionaries.ui.fragments.AddEditWordFragment;
import com.velychko.kyrylo.mydictionaries.ui.fragments.DictionariesListFragment;
import com.velychko.kyrylo.mydictionaries.ui.fragments.DictionaryFragment;
import com.velychko.kyrylo.mydictionaries.ui.fragments.StartTrainingFragment;
import com.velychko.kyrylo.mydictionaries.ui.fragments.WordDetailsFragment;
import com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs.AddDictionaryDialog;
import com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs.DeleteDictionaryDialog;
import com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs.DeleteWordDialog;
import com.velychko.kyrylo.mydictionaries.ui.fragments.dialogs.RenameDictionaryDialog;

import java.util.Date;

import static com.velychko.kyrylo.mydictionaries.utils.Constants.*;

public class MainActivity
        extends AppCompatActivity
        implements DictionariesListFragment.DictionariesListFragmentListener,
        DictionaryFragment.DictionaryFragmentListener,
        WordDetailsFragment.WordDetailsFragmentListener,
        AddEditWordFragment.AddEditWordFragmentListener,
        DeleteWordDialog.DeleteWordDialogListener,
        RenameDictionaryDialog.RenameDictionaryDialogListener {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initNavigationView();

        // Первоначальный экран - фрагмент со списком словарей
        if (savedInstanceState == null) {
            DictionariesListFragment dictionariesListFragment = new DictionariesListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, dictionariesListFragment);
            transaction.commit();
        }
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_my_dictionaries:
                        // Очищаем BackStack. Кто теперь нажмет назад - выйдет из пиложения
                        getSupportFragmentManager()
                                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        // Запуск фрагмента со списком всех словарей
                        transaction.replace(R.id.fragmentContainer, new DictionariesListFragment());
                        break;
                    case R.id.navigation_training:
                        // Очищаем BackStack. Кто теперь нажмет назад - выйдет из пиложения
                        getSupportFragmentManager()
                                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        // Запуск фрагмента с тренировками
                        transaction.replace(R.id.fragmentContainer, new StartTrainingFragment());
                        break;
                }
                transaction.commit();
                return true;
            }
        });
    }

    //region ===== Взаимодействие с фрагментами для работы со словарями =====

    @Override
    public void onAddDictionary() {
        // Вызов диалога добавления нового словаря
        new AddDictionaryDialog().show(getSupportFragmentManager(), "add dictionary");
    }


    @Override
    public void onSelectDictionary(Uri dictionaryUri) {
        // Вывод фрагмента со словами конкретного словаря
        // Uri словаря передаем параметром
        DictionaryFragment dictionaryFragment = new DictionaryFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_DICTIONARY_URI, dictionaryUri);
        dictionaryFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, dictionaryFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit();
    }

    @Override
    public void onRenameDictionary(String dictionaryName) {
        // Вызов диалога переименования словаря
        // Имя словаря передаем параметром
        RenameDictionaryDialog dialog = new RenameDictionaryDialog();
        Bundle arguments = new Bundle();
        arguments.putString(ARGS_DICTIONARY_NAME, dictionaryName);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "rename dictionary");
    }


    @Override
    public void onDeleteDictionary(String dictionaryName) {
        // Вызов диалога для подтверждения удаления словаря. Удаление словаря
        // Имя словаря передаем параметром
        DeleteDictionaryDialog dialog = new DeleteDictionaryDialog();
        Bundle arguments = new Bundle();
        arguments.putString(ARGS_DICTIONARY_NAME, dictionaryName);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "delete dictionary");
    }

    //endregion

    //region ===== Взаимодействие с фрагментами для работы со словами =====

    @Override
    public void onAddWord(String dictionaryName) {
        // Вывод фрагмента добавления/редактирования слова
        // Первый параметр = null - это значит, что будем добавлять новое слово в указанный словарь
        // dictionaryName
        displayAddEditWordFragment(null, dictionaryName);
    }

    @Override
    public void onSelectWord(Uri wordUri) {
        // Вывод фрагмента с деталями конкретного слова
        // Uri слова передаем параметром
        WordDetailsFragment wordDetailsFragment = new WordDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_WORD_URI, wordUri);
        wordDetailsFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, wordDetailsFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit(); // Приводит к отображению DictionaryFragment
    }

    @Override
    public void onEditWord(Uri wordUri) {
        // Вывод фрагмента добавления/редактирования слова
        // Первый параметр = wordUri - это значит, что будем редактировать существующее слово
        // с указанным Uri
        displayAddEditWordFragment(wordUri, null);
    }

    @Override
    public void onDeleteWord(Uri wordUri) {
        DeleteWordDialog dialog = new DeleteWordDialog();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_WORD_URI, wordUri);
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "delete word");
    }

    private void displayAddEditWordFragment(Uri wordUri, String dictionaryName) {
        // Вывод фрагмента добавления/редактирования слова
        // Если редактирование слова - Uri слова передаем параметром
        // Если добавление нового слова - Uri будет содержать null,
        // а передаем имя словаря нового слова
        AddEditWordFragment addEditWordFragment = new AddEditWordFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGS_WORD_URI, wordUri);
        arguments.putString(ARGS_DICTIONARY_NAME, dictionaryName);
        addEditWordFragment.setArguments(arguments);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, addEditWordFragment);
        transaction.addToBackStack("MyStack");
        transaction.commit();
    }

    @Override
    public void onAddEditWordCompleted(Uri wordUri, String dictionaryName) {
        // После любого изменения в слове, надо обновить дату последнего изменения его словаря
        changeDateOfChangeDictionary(dictionaryName);
    }

    // Обновление даты последнего изменения для словаря в таблице словарвей
    @Override
    public void changeDateOfChangeDictionary(String dictionaryName) {
        // Получаем _ID словаря по имени
        Cursor cursor = DatabaseMaster.getInstance(this)
                .updateDateOfChangeDictionary(dictionaryName);
        if (cursor.getCount() > 0) {
            // Обновляем дату последнего изменения словаря по Uri словаря
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(Dictionaries._ID));
            Uri dictionaryUri = Dictionaries.buildDictionariesUri(id);
            ContentValues cv = new ContentValues();
            cv.put(Dictionaries.COLUMN_DATE_OF_CHANGE, new Date().getTime() / 1000);
            int updatedRows = getContentResolver().update(
                    dictionaryUri,
                    cv,
                    null,
                    null);
            if (updatedRows < 0) {
                throw new SQLException("" + R.string.exc_invalid_update_uri + dictionaryUri);
            }
        }
    }

    //endregion

    //region ===== SnackBar =====

    @Override
    public void showSnackBar(int snackTextRId) {
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), snackTextRId,
                Snackbar.LENGTH_LONG);
        // Если показывать все нужные SnackBar'ы, на некоторых устройствах наблюдается проблема
        // с отображением FAB. Поэтому некотрые снэкбары временно недоступны
//        snackbar.show();
    }

    @Override
    // Спрятать snackbar
    public void dismissSnackBar() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    //endregion ===== SnackBar =====

    // По нажатию на кнопку Назад смотрим на BackStack. Если он пустой - запрашиваем подтверждение
    // на выход из приложения. Если нет - идем назад на один фрагмент.
    // Перед запуском фрагментов, с которых приложение не должно возвращаться назад, а должно
    // закрывать приложение, очищается BackStack
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.snack_confirm_exit,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.snack_exit, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }).show();
        } else {
            super.onBackPressed();
        }
    }

}
