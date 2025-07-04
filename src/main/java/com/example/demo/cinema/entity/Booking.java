package com.example.demo.cinema.entity;

import com.example.demo.cinema.enums.BookingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "booking_time", nullable = false, updatable = false)
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private BookingStatus status;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true, orphanRemoval = true)
    private Payment payment;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    public Booking() {
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.PENDING;
    }

    public Booking(Showtime showtime, User user, BigDecimal totalPrice, BookingStatus status) {
        this.showtime = showtime;
        this.user = user;
        this.totalPrice = totalPrice;
        this.status = status;
        this.bookingTime = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.bookingTime == null) {
            this.bookingTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Showtime getShowtime() { return showtime; }
    public void setShowtime(Showtime showtime) { this.showtime = showtime; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
    
    // --- Helper methods ---
    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        ticket.setBooking(this);
    }
    public void removeTicket(Ticket ticket) {
        this.tickets.remove(ticket);
        ticket.setBooking(null);
    }

    @Transient
    public String getSeatLabels() {
        if (tickets == null || tickets.isEmpty()) {
            return "(Chưa có vé)";
        }
        return tickets.stream()
                .map(ticket -> {
                    if (ticket.getSeat() != null && ticket.getSeat().getSeatLabel() != null) {
                        return ticket.getSeat().getSeatLabel();
                    } else {
                        return ticket.getSeatLabel(); // fallback từ ticket
                    }
                })
                .collect(Collectors.joining(", "));
    }
    
    @Transient
    public boolean isStillValid() {
        if (this.showtime == null || this.showtime.getShowDate() == null || this.showtime.getStartTime() == null) {
            
            return false;
        }
        
        // Ghép ngày và giờ của suất chiếu thành một đối tượng thời gian đầy đủ
        LocalDateTime showDateTime = this.showtime.getShowDate().atTime(this.showtime.getStartTime());
        // Lấy thời gian hiện tại của hệ thống
        LocalDateTime now = LocalDateTime.now();

        // So sánh
        boolean isValid = now.isBefore(showDateTime);

        

        return isValid;
    }
}
