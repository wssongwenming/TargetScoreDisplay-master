package com.huasun.display.main.mark;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.display.R;
import com.huasun.display.R2;
import com.huasun.display.database.UserProfile;
import com.huasun.display.main.mark.view.MarkDisplay;
import com.huasun.display.recycler.MultipleItemEntity;
import com.huasun.display.refresh.PagingBean;
import com.huasun.display.refresh.RefreshHandler;
import com.huasun.display.sign.ISignListener;
import com.huasun.display.sign.SignInByPassword.SignInByPassDelegate;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * author:songwenming
 * Date:2019/9/26
 * Description:
 */
public class MarkDelegate extends BottomItemDelegate {
    private static String COMMAND="COMMAND";
    private ArrayList<MultipleItemEntity> medicineHistoryList=new ArrayList<>();
    int llcPersonDataHeight=0;
    int tvPersonDataHeight=0;
    int srlMarkHeight=0;
    String markJson="";
    @BindView(R2.id.surface_pan)
    MarkDisplay markDisplay=null;
    @BindView(R2.id.tv_person_data)
    AppCompatTextView mTvPersonData=null;
    @BindView(R2.id.llc_person_data)
    LinearLayoutCompat mLlcPersonData=null;
    @BindView(R2.id.rv_mark)
    RecyclerView mRecyclerView=null;
    @BindView(R2.id.srl_mark)
    SwipeRefreshLayout mRefreshLayout=null;
    @BindView(R2.id.edit_name)
    EditText mName=null;
    @BindView(R2.id.edit_department)
    EditText mDepartment=null;
    @BindView(R2.id.edit_gun)
    EditText mGun=null;
    @BindView(R2.id.edit_bullet)
    EditText mBullet=null;
    @BindView(R2.id.edit_target_number)
    EditText mTargetNumber=null;
    @BindView(R2.id.edit_group_number)
    EditText mGroupNumber=null;
    private String command;
    public RefreshHandler mRefreshHandler;
    private void initRefreshLayout(){
        mRefreshLayout.setColorSchemeResources(
                android.R.color.background_dark,
                android.R.color.background_light
        );
        mRefreshLayout.setProgressViewOffset(true,llcPersonDataHeight+tvPersonDataHeight/2,llcPersonDataHeight+tvPersonDataHeight/2+200);
    }
    public static MarkDelegate newInstance(String command){
        final Bundle args = new Bundle();
        args.putString(COMMAND,command);
        final MarkDelegate delegate = new MarkDelegate();
        delegate.setArguments(args);
        return delegate;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            command = args.getString(COMMAND);
        }
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        initRefreshLayout();
        initRecyclerView();
        initBasicData(command);
        //mRefreshHandler.injectDataIntoRecy("index");
    }
    public void initMarkData(String markJson)
    {
        this.markJson=markJson;
    }
    public void initBasicData(String command) {
        final JSONObject commandJson= JSON.parseObject(command).getJSONObject("data");
        mName.setText(commandJson.getString("name"));
        mDepartment.setText(commandJson.getString("department"));
        mGun.setText(commandJson.getString("shooting_gun"));
        mBullet.setText(commandJson.getInteger("bullet_count")+"");//将int　转为CharSequence
        mGroupNumber.setText(commandJson.getString("group_number"));
        mTargetNumber.setText(commandJson.getString("target_number"));
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(linearLayoutManager);

    }
    @Override
    public Object setLayout() {
        return R.layout.delegate_mark;
    }
    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        mRefreshHandler=RefreshHandler.create(mRefreshLayout,mRecyclerView,new MarkDataConverter(),new PagingBean());
        ViewTreeObserver observer = mLlcPersonData.getViewTreeObserver();
         if(observer!=null) {
             observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                 @Override
                 public boolean onPreDraw() {

                     if(mLlcPersonData!=null) {
                         llcPersonDataHeight = mLlcPersonData.getHeight();
                     }
                     if(mTvPersonData!=null) {
                         tvPersonDataHeight = mTvPersonData.getHeight();
                     }
                     if(mRefreshLayout!=null) {
                         srlMarkHeight = mRefreshLayout.getHeight();
                     }

                     if( mTvPersonData!=null) {
                         LinearLayoutCompat.LayoutParams lp = (LinearLayoutCompat.LayoutParams) mTvPersonData.getLayoutParams();
                         lp.setMargins(30, -(llcPersonDataHeight + srlMarkHeight + 20 + tvPersonDataHeight / 2), 0, 0);
                         mTvPersonData.setLayoutParams(lp);
                     }
                     return true;

                 }
             });
         }

    }
    private IMarkAttachListener markAttachListener=null;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof IMarkAttachListener)
        {
            markAttachListener=(IMarkAttachListener)activity;
            markAttachListener.setMarkDelegate(this);
        }
    }
    public MarkDisplay getMarkDisplay() {
        return markDisplay;
    }
}
