package com.example.demo.cinema.dto.momo;

import java.io.Serializable;

public class MomoPaymentRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String partnerCode;
    private String partnerName;
    private String storeId;
    private String requestId;
    private long amount; // Đã đổi từ String sang long
    private String orderId;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String lang;
    private String extraData;
    private String requestType;
    private String signature;
    private String userName;

    public MomoPaymentRequest() {}

    // Getters and Setters
    public String getPartnerCode() { return partnerCode; }
    public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public long getAmount() { return amount; } // Đã đổi kiểu trả về và tham số
    public void setAmount(long amount) { this.amount = amount; } // Đã đổi kiểu tham số
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
    public String getIpnUrl() { return ipnUrl; }
    public void setIpnUrl(String ipnUrl) { this.ipnUrl = ipnUrl; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    // Đã xóa phương thức setAccessKey không cần thiết
}