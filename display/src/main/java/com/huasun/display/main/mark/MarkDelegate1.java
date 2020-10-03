package com.huasun.display.main.mark;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hmy.popwindow.PopItemAction;
import com.hmy.popwindow.PopWindow;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.display.R;
import com.huasun.display.R2;
import com.huasun.display.entity.MessagetoServer;
import com.huasun.display.main.mark.view.MarkDisplay;
import com.huasun.display.recycler.MultipleFields;
import com.huasun.display.recycler.MultipleItemEntity;
import com.huasun.display.recycler.MultipleRecyclerAdapter;
import com.huasun.display.refresh.PagingBean;
import com.huasun.display.refresh.RefreshHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import android.widget.PopupWindow;

/**
 * author:songwenming
 * Date:2019/9/26
 * Description:
 */
public class MarkDelegate1 extends BottomItemDelegate {
    private String server="192.168.1.3";
    private int port=5672;
    private String username="client";
    private String password="client";
    private String exchangeName="display-to-server-exchange";
    private String routingKey="display-to-server-routing-key";

    private static String COMMAND="COMMAND";
    private ArrayList<MultipleItemEntity> medicineHistoryList=new ArrayList<>();
    int llcPersonDataHeight=0;
    int tvPersonDataHeight=0;
    int srlMarkHeight=0;
    private String target_index="";
    private String group_index="";
    private String traineeId="";
    String markJson="";
    @BindView(R2.id.surface_pan)
    MarkDisplay markDisplay=null;
//    @BindView(R2.id.tv_person_data)
//    AppCompatTextView mTvPersonData=null;
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
//    @BindView(R2.id.tv_time)
//    TextView mTime=null;
    @OnClick(R2.id.btn_finish_shooting)
    void onClickSignIn(){
        int bulletCount=Integer.parseInt(mBullet.getText().toString());//获得子弹数目
        final View customView = View.inflate((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY), R.layout.finish_shooting_summary, null);//获得弹出框里要放的view
        AppCompatTextView textView=customView.findViewById(R.id.tv_shoot_mark);
        MultipleRecyclerAdapter multipleRecyclerAdapter= (MultipleRecyclerAdapter)mRecyclerView.getAdapter();
        double ringSum= (float) 0.0;
        if(multipleRecyclerAdapter!=null) {
            List<MultipleItemEntity> entityList = multipleRecyclerAdapter.getData();
            int count = entityList.size();
            for (int i = 1; i < count; i++) {
                ringSum = ringSum + Double.parseDouble(entityList.get(i).getField(MultipleFields.RINGNUMBER).toString());
            }
        }
        textView.setText("您的打靶总成绩为："+ringSum+"环,平均成绩为："+ringSum/bulletCount+"环");
        PopWindow popWindow = new PopWindow.Builder((Activity) Latte.getConfiguration(ConfigKeys.ACTIVITY))
                .setStyle(PopWindow.PopWindowStyle.PopUp)
                .setTitle("打靶成绩报告")
                .addContentView(customView)
                //.addItemAction(new PopItemAction("确定", PopItemAction.PopItemStyle.Cancel))
                .addItemAction(new PopItemAction("确定", PopItemAction.PopItemStyle.Normal, new PopItemAction.OnClickListener() {
                    @Override
                    public void onClick() {
                        new send().execute();
                        Toast.makeText((Activity) Latte.getConfiguration(ConfigKeys.ACTIVITY), "完成打靶", Toast.LENGTH_SHORT).show();
                    }
                }))
                .create();
        popWindow.show();


    }
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        //mTime.setText(format.format(date));
        final JSONObject commandJson= JSON.parseObject(command).getJSONObject("data");
        mName.setText(commandJson.getString("name"));
        mDepartment.setText(commandJson.getString("department"));
        mGun.setText(commandJson.getString("shooting_gun"));
        mBullet.setText(commandJson.getInteger("bullet_count")+"");//将int　转为CharSequence
        mGroupNumber.setText(commandJson.getString("group_number"));
        mTargetNumber.setText(commandJson.getString("target_number"));//靶位编号
        target_index=commandJson.getString("target_number");//将靶位编号取出打靶完毕后，用于将打靶完毕信息返回给ｓｅｒｖｅｒ
        group_index=commandJson.getString("group_number");//\
        traineeId=commandJson.getString("userId");
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
//        if(observer!=null) {
//            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//
//                    if(mLlcPersonData!=null) {
//                        llcPersonDataHeight = mLlcPersonData.getHeight();
//                    }
//                    if(mTvPersonData!=null) {
//                        tvPersonDataHeight = mTvPersonData.getHeight();
//                    }
//                    if(mRefreshLayout!=null) {
//                        srlMarkHeight = mRefreshLayout.getHeight();
//                    }
//
//                    if( mTvPersonData!=null) {
//                        LinearLayoutCompat.LayoutParams lp = (LinearLayoutCompat.LayoutParams) mTvPersonData.getLayoutParams();//
//                        lp.setMargins(10, -(llcPersonDataHeight + srlMarkHeight  + tvPersonDataHeight-10), 0, 0);
//                        mTvPersonData.setLayoutParams(lp);
//                    }
//                    return true;
//
//                }
//            });
//        }

    }
    private IMarkAttachListener markAttachListener=null;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof IMarkAttachListener)
        {
            markAttachListener=(IMarkAttachListener)activity;
            //markAttachListener.setMarkDelegate(this);
        }
    }
    public MarkDisplay getMarkDisplay() {
        return markDisplay;
    }
    private class send extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(server);
                factory.setUsername(username);
                factory.setPassword(password);
                factory.setPort(port);
                MessagetoServer message=new MessagetoServer();
                message.setCode(0);//code=0表示打靶完毕，
                message.setGroup_index(Integer.parseInt(group_index.trim()));
                message.setTarget_index(Integer.parseInt(target_index.trim()));
                message.setTraineeId(traineeId);
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.basicPublish(exchangeName, routingKey, null,JSONObject.toJSONString(message).getBytes());
                channel.close();
                connection.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }
}
