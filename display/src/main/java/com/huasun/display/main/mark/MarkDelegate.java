package com.huasun.display.main.mark;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hmy.popwindow.PopItemAction;
import com.hmy.popwindow.PopWindow;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.core.util.Config;
import com.huasun.display.R;
import com.huasun.display.R2;
import com.huasun.display.entity.MessagetoServer;
import com.huasun.display.launcher.LauncherDelegate;
import com.huasun.display.main.mark.view.MarkDisplay;
import com.huasun.display.recycler.MultipleFields;
import com.huasun.display.recycler.MultipleItemEntity;
import com.huasun.display.recycler.MultipleRecyclerAdapter;
import com.huasun.display.refresh.PagingBean;
import com.huasun.display.refresh.RefreshHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.smartown.tableview.library.TableView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import win.smartown.android.library.tableLayout.TableAdapter;
import win.smartown.android.library.tableLayout.TableLayout;

import android.widget.PopupWindow;

/**
 * author:songwenming
 * Date:2019/9/26
 * Description:
 */
public class MarkDelegate extends BottomItemDelegate {

    private PopWindow popWindow;
    private TableView tableView;
    private Button btn_ok;
    private String server= Config.serverIp;
    private int port=5672;
    private String username="client";
    private String password="client";
    private String exchangeName="display-to-server-exchange";
    private String routingKey="display-to-server-routing-key";
    private String name;//姓名
    private String gun;//枪械种类
    private String shootingPose;//射击姿势
    private String shootingDistance;//射击距离
    private int countof10=0;//十环数目
    private int countof9=0;
    private int countof8=0;
    private int countof7=0;
    private int countof6=0;
    private int countof5=0;
    private int countofMiss=0;//脱靶数目

    private int bulletNumber;
    private double totalRingNumber;

    private static String COMMAND="COMMAND";
    private ArrayList<MultipleItemEntity> medicineHistoryList=new ArrayList<>();
    int llcPersonDataHeight=0;
    int tvPersonDataHeight=0;
    int btFinishButtonHeight=0;
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
//    @BindView(R2.id.edit_department)
//    EditText mDepartment=null;
//    @BindView(R2.id.edit_gun)
//    EditText mGun=null;
    @BindView(R2.id.edit_bullet)
    EditText mBullet=null;
    @BindView(R2.id.edit_target_number)
    EditText mTargetNumber=null;

    @BindView(R2.id.btn_finish_shooting)
    Button mFinishButton=null;

    @BindView(R2.id.edit_group_number)
    EditText mGroupNumber=null;

//    @BindView(R2.id.tv_time)
//    TextView mTime=null;
    @OnClick(R2.id.btn_finish_shooting)
    void onFinishShooting(){

        //int bulletCount=commandJson.getInteger("bullet_count");;//获得子弹数目
        String userName=mName.getText().toString();
        final View customView = View.inflate((Context) Latte.getConfiguration(ConfigKeys.ACTIVITY), R.layout.end_shoot, null);//获得弹出框里要放的view

        TextView mName=(TextView) customView.findViewById(R.id.tv_name);
        TextView mGun=(TextView)customView.findViewById(R.id.tv_gun_type);
        TextView mShootDistance=(TextView)customView.findViewById(R.id.tv_shoot_distance);
        TextView mShootPose=(TextView)customView.findViewById(R.id.tv_shoot_pose);
        TextView mBulletCount=(TextView)customView.findViewById(R.id.tv_bullet_count);


        tableView = (TableView) customView.findViewById(R.id.table);
        btn_ok=(Button)customView.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
                new send().execute();
                start(new LauncherDelegate(),1);
                Latte.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Resources resources=getActivity().getResources();;//获取本地资源
                        RelativeLayout relativeLayout=getActivity().findViewById(R.id.layout_launch);
                        Boolean connect_to_rabbit=Latte.getConfiguration(ConfigKeys.CONNECT_RABBIT);
                        if(connect_to_rabbit) {
                            relativeLayout.setBackground(resources.getDrawable(R.drawable.connect_rab));
                        }else {
                            relativeLayout.setBackground(resources.getDrawable(R.drawable.disconnect_rab));
                        }
                    }
                });
            }
        });
        tableView.clearTableContents()
                .setHeader("10环", "9环", "8环", "7环", "6环", "5环", "脱靶")
                .addContent("1", "2", "5", "2", "5", "2", "5")
                .refreshTable();


        MultipleRecyclerAdapter multipleRecyclerAdapter= (MultipleRecyclerAdapter)mRecyclerView.getAdapter();
        int ringSum= 0;
        if(multipleRecyclerAdapter!=null) {
            List<MultipleItemEntity> entityList = multipleRecyclerAdapter.getData();
            int count = entityList.size();
            for (int i = 1; i < count; i++) {
                String RINGNUMBER_STR=entityList.get(i).getField(MultipleFields.RINGNUMBER).toString();
                double RINGNUMBER=Double.parseDouble(RINGNUMBER_STR);

                ringSum = (int) (ringSum + Math.floor(RINGNUMBER));
            }
        }
        mName.setText(name);
        mGun.setText("枪械种类:"+gun);
        mShootPose.setText("射击姿势:"+shootingPose);
        mShootDistance.setText("射击距离"+shootingDistance);
        mBulletCount.setText("子弹数量:"+bulletNumber);

        popWindow = new PopWindow.Builder((Activity) Latte.getConfiguration(ConfigKeys.ACTIVITY))
                .setStyle(PopWindow.PopWindowStyle.PopUp)
               // .setTitle("射击成绩报告")
                .setTotalRingNumber(ringSum)
                .setTotalBulletNumber(bulletNumber)
                .addContentView(customView)
                .addItemAction(new PopItemAction(Html.fromHtml("<font color=\'#000000\'><b>确定</b></font>"), PopItemAction.PopItemStyle.Normal, new PopItemAction.OnClickListener() {
                    @Override
                    public void onClick() {
                        new send().execute();
                        start(new LauncherDelegate(),1);
                        Latte.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Resources resources=getActivity().getResources();;//获取本地资源
                                RelativeLayout relativeLayout=getActivity().findViewById(R.id.layout_launch);
                                Boolean connect_to_rabbit=Latte.getConfiguration(ConfigKeys.CONNECT_RABBIT);
                                if(connect_to_rabbit) {
                                    relativeLayout.setBackground(resources.getDrawable(R.drawable.connect_rab));
                                }else {
                                    relativeLayout.setBackground(resources.getDrawable(R.drawable.disconnect_rab));
                                }
                            }
                        });
                        //startWithPop(new LauncherDelegate());
                        //Toast.makeText((Activity) Latte.getConfiguration(ConfigKeys.ACTIVITY), "完成打靶", Toast.LENGTH_SHORT).show();
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
        mName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        //mRefreshHandler.injectDataIntoRecy("index");
    }
    public void initMarkData(String markJson)
    {
        this.markJson=markJson;
    }
    public void initBasicData(String command) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
//      mTime.setText(format.format(date));
        final JSONObject commandJson= JSON.parseObject(command).getJSONObject("data");
        mName.setText(commandJson.getString("name"));
        mName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        });
        mName.setInputType(InputType.TYPE_NULL);
//        mDepartment.setText(commandJson.getString("department"));
//        mDepartment.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                return true;
//            }
//        });
//        mDepartment.setInputType(InputType.TYPE_NULL);
//        mGun.setText(commandJson.getString("shooting_gun"));
//        mGun.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                return true;
//            }
//        });
//        mGun.setInputType(InputType.TYPE_NULL);
        gun=commandJson.getString("shooting_gun");
        name=commandJson.getString("name");
        shootingPose=commandJson.getString("shooting_pose");
        shootingDistance=commandJson.getString("shooting_distance");
        bulletNumber=commandJson.getInteger("bullet_count");

        mBullet.setText(commandJson.getInteger("bullet_count")+"");//将int　转为CharSequence
        mBullet.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        });
        mBullet.setInputType(InputType.TYPE_NULL);
        mGroupNumber.setText(commandJson.getString("group_number"));
        mGroupNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        });
        mGroupNumber.setInputType(InputType.TYPE_NULL);
        mTargetNumber.setText(commandJson.getString("target_number"));//靶位编号
        mTargetNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        });
        mTargetNumber.setInputType(InputType.TYPE_NULL);
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
//                        lp.setMargins(10, -(llcPersonDataHeight + srlMarkHeight  + tvPersonDataHeight-8), 0, 5);
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
            markAttachListener.setMarkDelegate(this);
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
    //          2020.10.30添加可能引发错误
    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }
    //          2020.10.30添加可能引发错误


    //将第一行作为标题


    @Override
    public void onPause() {
        super.onPause();
        if(popWindow!=null){
            popWindow.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(popWindow!=null){
            popWindow.dismiss();
        }
    }
}
