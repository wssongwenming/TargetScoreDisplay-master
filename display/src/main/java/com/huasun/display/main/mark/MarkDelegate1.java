package com.huasun.display.main.mark;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.core.net.RestClient;
import com.huasun.core.net.callback.ISuccess;
import com.huasun.core.ui.launcher.LauncherHolderCreator;
import com.huasun.core.util.callback.CallbackManager;
import com.huasun.core.util.callback.CallbackType;
import com.huasun.core.util.callback.IGlobalCallback;
import com.huasun.core.util.storage.LattePreference;
import com.huasun.display.R;
import com.huasun.display.R2;
import com.huasun.display.database.UserProfile;
import com.huasun.display.main.mark.view.MarkDisplay;
import com.huasun.display.recycler.ItemType;
import com.huasun.display.recycler.MultipleFields;
import com.huasun.display.recycler.MultipleItemEntity;
import com.huasun.display.recycler.MultipleRecyclerAdapter;
import com.huasun.display.refresh.PagingBean;
import com.huasun.display.refresh.RefreshHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * author:songwenming
 * Date:2019/9/26
 * Description:
 */
public class MarkDelegate1 extends BottomItemDelegate {

    String medicineName=null;
    //private List<MedicinePlanInfo> list =new ArrayList<>();
    //private MyElvAdapterForIndex myAdapter;
    private Context context;
    //显示用药计划
    //private Map<String, List<MedicinePlan>> dataset = new HashMap<>();
    private  ArrayList<String> parentList=new ArrayList<>();
    //MedicinePlanExpandableListViewAdapter medicinePlanExpandableListViewAdapter;
    //显示用药历史
    ArrayList<MultipleItemEntity> medicineHistoryList=new ArrayList<>();
    MultipleRecyclerAdapter medicineHistoryRecyclerViewAdapter;
    //
    @BindView(R2.id.banner_index)
    ConvenientBanner mConvenientBanner=null;

    @BindView(R2.id.rv_index_history)
    RecyclerView mRecyclerViewHistory=null;
    @BindView(R2.id.elv_index_plan)
    ExpandableListView mExpandableListView=null;
    @BindView(R2.id.srl_index)
    SwipeRefreshLayout mRefreshLayout=null;

    private RefreshHandler mRefreshHandler=null;
    //private Set<SwipeListLayout> sets = new HashSet();
    //private ConvenientBanner<Integer> mConvenientBanner=null;
    private static final ArrayList<Integer> INTEGERS=new ArrayList<>();

    private void initRefreshLayout(){
        mRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_red_light,
                android.R.color.holo_red_light
        );
        mRefreshLayout.setProgressViewOffset(true,120,300);
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        if(mRefreshLayout!=null) {
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh();
                }
            });
        }
        UserProfile userProfile= (UserProfile) Latte.getConfigurations().get(ConfigKeys.LOCAL_USER);
        if(userProfile==null){
            //startWithPop(new SignInDelegate());
        }else {
            //tel=Long.toString(userProfile.getTel());
            //boxId= LattePreference.getBoxId();
           // if(boxId.equalsIgnoreCase("未设置boxId"))
          //  {
         //       Toast.makeText(getContext(),"请添加药箱，并绑定当前药箱",Toast.LENGTH_LONG).show();
        //    }

        }
        CallbackManager.getInstance()
/*                .addCallback(CallbackType.ON_SCAN, new IGlobalCallback() {

                    @Override
                    public void executeCallback(@Nullable final Object args){
                        String target=Latte.getConfiguration(ConfigKeys.SCANFOR);

                        if(target.equalsIgnoreCase("medicine")) {
                            //Toast.makeText(getContext(),"扫描到的二维码"+args,Toast.LENGTH_LONG).show();
                            RestClient.builder()
                                    .clearParams()
                                    .url(UploadConfig.API_HOST + "/api/get_drugs_by_code")
                                    .params("code", args.toString().trim())
                                    .success(new ISuccess() {
                                        @Override
                                        public void onSuccess(String response) {
                                            Log.d("responseforscan", response);
                                            JSONObject object = JSON.parseObject(response);
                                            int code = object.getIntValue("code");
                                            if (code == 1) {
                                                JSONObject detail = object.getJSONObject("detail");
                                                medicineName = detail.getString("name");
                                                HandAddDelegateForIndex delegate = HandAddDelegateForIndex.newInstance(args.toString().trim(), medicineName);
                                                getParentDelegate().start(delegate);
                                                Log.d("medicinename", "name=" + medicineName);
                                            } else {
                                                HandAddDelegateForIndex delegate = HandAddDelegateForIndex.newInstance(args.toString().trim(), "");
                                                getParentDelegate().start(delegate);
                                            }
                                        }
                                    })
                                    .build()
                                    .get();
                        }else if(target.equalsIgnoreCase("boxId")){
                            AddMedicineBoxByScanDelegate delegate=AddMedicineBoxByScanDelegate.newInstance(args.toString());
                            getParentDelegate().start(delegate);
                        }



                    }
                })*/
                .addCallback(CallbackType.ON_BIND_BOXID, new IGlobalCallback() {
                    @Override
                    public void executeCallback(@Nullable Object args) {
                        //Toast.makeText(getContext(),"boxId="+ LattePreference.getBoxId(),Toast.LENGTH_LONG).show();
                    }
                }).addCallback(CallbackType.ON_GET_MEDICINE_PLAN_INDEX, new IGlobalCallback() {
            @Override
            public void executeCallback(@Nullable Object args) {
               // getMedicinePlan();
/*                RestClient.builder()
                        .clearParams()
                        .url(UploadConfig.API_HOST+"/api/get_plan")
                        //.url("medicine_plan")
                        .params("tel",tel)
                        .params("boxId",LattePreference.getBoxId())
                        .success(new ISuccess() {
                            @Override
                            public void onSuccess(String response) {
                                JSONObject object= JSON.parseObject(response);
                                int code=object.getIntValue("code");
                                if(code==1) {
                                    //Toast.makeText(getContext(),"该刷新了",Toast.LENGTH_LONG).show();
                                    initData(response);
                                    myAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .build()
                        .get();*/
            }
        }).addCallback(CallbackType.ON_CHANGE_BOXID_FOR_HISTORY, new IGlobalCallback() {
            @Override
            public void executeCallback(@Nullable Object args) {
               // getMedicineHistory();
/*                RestClient.builder()
                        .clearParams()
                        .url(UploadConfig.API_HOST+"/api/get_plan")
                        //.url("medicine_plan")
                        .params("tel",tel)
                        .params("boxId",LattePreference.getBoxId())
                        .success(new ISuccess() {
                            @Override
                            public void onSuccess(String response) {
                                com.alibaba.fastjson.JSONObject object= JSON.parseObject(response);
                                int code=object.getIntValue("code");
                                if(code==1) {
                                    initData(response);
                                    myAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .build()
                        .get();*/
            }
        });

        //mRefreshHandler=RefreshHandler.create(mRefreshLayout,mRecyclerViewHistory,
        //mExpandableListView,new IndexDataConverter(),this.getParentDelegate(),null);
        mExpandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View firstView = view.getChildAt(firstVisibleItem);
                if (firstVisibleItem == 0 && (firstView == null || firstView.getTop() == 0)) {
                    mRefreshLayout.setEnabled(true);
                } else {
                    mRefreshLayout.setEnabled(false);
                }
            }
        });
    }

/*    private void initGridView(){
        mData = new LinkedList<>();
        mData.add(new Icon(R.mipmap.icon_medicine_scan_add, "扫码添加"));
        mData.add(new Icon(R.mipmap.icon_medicine_hand_add, "手动添加"));
        mData.add(new Icon(R.mipmap.icon_mdicine_mine, "我的药品"));
        mData.add(new Icon(R.mipmap.icon_medicine_take_plan, "用药计划"));
        mData.add(new Icon(R.mipmap.icon_medicine_take_history, "用药记录"));
        mData.add(new Icon(R.mipmap.main_health, "身体状况"));
        mAdapter = new MyAdapter<Icon>(mData, R.layout.item_grid_icon) {
            @Override
            public void bindView(ViewHolder holder, Icon icon) {
                holder.setImageResource(R.id.img_icon, icon.getiId());
                holder.setText(R.id.txt_icon,icon.getiName());
            }
        };
        buttonGrid.setAdapter(mAdapter);
        buttonGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0://点击了“扫码添加”
                        getParentDelegate().startScanWithCheck(getParentDelegate());
                        break;
                    case 1://点击了“手动添加”
                        getParentDelegate().start(new HandAddDelegate());
                        break;
                    case 2://点击了“我的药品”
                        getParentDelegate().start(new MedicineMineDelegate());
                        break;
                    case 3://点击了“用药计划”
                        getParentDelegate().start(new MedicineTakePlanDelegate());
                        break;
                    case 4://点击了“用药记录”
                        getParentDelegate().start(new MedicineTakeHistoryDelegate());
                        break;
                    case 5://点击了“用药记录”
                        getParentDelegate().start(new BodySituationDelegate());
                        break;
                }
            }
        });
    }*/
/*    private void initBanner(){
        INTEGERS.add(R.mipmap.banner_01);
        //INTEGERS.add(R.mipmap.banner_02);
        INTEGERS.add(R.mipmap.banner_03);
        mConvenientBanner
                .setPages(new LauncherHolderCreator(),INTEGERS)
                .setPageIndicator(new int[]{R.drawable.dot_normal,R.drawable.dot_focus})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .setCanLoop(false);
    }*/
    private void initRecyclerView(){
        final LinearLayoutManager linearLayoutManager_history=new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        mRecyclerViewHistory.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerViewHistory.setLayoutManager(linearLayoutManager);
    }
    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        initRecyclerView();
        //initGridView();
        initRefreshLayout();
        //initBanner();
        //getMedicinePlan();
        //getMedicineHistory();
        //mRefreshHandler.firstPage_medicine_history(UploadConfig.API_HOST+"/api/get_history",tel,1,5);
        //mRefreshHandler.get_medicine_plan(UploadConfig.API_HOST+"/api/get_plan",tel,boxId);

    }
    @Override
    public Object setLayout() {
        return R.layout.delegate_mark1;
    }

/*    private void getMedicinePlan(){
        Log.d("Latteboxid", LattePreference.getBoxId());
        RestClient.builder()
                .clearParams()
                .url(UploadConfig.API_HOST+"/api/get_plan")
                //.url("medicine_plan")
                .params("tel",tel)
                .params("boxId",LattePreference.getBoxId())
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        com.alibaba.fastjson.JSONObject object= JSON.parseObject(response);
                        int code=object.getIntValue("code");
                        if(code==1) {
                            initData(response);
                            myAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .build()
                .get();
    }
    private void getMedicineHistory()
    {
        RestClient.builder()
                .clearParams()
                .url(UploadConfig.API_HOST+"/api/get_history")
                //.url("medicine_plan")
                .params("tel",tel)
                .params("boxId",LattePreference.getBoxId())
                .params("page",0)
                .params("count",5)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        com.alibaba.fastjson.JSONObject object= JSON.parseObject(response);
                        int code=object.getIntValue("code");
                        if(code==1) {
                            convert_response_to_history(response);
                            medicineHistoryRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .build()
                .get();
    }*/

/*    private void convert_response_to_plan(String jsonString){
        if(jsonString!=null) {
            dataset.clear();
            parentList.clear();
            final JSONObject jsonObject = JSON.parseObject(jsonString);
            //String tel=jsonObject.getString("tel");
            final com.alibaba.fastjson.JSONObject data = jsonObject.getJSONObject("detail");
            final JSONArray dataArray = data.getJSONArray("planlist");
            int size = dataArray.size();
            for (int i = 0; i < size; i++) {

                JSONObject jsondata = (JSONObject) dataArray.get(i);
                parentList.add(jsondata.getString("time"));
                JSONArray jsonArray = jsondata.getJSONArray("plans");
                int lenght = jsonArray.size();
                List<MedicinePlan> childrenList = new ArrayList<>();
                for (int j = 0; j < lenght; j++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(j);
                    MedicinePlan medicinePlanModel = new MedicinePlan();
                    medicinePlanModel.setAtime(jsonObject1.getString("atime"));
                    medicinePlanModel.setEndRemind(jsonObject1.getString("endRemind"));
                    medicinePlanModel.setId(jsonObject1.getString("id"));
                    medicinePlanModel.setMedicineUseCount(jsonObject1.getIntValue("medicineUseCount"));
                    //medicinePlanModel.setDayInterval(jsonObject1.getInteger("dayInterval"));
                    medicinePlanModel.setStartRemind(jsonObject1.getString("startRemind"));
                    medicinePlanModel.setMedicineName(jsonObject1.getString("medicineName"));
                    medicinePlanModel.setBoxId(jsonObject1.getString("boxId"));
                    childrenList.add(medicinePlanModel);
                }
                dataset.put(parentList.get(i), childrenList);
            }
        }
    }
    private void convert_response_to_history(String jsonString){
        medicineHistoryList.clear();
        if(jsonString!=null) {

            String medicineId = null;
            String medicineName = null;
            String medicineUseTime = null;
            int historyType=-1;
            String medicineHistoryType=null;
            String tel = null;
            String boxId = null;
            String id = null;
            if (jsonString != null) {
                JSONObject jsonobject = JSON.parseObject(jsonString);
                JSONObject jsonobject1 = jsonobject.getJSONObject("detail");
                JSONArray jsonarray = jsonobject1.getJSONArray("histories");
                int size = jsonarray.size();
                for (int i = 0; i < size; i++) {
                    JSONObject jsonobject2 = jsonarray.getJSONObject(i);
                    boxId = jsonobject2.getString("boxId");
                    medicineName = jsonobject2.getString("medicineNames");
                    medicineUseTime = jsonobject2.getString("medicineUseTime");
                    historyType=jsonobject2.getIntValue("status");
                    switch (historyType) {
                        case 1:
                            medicineHistoryType = "药盒按时服用:";
                            break;
                        case 2:
                            medicineHistoryType = "药箱按时服用:";
                            break;
                        case 3:
                            medicineHistoryType = "药盒未按时服用:";
                            break;
                        case 4:
                            medicineHistoryType = "药箱未按时服用:";
                            break;
                        case 5:
                            medicineHistoryType = "药盒非服药操作";
                            break;
                        case 6:
                            medicineHistoryType = "药箱非服药操作";
                            break;
                    }
                    tel = jsonobject2.getString("tel");
                    id = jsonobject2.getString("id");
                    final MultipleItemEntity entity = MultipleItemEntity.builder()
                            .setField(MultipleFields.ITEM_TYPE, ItemType.TEXT_TEXT)
                            .setField(MultipleFields.SPAN_SIZE, 3)
                            .setField(MultipleFields.MEDICINE_NAME, medicineHistoryType+" "+medicineName)
                            .setField(MultipleFields.MEDICINEUSERTIME, medicineUseTime)
                            .setField(MultipleFields.BOXID, boxId)
                            .setField(MultipleFields.MEDICINEHISTORYTYPE,medicineHistoryType)
                            .setField(MultipleFields.TEL, tel)
                            .setField(MultipleFields.ID, id)
                            .build();
                    medicineHistoryList.add(entity);
                }
            }
        }
    }*/


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //medicinePlanExpandableListViewAdapter = new MedicinePlanExpandableListViewAdapter( dataset,parentList,null, IndexDelegate.this);
        //mExpandableListView.setAdapter(medicinePlanExpandableListViewAdapter);
        //initView();
        //medicineHistoryRecyclerViewAdapter= MultipleRecyclerAdapter.create(medicineHistoryList,this.getParentDelegate());
        //mRecyclerViewHistory.setAdapter(medicineHistoryRecyclerViewAdapter);
    }
    private void refresh(){
        if(mRefreshLayout!=null)
            mRefreshLayout.setRefreshing(true);
        Latte.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //getMedicinePlan();
               // getMedicineHistory();
                //可以进行网络请求，REFESH_LAYOUT.setRefreshing(false);可以放入网络请求的success回调
                mRefreshLayout.setRefreshing(false);
            }
        },2000);
    }


/*    public void initView() {
        myAdapter = new MyElvAdapterForIndex(context, mExpandableListView,list);
        mExpandableListView.setAdapter(myAdapter);
        mExpandableListView.setGroupIndicator(null);
    }
    private void initData(String responseJsonString) {
        list.clear();
        final JSONObject jsonObject = JSON.parseObject(responseJsonString);
        final JSONObject data = jsonObject.getJSONObject("detail");
        final JSONArray dataArray = data.getJSONArray("planlist");
        int size = dataArray.size();
        for (int i = 0; i < size; i++) {
            MedicinePlanInfo medicinePlanInfo=new MedicinePlanInfo();
            JSONObject jsondata = (JSONObject) dataArray.get(i);
            String medicineUseTime=jsondata.getString("time");
            medicinePlanInfo.setTimeString(medicineUseTime);
            JSONArray jsonArray = jsondata.getJSONArray("plans");
            int lenght = jsonArray.size();
            List<MedicinePlan> childrenList = new ArrayList<>();
            for (int j = 0; j < lenght; j++) {
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(j);
                MedicinePlan medicinePlan = new MedicinePlan();
                medicinePlan.setAtime(jsonObject1.getString("atime"));
                medicinePlan.setEndRemind(jsonObject1.getString("endRemind"));
                medicinePlan.setId(jsonObject1.getString("id"));
                medicinePlan.setMedicineUseCount(jsonObject1.getIntValue("medicineUseCount"));
                medicinePlan.setMedicineType(jsonObject1.getIntValue("medicineType"));
                medicinePlan.setStartRemind(jsonObject1.getString("startRemind"));
                medicinePlan.setMedicineName(jsonObject1.getString("medicineName"));
                medicinePlan.setBoxId(jsonObject1.getString("boxId"));
                childrenList.add(medicinePlan);
            }
            medicinePlanInfo.setDatas(childrenList);
            list.add(medicinePlanInfo);

        }

    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //StatusBarCompat.translucentStatusBar(getActivity(),false);
        int color=getResources().getColor(R.color.Apricot);
       // StatusBarCompat.setStatusBarColor(getActivity(),color);
    }

}
