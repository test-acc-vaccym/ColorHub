package cheetatech.com.colorhub;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cheetatech.com.colorhub.adapters.SaveListAdapter;
import cheetatech.com.colorhub.adapters.ViewPagerAdapter;
import cheetatech.com.colorhub.ads.AdsUtils;
import cheetatech.com.colorhub.dialog.SaveDialog;
import cheetatech.com.colorhub.listeners.IOnFocusListenable;
import cheetatech.com.colorhub.models.Model;
import cheetatech.com.colorhub.realm.RealmX;
import cheetatech.com.colorhub.realm.SavedObject;
import io.realm.RealmList;
import layout.ColorPicker1;
import layout.ColorPicker3;

public class ColorPickerActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener , ColorPicker1.OnColorListener,SaveDialog.OnSaveListener{
    private TabLayout tabLayout = null;
    private ViewPager viewPager = null;
    private ColorPicker1 ColorPicker_1 = null;

    private int currentPosition = 0;
    public static boolean erase = false;
    @BindView(R.id.saved_color_layout)
    RelativeLayout mSavedLayout;

    @BindView(R.id.image_layout)
    RelativeLayout mImageLayout;

    @BindView(R.id.image_up_down)
    ImageView upDownImage;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.fabAdd)
    FloatingActionButton fabAddButton;

    private SaveListAdapter mAdapter = null;

    private List<Model> listModel = new ArrayList<>();

    Animation slideUp, slideDown, fadeIn, fadeOut ;
    private int i = 0;
    private int width, height, imageHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        ButterKnife.bind(this);

        slideUp = AnimationUtils.loadAnimation(this,R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this,R.anim.slide_down);
        fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this,R.anim.fade_out);

        if(fabAddButton.getVisibility() == View.VISIBLE)
            fabAddButton.setVisibility(View.INVISIBLE);

        ViewTreeObserver observer = mSavedLayout.getViewTreeObserver();
        if(observer.isAlive()){
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mSavedLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mSavedLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    width = mSavedLayout.getMeasuredWidth();
                    height = mSavedLayout.getMeasuredHeight();

                    imageHeight = mImageLayout.getMeasuredHeight();
                    if(imageHeight == -1)
                        imageHeight = 100;
                    Log.e("TAG","ImageLayout :  " + imageHeight);

                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            width, imageHeight);
                    lp.setMargins(0, width, 0, 0);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    mSavedLayout.setLayoutParams(lp);
                }
            });
        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_action_back_button);
        getSupportActionBar().setTitle("ColorHub");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.pager);
        setUpViewPager(viewPager);

        tabLayout = (TabLayout)findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(this);

        RealmX.list();
        loadAdapters();
    }

    private void loadAdapters() {
        listModel.clear();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new SaveListAdapter(listModel);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @OnClick(R.id.fabAdd) void fabAddClick(){
        if(listModel.size() != 0)
            (SaveDialog.newInstance(this)).show(getSupportFragmentManager(),getString(R.string.save_dialog));
    }

    @OnClick(R.id.image_layout) void updownImageClick(){
        if(!isOpen()){ // Will Open
            openLayout();
            return;
        }
        if(isOpen()){
            closeLayout();
            return;
        }
    }
    private boolean isOpen(){
        int height = mSavedLayout.getMeasuredHeight();
        if(height > 150)
            return true;
        else
            return false;
    }
    private void closeLayout() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            upDownImage.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_up));
        } else {
            upDownImage.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_action_up));
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                width, height);
        lp.setMargins(0, width, 0, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        if(fabAddButton.getVisibility() == View.VISIBLE)
            fabAddButton.setVisibility(View.INVISIBLE);


        mSavedLayout.setLayoutParams(lp);
        mSavedLayout.clearAnimation();
        mSavedLayout.startAnimation(slideDown);
        slideDown.setFillAfter(true);
        slideDown.setFillEnabled(true);
        slideDown.setFillBefore(true);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mSavedLayout.clearAnimation();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        width, imageHeight);
                lp.setMargins(0, width, 0, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mSavedLayout.setLayoutParams(lp);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void openLayout() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            upDownImage.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_down));
        } else {
            upDownImage.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_action_down));
        }

        if(fabAddButton.getVisibility() == View.INVISIBLE)
            fabAddButton.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                width, height);
        lp.setMargins(0, width, 0, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mSavedLayout.setLayoutParams(lp);

        mSavedLayout.clearAnimation();
        mSavedLayout.startAnimation(slideUp);;
        slideUp.setFillAfter(true);
        slideUp.setFillEnabled(true);
        slideUp.setFillBefore(true);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.e("TAG", "onAnimationEnd: Click");
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        width, height);
                lp.setMargins(0, width, 0, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mSavedLayout.setLayoutParams(lp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public  void setUpViewPager(ViewPager viewPager)
    {
        if(ColorPicker_1 == null)
            ColorPicker_1 = ColorPicker1.newInstance(this);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ColorPicker_1,"ColorPicker X");
        //adapter.addFragment(new ColorPicker2(),"ColorPicker2");
        adapter.addFragment(ColorPicker3.newInstance(this),"ColorPicker Y");
        viewPager.setAdapter(adapter);
    }
    @Override
    protected void onDestroy() {
        RealmX.closeRealm();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        Fragment currentFragment = getVisibleFragment();
        if(currentFragment != null)
        {
            if(currentFragment instanceof  IOnFocusListenable)
                ((IOnFocusListenable)currentFragment).onWindowFocusChanged(hasFocus);
        }

    }
    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = ColorPickerActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        currentPosition =  tab.getPosition();

        tabLayout.getTabAt(currentPosition).select();
        viewPager.setCurrentItem(currentPosition);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onAddColor(String color) {
        if(!isInList(this.listModel,color)){
            AdsUtils.getInstance().increaseInteraction();
            this.listModel.add(new Model(color));
            mAdapter.notifyDataSetChanged();
            onMessage(getString(R.string.success_add_color));
        }else{
            onMessage(getString(R.string.allready_added_color));
        }
    }

    private void onMessage(String msg){
        Toast.makeText(ColorPickerActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isInList(List<Model> models,String color){
        boolean hold = false;
        for (Model m: models) {
            if(m.getColorCode().equals(color))
                return true;
        }
        return hold;
    }

    @Override
    public void onSavedName(String name) {
        SavedObject object = new SavedObject();
        object.setName(name);
        RealmList<Model> mList = new RealmList<Model>();
        mList.addAll(this.listModel);
        object.setList(mList);
        RealmX.save(object);
        this.listModel.clear();
        this.mAdapter.notifyDataSetChanged();
        closeLayout();
        AdsUtils.getInstance().increaseInteraction();
    }
}