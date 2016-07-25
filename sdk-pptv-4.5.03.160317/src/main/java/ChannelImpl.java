import android.app.Activity;
import android.content.Context;

import com.ksc.client.core.api.entity.OrderResponse;
import com.ksc.client.core.base.entity.AppInfo;
import com.ksc.client.core.base.entity.PayInfo;
import com.ksc.client.core.base.entity.RoleInfo;
import com.ksc.client.core.config.KSCStatusCode;
import com.ksc.client.core.inner.ChannelBase;
import com.pptv.vassdk.agent.PptvVasAgent;
import com.pptv.vassdk.agent.listener.ExitDialogListener;
import com.pptv.vassdk.agent.listener.LoginListener;
import com.pptv.vassdk.agent.listener.PayListener;
import com.pptv.vassdk.agent.model.LoginResult;
import com.pptv.vassdk.agent.model.PayResult;

import org.json.JSONObject;

/**
 * Created by Alamusi on 2016/7/25.
 */
public class ChannelImpl extends ChannelBase {

    private Context mContext;

    @Override
    public void init(Activity activity, AppInfo appInfo, JSONObject channelInfo) {
        mContext = activity;
        PptvVasAgent.init(activity, channelInfo.optString("gid"), "", "", false);
        PptvVasAgent.setDebugMode(false);
        mUserCallBack.onInitSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
    }

    @Override
    public void login(final Activity activity) {
        PptvVasAgent.startLoginActivity(activity, new LoginListener() {
            @Override
            public void onLoginCancel() {
                mUserCallBack.onLoginCancel(KSCStatusCode.LOGIN_CANCEL, KSCStatusCode.getErrorMsg(KSCStatusCode.LOGIN_CANCEL));
            }

            @Override
            public void onLoginSuccess(LoginResult loginResult) {
                String token = loginResult.getSessionId();
                String uid = loginResult.getUserId();
                String userName = loginResult.getBindUsrName();
                setAuthInfo(uid + "___" + token + "___" + userName);
                PptvVasAgent.showFloatingView(activity);
                mUserCallBack.onLoginSuccess(KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }
        });
    }

    @Override
    public void switchAccount(Activity activity) {
        logout(activity);
        login(activity);
    }

    @Override
    public void logout(Activity activity) {
        super.logout(activity);
        PptvVasAgent.releseFloatViewWindow();
    }

    @Override
    public void pay(Activity activity, final PayInfo payInfo, OrderResponse response) {
        PptvVasAgent.startPayActivity(activity, payInfo.getZoneId(), payInfo.getRoleId(), response.getKscOrder(), 1, response.getAmount(), response.getProductName(), new PayListener() {
            @Override
            public void onPayFinish() {

            }

            @Override
            public void onPaySuccess(PayResult payResult) {
                mUserCallBack.onPaySuccess(payInfo, KSCStatusCode.SUCCESS, KSCStatusCode.getErrorMsg(KSCStatusCode.SUCCESS));
            }

            @Override
            public void onPayFail(PayResult payResult) {
                mUserCallBack.onPayFail(payInfo, KSCStatusCode.PAY_FAILED, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_FAILED));
            }

            @Override
            public void onPayWait(PayResult payResult) {
                mUserCallBack.onPayOthers(payInfo, KSCStatusCode.PAY_PROGRESS, KSCStatusCode.getErrorMsg(KSCStatusCode.PAY_PROGRESS));
            }
        });
    }

    @Override
    public void exit(Activity activity) {
        PptvVasAgent.onExit(activity, new ExitDialogListener() {
            @Override
            public void onExit() {
                mUserCallBack.doExit();
            }

            @Override
            public void onContinue() {

            }
        });
    }

    @Override
    public void onDestroy(Activity activity) {
        super.onDestroy(activity);
        PptvVasAgent.releseFloatViewWindow();
    }

    @Override
    public void onCreateRole(RoleInfo info) {
        super.onCreateRole(info);
        PptvVasAgent.statisticCreateRole(mContext);
    }

    @Override
    public void onEnterGame(RoleInfo roleInfo) {
        super.onEnterGame(roleInfo);
        PptvVasAgent.statisticEnterGame(mContext);
    }
}
