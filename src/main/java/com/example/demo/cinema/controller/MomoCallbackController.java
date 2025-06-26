package com.example.demo.cinema.controller;

import com.example.demo.cinema.config.MomoConfig;
import com.example.demo.cinema.enums.PaymentStatus;
import com.example.demo.cinema.service.MomoService;
import com.example.demo.cinema.service.PaymentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class MomoCallbackController {

    private static final Logger log = LoggerFactory.getLogger(MomoCallbackController.class);

    @Autowired
    private MomoService momoService;

    @Autowired
    private PaymentService paymentService;

    //  (khi người dùng thanh toán xong)
    @GetMapping("/payment/momo")
    public String handleMomoReturn(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        String momoOrderId = allParams.get("orderId"); 
        String resultCode = allParams.get("resultCode");
        log.info("MoMo Redirect received for orderId: {}, resultCode: {}", momoOrderId, resultCode);
        
        // Logic xác thực chữ ký cho redirect URL
        Map<String, String> fieldsToVerify = new HashMap<>(allParams);
        fieldsToVerify.put("accessKey", MomoConfig.ACCESS_KEY);

        String rawDataForVerification = fieldsToVerify.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("signature"))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        
        String receivedSignature = allParams.get("signature");
        boolean isSignatureValid = momoService.verifySignature(rawDataForVerification, receivedSignature, MomoConfig.SECRET_KEY);

        if (!isSignatureValid) {
            log.error("Momo Redirect: Invalid signature for orderId: {}", momoOrderId);
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán không hợp lệ (lỗi xác thực chữ ký).");
            return "redirect:/momo/failure";
        }
        
        if ("0".equals(resultCode)) {
            try {
                String bookingIdStr = momoOrderId.split("_")[1];
                Long bookingId = Long.parseLong(bookingIdStr);
                
                redirectAttributes.addAttribute("bookingId", bookingId);
                redirectAttributes.addAttribute("momoOrderId", momoOrderId);
            } catch (Exception e) {
                log.error("Could not parse bookingId from momoOrderId: {}", momoOrderId, e);
                return "redirect:/user/my-bookings";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Giao dịch đã được ghi nhận và đang chờ xử lý.");
            return "redirect:/momo/confirm";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại hoặc đã hủy. " + allParams.get("message"));
            return "redirect:/momo/failure";
        }
    }


    @PostMapping("/momo/ipn")
    public ResponseEntity<String> handleMomoIpn(@RequestBody Map<String, String> ipnData) {
        String orderId = ipnData.get("orderId");
        log.info("Momo IPN received for orderId: {}, resultCode: {}", orderId, ipnData.get("resultCode"));

        // Sử dụng phương pháp xác thực chữ ký an toàn
        String rawDataForVerification = ipnData.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("signature"))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        String receivedSignature = ipnData.get("signature");

        boolean isSignatureValid = momoService.verifySignature(rawDataForVerification, receivedSignature, MomoConfig.SECRET_KEY);

        if (!isSignatureValid) {
            log.error("Momo IPN: Invalid signature for orderId: {}", orderId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Chỉ cập nhật CSDL khi nhận được IPN hợp lệ
        try {
            int resultCode = Integer.parseInt(ipnData.get("resultCode"));
            PaymentStatus status = (resultCode == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            
            // Gọi service để cập nhật trạng thái thanh toán và đơn hàng
            paymentService.updatePaymentStatus(
                orderId,
                ipnData.get("transId"),
                status,
                resultCode,
                ipnData.get("message"),
                receivedSignature
            );
            log.info("Updated payment status for orderId {} to {} via IPN.", orderId, status);         
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error updating payment status from IPN for orderId {}: {}", orderId, e.getMessage(), e);         
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    @GetMapping("/momo/confirm")
    public String showConfirmationPage(Model model, 
                                       @RequestParam(required = false) String momoOrderId,
                                       @RequestParam(required = false) Long bookingId) {
        model.addAttribute("pageTitle", "Đặt vé thành công");
        model.addAttribute("orderId", momoOrderId); 
        model.addAttribute("bookingId", bookingId); 
        return "user/movie/confirmation";
    }
 
    @GetMapping("/momo/failure")
    public String showFailurePage(Model model) {
        model.addAttribute("pageTitle", "Payment Failed");
        return "user/movie/failure";
    }
}