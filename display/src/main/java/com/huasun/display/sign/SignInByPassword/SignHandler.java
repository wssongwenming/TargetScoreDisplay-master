package com.huasun.display.sign.SignInByPassword;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huasun.core.app.AccountManager;
import com.huasun.core.app.ConfigKeys;
import com.huasun.core.app.Latte;
import com.huasun.core.util.log.LatteLogger;
import com.huasun.display.database.DatabaseManager;
import com.huasun.display.database.UserProfile;
import com.huasun.display.sign.ISignListener;

/**
 * author:songwenming
 * Date:2019/9/24
 * Description:
 */
public class SignHandler {
    public static void onSignIn(String response,ISignListener signListener){
        DatabaseManager.getInstance().getDao().deleteAll();
        final JSONObject profileJson= JSON.parseObject(response).getJSONObject("detail");

        final long userId=profileJson.getLong("userId");
        final String name=profileJson.getString("name");
        final String department=profileJson.getString("department");
        final String shooting_gun=profileJson.getString("shooting_gun");
        final int bullet_count=profileJson.getInteger("bullet_count");
        final String target_number=profileJson.getString("target_number");//靶位序号
        final String group_number=profileJson.getString("group_number");//组序号
        final UserProfile localUser=new UserProfile(userId, name, department,
                shooting_gun,bullet_count,target_number, group_number);
        //在内存中保留登陆数据
        Latte.getConfigurations().remove(ConfigKeys.LOCAL_USER);
        Latte.getConfigurations().put(ConfigKeys.LOCAL_USER,localUser);
        DatabaseManager.getInstance().getDao().insert(localUser);
        //  已经登陆成功了
        AccountManager.setSignState(true);
        signListener.onSignInSuccess(3,localUser);
    }
}
