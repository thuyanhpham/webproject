package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.BookingService;
import com.example.demo.cinema.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections; 
import java.util.List;
import java.util.Optional; 

@Controller
@RequestMapping("/user")
public class BookingHistoryController {

    private static final Logger log = LoggerFactory.getLogger(BookingHistoryController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping("/my-bookings")
    public String showBookingHistory(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return "redirect:/login";
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
  
        List<Booking> bookings = Optional.ofNullable(bookingService.findBookingsByUser(currentUser))
                                         .orElse(Collections.emptyList());
        // ===========================================================================
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("pageTitle", "Lịch sử Đặt vé");
        
        //log.info("Fetched {} bookings for user {}", bookings.size(), username);
        
        return "user/movie/booking-history";
    }

    @GetMapping("/booking/{bookingId}")
    public String showBookingDetail(@PathVariable("bookingId") Long bookingId, Model model) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
//            return "redirect:/login";
//        }
//        
//        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

//        if (!booking.getUser().getUsername().equals(username)) {
//            log.warn("User {} attempting to access booking {} of another user.", username, bookingId);
//            return "redirect:/error?code=403";
//        }

        model.addAttribute("booking", booking);
        model.addAttribute("pageTitle", "Chi tiết vé");

        return "user/movie/booking-details";
    }
}
