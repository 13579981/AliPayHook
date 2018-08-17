package bean;

import com.example.acitsec.hook.Constance;

import org.xutils.common.util.MD5;

import utils.SPUtils;


/**
 * Created by ACITSEC on 2018/5/29.
 * 正常返回
 */

public class WebsocketRequestBean {
    private String orgId;
    private String totalAmount;
    private String reason;
    private String retCode;
    private String retMsg;
    private String txnType;
    private String payUrl;
    private String sign;

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
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

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public void setSign(String key) {
        this.sign = MD5.md5("orgId=" + orgId + "&" +
                "totalAmount=" + totalAmount + "&" +
                "reason=" + reason + "&" +
                "retCode="+retCode+"&"+
                "retMsg="+retMsg+"&"+
                "txnType="+txnType+"&"+
                "payUrl=" + payUrl + "&" +
                "key=" + key);
    }

//    public String getSign() {
//        sign = MD5.md5("orgId=" + orgId + "&" +
//                "totalAmount=" + totalAmount + "&" +
//                "reason=" + reason + "&" +
//                "retCode="+retCode+"&"+
//                "retMsg="+retMsg+"&"+
//                "txnType="+txnType+"&"+
//                "payUrl=" + payUrl + "&" +
//                "key=" + SPUtils.getInstance().getString(Constance.KEY));
//        return sign;
//    }

}
