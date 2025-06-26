package com.example.demo.cinema.dto.momo;

import java.io.Serializable;

public class MomoPaymentResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String partnerCode;
    private String requestId;
    private String orderId;
    private long amount;       // Đã đổi từ String sang long
    private String orderInfo;
    private String message;
    private int resultCode;    // 0: thành công
    private String payUrl;     // URL để chuyển hướng người dùng
    private String signature;
    private String transId;    // ID giao dịch của Momo
    private String deeplink;   // Deep link để mở app Momo (có thể không có)
    private String qrCodeUrl;  // URL của mã QR (có thể không có)
    private String errorCode;  // Mã lỗi cụ thể (nếu có)
    private String localMessage; // Tin nhắn lỗi địa phương (nếu có)
    private String payType;    // Kiểu thanh toán (ví dụ: QR, Wallet)
    private String responseTime; // Thời gian phản hồi
    private String extraData; // Đảm bảo trường này được khai báo đúng
    private String paymentOption;
    
    public MomoPaymentResponse() {}
    
    public MomoPaymentResponse(int resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
        // Có thể gán các giá trị mặc định khác nếu cần, ví dụ:
        this.orderId = null;
        this.requestId = null;
        this.amount = 0; // Hoặc BigDecimal.ZERO
        this.payUrl = null;
        this.signature = null;
    }

    // Getters and Setters
    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }
    public String getPartnerCode() { return partnerCode; }
    public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public long getAmount() { return amount; } // Kiểu trả về là long
    public void setAmount(long amount) { this.amount = amount; } // Kiểu tham số là long

    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getResultCode() { return resultCode; }
    public void setResultCode(int resultCode) { this.resultCode = resultCode; }

    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getTransId() { return transId; }
    public void setTransId(String transId) { this.transId = transId; }

    public String getDeeplink() { return deeplink; }
    public void setDeeplink(String deeplink) { this.deeplink = deeplink; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getLocalMessage() { return localMessage; }
    public void setLocalMessage(String localMessage) { this.localMessage = localMessage; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public String getResponseTime() { return responseTime; }
    public void setResponseTime(String responseTime) { this.responseTime = responseTime; }

    public String getExtraData() { return extraData; } // Đảm bảo getter này đúng
    public void setExtraData(String extraData) { this.extraData = extraData; } // Đảm bảo setter này đúng
}