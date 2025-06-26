package com.example.demo.cinema.service;

import com.example.demo.cinema.dto.momo.MomoPaymentRequest; // <<< THÊM IMPORT NÀY
import com.example.demo.cinema.dto.momo.MomoPaymentResponse;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface MomoService {

    /**
     * Sửa đổi chữ ký của phương thức để nhận một đối tượng MomoPaymentRequest duy nhất.
     * Điều này làm cho code sạch hơn và khớp với lớp triển khai MomoServiceImp.
     */
    MomoPaymentResponse createPayment(MomoPaymentRequest request); // <<< SỬA DÒNG NÀY

    /**
     * Xử lý thông báo IPN (Instant Payment Notification) từ MoMo.
     * @param ipnData Dữ liệu MoMo gửi đến.
     * @return true nếu chữ ký hợp lệ và xử lý thành công, false nếu ngược lại.
     */
    boolean processIpn(Map<String, String> ipnData);

    /**
     * Truy vấn trạng thái của một giao dịch trên hệ thống MoMo.
     * @param orderId ID đơn hàng của bạn.
     * @param requestId ID yêu cầu tương ứng lúc tạo giao dịch.
     * @return Một Map chứa thông tin trạng thái trả về từ MoMo.
     */
    Map<String, String> queryOrderStatus(String orderId, String requestId);

    /**
     * Xác thực chữ ký nhận được từ MoMo.
     * @param rawData Dữ liệu thô dùng để tạo chữ ký.
     * @param signature Chữ ký nhận được từ MoMo.
     * @param secretKey Khóa bí mật của bạn.
     * @return true nếu chữ ký hợp lệ, false nếu không.
     */
    boolean verifySignature(String rawData, String signature, String secretKey);
}
