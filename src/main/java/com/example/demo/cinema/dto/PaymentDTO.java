package com.example.demo.cinema.dto; // Đảm bảo package này đúng

import com.example.demo.cinema.entity.Payment; // Import Payment entity
import com.example.demo.cinema.enums.PaymentMethod;
import com.example.demo.cinema.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {
    private Long id;
    private LocalDateTime createdAt; // Chuẩn hóa tên
    private String momoTransactionId;
    private String momoOrderId;
    private BigDecimal amount; // Chuẩn hóa kiểu dữ liệu
    private PaymentMethod paymentMethod;
    private PaymentStatus status; // Thêm trạng thái
    private Integer momoResultCode;
    private String momoMessage;

    public PaymentDTO() {
    }

    // Constructor để chuyển từ Entity sang DTO
    public PaymentDTO(Payment payment) {
        this.id = payment.getId();
        this.createdAt = payment.getCreatedAt();
        this.momoTransactionId = payment.getMomoTransactionId();
        this.momoOrderId = payment.getMomoOrderId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();
        this.momoResultCode = payment.getMomoResultCode();
        this.momoMessage = payment.getMomoMessage();
    }

    // --- Getters and Setters (Bạn hãy tự thêm cho tất cả các trường) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getMomoTransactionId() { return momoTransactionId; }
    public void setMomoTransactionId(String momoTransactionId) { this.momoTransactionId = momoTransactionId; }
    public String getMomoOrderId() { return momoOrderId; }
    public void setMomoOrderId(String momoOrderId) { this.momoOrderId = momoOrderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public Integer getMomoResultCode() { return momoResultCode; }
    public void setMomoResultCode(Integer momoResultCode) { this.momoResultCode = momoResultCode; }
    public String getMomoMessage() { return momoMessage; }
    public void setMomoMessage(String momoMessage) { this.momoMessage = momoMessage; }
}
