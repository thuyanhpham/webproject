package com.example.demo.cinema.entity; // Đảm bảo package này đúng

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.cinema.enums.PaymentMethod;
import com.example.demo.cinema.enums.PaymentStatus;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo bản ghi Payment
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật bản ghi Payment

    // Mã giao dịch thực từ Momo (transId), có thể null ban đầu nếu chưa hoàn tất
    @Column(name = "momo_transaction_id", unique = true) 
    private String momoTransactionId;

    // Mã đơn hàng bạn gửi lên Momo (orderId mà bạn tạo và gửi cho Momo)
    @Column(name = "momo_order_id", nullable = false, unique = true)
    private String momoOrderId; 

    // Mã request gửi lên Momo (requestId)
    @Column(name = "momo_request_id", nullable = false, unique = true)
    private String momoRequestId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2) // <-- BigDecimal cho tiền
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false) // <-- Trạng thái thanh toán
    private PaymentStatus status;

    @Column(name = "momo_result_code") // Mã kết quả từ Momo (0: thành công, khác 0: lỗi)
    private Integer momoResultCode;

    @Column(name = "momo_message", length = 255) // Tin nhắn lỗi/thông báo từ Momo
    private String momoMessage;

    @Column(name = "momo_signature", length = 500) // Chữ ký từ Momo IPN/Callback
    private String momoSignature;

    // Liên kết một-một với Booking
    @OneToOne(fetch = FetchType.LAZY) // Lazy loading để không tải Booking không cần thiết
    @JoinColumn(name = "booking_id", unique = true, nullable = false) // Foreign key to Booking, unique for 1-1
    private Booking booking; // Liên kết với Booking entity

    // --- Constructors ---
    public Payment() {
    }

    public Payment(String momoOrderId, String momoRequestId, BigDecimal amount, PaymentMethod paymentMethod, Booking booking) {
        this.momoOrderId = momoOrderId;
        this.momoRequestId = momoRequestId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.booking = booking;
        this.status = PaymentStatus.PENDING; // Mặc định là PENDING khi khởi tạo
    }

    // --- Getters and Setters (Bạn hãy tự thêm cho tất cả các trường) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getMomoTransactionId() { return momoTransactionId; }
    public void setMomoTransactionId(String momoTransactionId) { this.momoTransactionId = momoTransactionId; }
    public String getMomoOrderId() { return momoOrderId; }
    public void setMomoOrderId(String momoOrderId) { this.momoOrderId = momoOrderId; }
    public String getMomoRequestId() { return momoRequestId; }
    public void setMomoRequestId(String momoRequestId) { this.momoRequestId = momoRequestId; }
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
    public String getMomoSignature() { return momoSignature; }
    public void setMomoSignature(String momoSignature) { this.momoSignature = momoSignature; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
