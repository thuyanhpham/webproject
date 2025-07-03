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

    /**
     * Xử lý khi người dùng được MoMo chuyển hướng trở lại trang web sau khi thanh toán.
     * Nhiệm vụ chính: Xác thực chữ ký và hiển thị trang kết quả cho người dùng.
     * Không cập nhật trạng thái cuối cùng của đơn hàng ở đây.
     */
    @GetMapping("/payment/momo")
    public String handleMomoReturn(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        String momoOrderId = allParams.get("orderId"); 
        String resultCode = allParams.get("resultCode");
        log.info("MoMo Redirect received for orderId: {}, resultCode: {}", momoOrderId, resultCode);
        
        // Tạo một bản sao của các tham số và thêm accessKey (vì nó không được gửi về)
        Map<String, String> fieldsToVerify = new HashMap<>(allParams);
        fieldsToVerify.put("accessKey", MomoConfig.ACCESS_KEY);

        // Tạo chuỗi ký từ map đã được bổ sung, loại bỏ signature và sắp xếp theo thứ tự chữ cái.
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
                // Phân tích chuỗi momoOrderId để lấy ra bookingId thực sự
                String bookingIdStr = momoOrderId.split("_")[1];
                Long bookingId = Long.parseLong(bookingIdStr);
                
                // Truyền cả bookingId và momoOrderId đến trang confirm
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

    /**
     * Xử lý thông báo IPN (Instant Payment Notification) từ server MoMo.
     * Đây là nguồn tin cậy duy nhất để cập nhật trạng thái thanh toán vào CSDL.
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<String> handleMomoIpn(@RequestBody Map<String, String> ipnData) {
        String orderId = ipnData.get("orderId");
        log.info("Momo IPN received for orderId: {}, resultCode: {}", orderId, ipnData.get("resultCode"));

        // Tạo một bản sao của dữ liệu IPN và thêm accessKey bị thiếu vào để xác thực chữ ký
        Map<String, String> fieldsToVerify = new HashMap<>(ipnData);
        fieldsToVerify.put("accessKey", MomoConfig.ACCESS_KEY);

        String rawDataForVerification = fieldsToVerify.entrySet().stream()
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

        try {
            int resultCode = Integer.parseInt(ipnData.get("resultCode"));
            PaymentStatus status;
            //PaymentStatus status = (resultCode == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            if (resultCode == 0) {
                status = PaymentStatus.SUCCESS;
                //log.info("Payment for order {} was SUCCESSFUL via IPN.", orderId);
            } else if (resultCode == 1006) { 
                status = PaymentStatus.CANCELLED;
                //log.warn("Payment for order {} was CANCELLED by user via IPN.", orderId);
            }else if (resultCode == 1004) { 
            status = PaymentStatus.EXPIRED;
            //log.warn("Payment for order {} has EXPIRED via IPN.", orderId);
            }
            else {
                status = PaymentStatus.FAILED;
                log.warn("Payment for order {} FAILED with code: {} via IPN.", orderId, resultCode);
            }
            
            paymentService.updatePaymentStatus(
                orderId,
                ipnData.get("transId"),
                status,
                resultCode,
                ipnData.get("message"),
                receivedSignature
            );
            //log.info("Updated payment status for orderId {} to {} via IPN.", orderId, status);         
            // Trả về 204 No Content cho MoMo để xác nhận đã nhận thành công
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Error updating payment status from IPN for orderId {}: {}", orderId, e.getMessage(), e);         
            // Nếu có lỗi, trả về lỗi server để MoMo có thể gửi lại IPN sau
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Hiển thị trang xác nhận thanh toán thành công.
     */
    @GetMapping("/momo/confirm")
    public String showConfirmationPage(Model model, 
                                       @RequestParam(required = false) String momoOrderId,
                                       @RequestParam(required = false) Long bookingId) {
        model.addAttribute("pageTitle", "Đặt vé thành công");
        model.addAttribute("orderId", momoOrderId); 
        model.addAttribute("bookingId", bookingId); 
        return "user/movie/confirmation";
    }
 
    /**
     * Hiển thị trang thanh toán thất bại.
     */
    @GetMapping("/momo/failure")
    public String showFailurePage(Model model) {
        model.addAttribute("pageTitle", "Payment Failed");
        return "user/movie/failure";
    }
}