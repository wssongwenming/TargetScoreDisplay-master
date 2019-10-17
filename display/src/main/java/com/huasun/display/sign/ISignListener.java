package com.huasun.display.sign;

import com.huasun.display.database.UserProfile;

public interface ISignListener {
    //分别为登录和注册成功的回调
    void onSignInSuccess(int index, UserProfile userProfile);
    void onSignUpSuccess();
    void onSignInError(String msg);
    void onSignUpError(String msg);
    void onSignInFailure(String msg);
    void onSignUpFailure(String msg);
}
