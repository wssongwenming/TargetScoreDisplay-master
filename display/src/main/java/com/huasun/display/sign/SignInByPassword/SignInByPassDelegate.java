package com.huasun.display.sign.SignInByPassword;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.config.ServerConfig;
import com.huasun.core.app.Latte;
import com.huasun.core.delegates.LatteDelegate;
import com.huasun.core.delegates.bottom.BottomItemDelegate;
import com.huasun.core.net.RestClient;
import com.huasun.core.net.callback.ISuccess;
import com.huasun.core.util.Config;
import com.huasun.core.util.timer.ITimerListener;
import com.huasun.display.R;
import com.huasun.display.R2;
import com.huasun.display.recycler.ItemType;
import com.huasun.display.recycler.MultipleFields;
import com.huasun.display.recycler.MultipleItemEntity;
import com.huasun.display.sign.ISignListener;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * author:songwenming
 * Date:2019/9/23
 * Description:
 */
public class SignInByPassDelegate extends BottomItemDelegate {
    private static final String USER_INFO = "USER_INFO";
    private String userInfo;
    @BindView(R2.id.edit_sign_in_id)
    EditText mId=null;
    @BindView(R2.id.edit_sign_in_name)
    EditText mName=null;
    @BindView(R2.id.edit_sign_in_department)
    EditText mDepartment=null;
    @BindView(R2.id.edit_sign_in_password)
    EditText mPassword=null;
    @BindView(R2.id.img_sign_in_photo)
    AppCompatImageView mPhoto=null;
    private ISignListener mISignListener=null;
    @OnClick(R2.id.btn_sign_in)
    void onClickSignIn(){
        if(checkForm()){
            RestClient.builder()
                    //.url("http://192.168.1.3:8081/Web01_exec/UserLogin")
                    .url("http://192.168.1.3:8080/sys/trainee/login")
                    //.url(Config.loginIp)
                    .params("id",mId.getText().toString())
                    .params("password",mPassword.getText().toString())
                    .success(new ISuccess() {
                        @Override
                        public void onSuccess(String response) {
                            SignInHandler.onSignIn(response,mISignListener);
                        }
                    })
                    .build()
                    .post();
        }
    }

    private static final RequestOptions REQUEST_OPTIONS=
            new RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate();
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof ISignListener)
        {
            mISignListener=(ISignListener) activity;
        }
    }
    public void initData(String json){
        if(json!=null&&!json.isEmpty()) {
            final JSONObject data= JSON.parseObject(json).getJSONObject("data");
                final int id = data.getInteger("userId");//编号
                final String name = data.getString("name");
                final String department = data.getString("department");
                final String password = data.getString("password");//登陆者的密码，一般为空，有时不需输入时可以输入默认的密码
                final String photopath=ServerConfig.API_SERVER+data.getString("photopath");//登陆者的图片路径

                System.out.print("path======="+photopath);
                mId.setText(id+"");
                mName.setText(name);
                mDepartment.setText(department);
                mPassword.setText(password);
                Glide.with(getContext())
                        .load(photopath)
                        .apply(REQUEST_OPTIONS)
                        .into(mPhoto);

            }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            userInfo = args.getString(USER_INFO);
        }
    }

    public static SignInByPassDelegate newInstance(String userInfo){
        final Bundle args = new Bundle();
        args.putString(USER_INFO,userInfo);
        final SignInByPassDelegate delegate = new SignInByPassDelegate();
        delegate.setArguments(args);
        return delegate;
    }

    @Override
    public Object setLayout() {
        return R.layout.delegate_sign_in_by_password;
    }


    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        initData(userInfo);
    }
    private boolean checkForm(){
        final String id=mId.getText().toString();
        final String name=mName.getText().toString();
        final String department=mDepartment.getText().toString();
        final String password=mPassword.getText().toString();
        boolean isPass=true;
//        if(id.isEmpty()){
//            mId.setError("请输入编码");
//            isPass=false;
//        }else {
//            mId.setError(null);
//        }
        /*if(name.isEmpty()){
            mName.setError("请输入姓名");
            isPass=false;
        }else {
          mName.setError(null);
        }
        if(department.isEmpty()){
            mDepartment.setError("请输入部职别/所属单位");
            isPass=false;
        }else {
            mDepartment.setError(null);
        }*/
//        if(password.isEmpty()||password.length()<6){
//            mPassword.setError("请填写至少6位数密码");
//            isPass=false;
//        }else{
//            mPassword.setError(null);
//        }
        return isPass;
    }


}
