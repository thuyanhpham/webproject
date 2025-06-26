package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.*;
import com.example.demo.cinema.enums.BookingStatus;
import com.example.demo.cinema.enums.PaymentMethod;
import com.example.demo.cinema.enums.PaymentStatus;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.*;
import com.example.demo.cinema.dto.momo.MomoPaymentRequest;
import com.example.demo.cinema.dto.momo.MomoPaymentResponse;


import org.springframework.transaction.annotation.Transactional; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    @Autowired
    private ShowtimeService showtimeService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private MomoService momoService;

   
    @Autowired
    public CheckoutController(ShowtimeService showtimeService, MovieService movieService, SeatService seatService, UserService userService, BookingService bookingService, PaymentService paymentService, MomoService momoService) {
        this.showtimeService = showtimeService;
        this.movieService = movieService;
        this.seatService = seatService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.momoService = momoService;
    }

    @GetMapping
    public String showCheckoutPage(@RequestParam("showtimeId") Long showtimeId, @RequestParam("seatIds") List<Long> seatIds, Model model) {
        Showtime showtime = showtimeService.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with ID: " + showtimeId));
        Movie movie = showtime.getMovie();
        
        List<Seat> selectedSeats = seatService.findAllById(seatIds);
        if (selectedSeats.isEmpty()) {
            throw new ResourceNotFoundException("No valid seats found for IDs: " + seatIds);
        }

        BigDecimal totalTicketPriceBd = selectedSeats.stream()
                .map(seat -> seat.getSeatType().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        User currentUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            currentUser = userService.findByUsername(username).orElse(null);
        }
        
        model.addAttribute("movie", movie);
        model.addAttribute("showtime", showtime);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("totalTicketPrice", totalTicketPriceBd.doubleValue());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "Xác nhận & Thanh toán");

        return "user/movie/payment";
    }

    @PostMapping("/initiate-momo-payment")
    @Transactional // Annotation này giờ là của Spring, sẽ hoạt động đúng
    public String initiateMomoPayment(
            @RequestParam("showtimeId") Long showtimeId,
            @RequestParam("seatIds") List<Long> seatIds,
            @RequestParam("totalAmount") double totalAmount,
            RedirectAttributes redirectAttributes) {

        BigDecimal totalAmountBd = BigDecimal.valueOf(totalAmount);

        try {
            Showtime showtime = showtimeService.findById(showtimeId).orElseThrow(() -> new ResourceNotFoundException("Showtime not found."));
            Movie movie = showtime.getMovie();

            User user = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                String username = ((UserDetails) authentication.getPrincipal()).getUsername();
                user = userService.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found."));
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để thực hiện thanh toán.");
                return "redirect:/login";
            }

            Booking booking = new Booking(showtime, user, totalAmountBd, BookingStatus.PENDING);
            booking = bookingService.save(booking);
            
            List<Seat> seatsToBook = seatService.findAllById(seatIds);
            for (Seat seat : seatsToBook) {
                Ticket ticket = new Ticket();
                ticket.setBooking(booking);
                ticket.setSeat(seat);
                ticket.setPrice(seat.getSeatType().getPrice());
                ticket.setSeatLabel(seat.getSeatLabel());
                booking.addTicket(ticket);
            }
            booking = bookingService.saveAndFlush(booking);

            String momoOrderId = "BOOKING_" + booking.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);
            String requestId = UUID.randomUUID().toString();
            Payment payment = paymentService.createPendingPayment(totalAmountBd, momoOrderId, requestId, PaymentMethod.MOMO, booking);
            booking.setPayment(payment);
            bookingService.save(booking);

            String orderInfo = "Thanh toán vé phim: " + movie.getTitle();
            
            MomoPaymentRequest momoRequest = new MomoPaymentRequest();
            momoRequest.setAmount(totalAmountBd.longValue());
            momoRequest.setOrderId(momoOrderId);
            momoRequest.setOrderInfo(orderInfo);
            momoRequest.setRequestId(requestId);
            momoRequest.setExtraData("");
            momoRequest.setRequestType("payWithMethod");
            momoRequest.setUserName(user.getUsername());
            
            MomoPaymentResponse momoResponse = momoService.createPayment(momoRequest);

            if (momoResponse != null && momoResponse.getResultCode() == 0 && momoResponse.getPayUrl() != null && !momoResponse.getPayUrl().isEmpty()) {
                log.info("Momo payment initiated successfully. Redirecting to PayUrl: {}", momoResponse.getPayUrl());
                return "redirect:" + momoResponse.getPayUrl();
            } else { 
                String errorMessage = momoResponse != null ? momoResponse.getMessage() : "Unknown error from Momo.";
                int resultCode = momoResponse != null ? momoResponse.getResultCode() : -1;
                paymentService.updatePaymentStatus(momoOrderId, null, PaymentStatus.FAILED, resultCode, errorMessage, null);
                log.error("Lỗi khởi tạo thanh toán Momo: {}", errorMessage);
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể khởi tạo thanh toán Momo. Lỗi: " + errorMessage);
                
                throw new IllegalStateException("Momo payment initiation failed: " + errorMessage);
            }

        } catch (Exception e) {
            log.error("Exception during Momo payment initiation, transaction will be rolled back: {}", e.getMessage(), e);
            
            throw new RuntimeException("Lỗi hệ thống khi xử lý thanh toán, giao dịch đã được hủy.", e);
        }
    }
}