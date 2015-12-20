package com.sreemenon.opensesame;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Sree on 19/10/2015.
 *
 * MainActivity class
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private List<DataItem> dataItemList;

    private int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(R.layout.fragment_home);
            }
        });
        toolbar.setVisibility(View.GONE);

        Bundle extras = getIntent().getExtras();
        if(dataItemList == null)
            dataItemList = (List<DataItem>)extras.getSerializable("dataItemList");

        getSupportFragmentManager().beginTransaction().add(R.id.mainFragmentContainer, HomeFragment.newInstance()).commit();
    }

    /**
     * Getter for the DataItemList
     *
     * @return dataItemList: list of all dataItems
     */
    public List<DataItem> getDataItemList(){
        return dataItemList;
    }

    /**
     * Delete an entry from dataItemList and refreshing the recyclerview without pinging the DB
     *
     * @param id the id of dataItem to be deleted
     */
    public void deleteFromDataItemList(int id){
        for(int i = 0; i < dataItemList.size(); i++){
            if(dataItemList.get(i).getId() == id) {
                dataItemList.remove(i);
                break;
            }
        }
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if(fragment instanceof HomeFragment){
            ((HomeFragment)fragment).refreshAdapter();
        }
    }

    /**
     * Edit an entry from dataItemList and refreshing the recyclerview without pinging the DB      *
     *
     * @param id the id of dataItem to be edited
     * @param item the dataItem to be replaced with
     */
    public void editDataItemList(int id, DataItem item){
        deleteFromDataItemList(id);
        addToDataItemList(item);
    }

    /**
     * Add an entry to dataItemList and refreshing the recyclerview without pinging the DB
     * Also it sorts the list in ascending order (website,uname)
     *
     * @param item the dataItem to be added
     */
    public void addToDataItemList(DataItem item){
        if(dataItemList.size() == 0){
            dataItemList.add(item);
        }
        for(int i = 0; i < dataItemList.size(); i++){
            if(item.getWebsite().compareTo(dataItemList.get(i).getWebsite()) < 0){
                dataItemList.add(i, item);
                break;
            }else if(item.getWebsite().compareTo(dataItemList.get(i).getWebsite()) == 0){
                if(item.getUname().compareTo(dataItemList.get(i).getUname()) < 0) {
                    dataItemList.add(i, item);
                    break;
                }
            }
        }
    }

    /**
     * get the position or index of the dataitem to be edited
     *
     * @return index of the dataitem
     */
    public int getEditPosition() {
        return editPosition;
    }

    /**
     * set the position or index of the dataitem to be edited
     * @param editPosition
     */
    public void setEditPosition(int editPosition) {
        this.editPosition = editPosition;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if(getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer) instanceof  AddFragment) {
                AddFragment addFragment = (AddFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
                addFragment.saveNewPassword();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * Switch between the fragments
     *
     * @param fragmentId the layout id of the fragment to be replaced with
     */
    public void switchFragment(int fragmentId){
        switch (fragmentId){
            case R.layout.fragment_home:
                toolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.toolbar_slide_up));
                TransitionManager.beginDelayedTransition(toolbar);
                toolbar.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, HomeFragment.newInstance()).commit();
                break;
            case R.layout.fragment_add:
                toolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.toolbar_slide_down));
                TransitionManager.beginDelayedTransition(toolbar);
                toolbar.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, AddFragment.newInstance()).commit();
                break;
            case R.layout.fragment_password:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if(fragment instanceof AddFragment){
            switchFragment(R.layout.fragment_home);
        }else {
            super.onBackPressed();
        }
    }
}
