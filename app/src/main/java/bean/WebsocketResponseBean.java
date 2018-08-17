package bean;

import android.util.Log;

import com.example.acitsec.hook.Constance;

import org.xutils.common.util.MD5;

import utils.SPUtils;

/**
 * Created by ACITSEC on 2018/5/29.
 */

public class WebsocketResponseBean {
    private String orgId;
    private String totalAmount;
    private String reason;
    private String sign;
    private String txnType;
    private static final String TAG = "WebsocketResponseBean";
    private String responseTime;

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public boolean isCorrectResponse(String key) {
        String sign = MD5.md5("orgId=" + orgId + "&" +
                "totalAmount=" + totalAmount + "&" +
                "reason=" + reason + "&" +
                "txnType=" + txnType + "&" +
                "key=" + key);
        Log.e(TAG, "isCorrectResponse: "+key);
        if (this.sign.equals(sign)) {
            return true;
        }
        return false;
    }


}
