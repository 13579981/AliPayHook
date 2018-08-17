package bean;

import com.example.acitsec.hook.Constance;

import org.xutils.common.util.MD5;

import utils.SPUtils;


/**
 * Created by ACITSEC on 2018/5/31.
 * 错误返回请求
 */

public class WebsocketErrorRequestBean {
    private String orgId;
    private String sign;
    private String retCode;
    private String retMsg;
    private String txnType;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String setSign() {
        sign = MD5.md5("orgId=" + orgId + "&" +
                "retCode="+retCode+"&"+
                "retMsg="+retMsg+"&"+
                "txnType="+txnType+"&"+
                "key=" + SPUtils.getInstance().getString(Constance.KEY));
        return sign;
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
}
