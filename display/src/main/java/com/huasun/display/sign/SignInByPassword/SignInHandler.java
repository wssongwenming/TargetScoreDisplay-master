package com.huasun.display.sign.SignInByPassword;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bcsb.rabbitmq.entity.Command;
import com.bcsb.rabbitmq.entity.UserDetail;
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
public class SignInHandler {
    public static void onSignIn(String response,ISignListener signListener){
        //DatabaseManager.getInstance().getDao().deleteAll();
        final JSONObject profileJson= JSON.parseObject(response).getJSONObject("data");

        final int userId=profileJson.getInteger("userId");
        final String name=profileJson.getString("name");
        final String department=profileJson.getString("department");
        final String shooting_gun=profileJson.getString("shooting_gun");
        final int bullet_count=profileJson.getInteger("bullet_count");
        final String target_number=profileJson.getString("target_number");//靶位序号
        final String group_number=profileJson.getString("group_number");//组序号
        //  已经登陆成功了
        AccountManager.setSignState(true);
        //建立Userdetail就是为了便于建立command
        UserDetail userDetail =new UserDetail();
        Command command=new Command();
        command.setCode(0);
        command.setDescription("密码登陆");
        command.setIndex(1);//
        userDetail.setUserId(userId);
        userDetail.setBullet_count(bullet_count);
        userDetail.setDepartment(department);
        userDetail.setGroup_number(group_number);
        userDetail.setName(name);
        userDetail.setPhoto_path("");
        userDetail.setPassword("");
        userDetail.setShooting_gun(shooting_gun);
        userDetail.setTarget_number(target_number);
        command.setData(userDetail);
        String commandjson=JSON.toJSONString(command);
        signListener.onSignInSuccess(3,commandjson);
    }
}
