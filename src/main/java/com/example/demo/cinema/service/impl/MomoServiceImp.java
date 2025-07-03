package com.example.demo.cinema.service.impl;

import com.example.demo.cinema.config.MomoConfig;
import com.example.demo.cinema.dto.momo.MomoPaymentRequest;
import com.example.demo.cinema.dto.momo.MomoPaymentResponse;
import com.example.demo.cinema.enums.PaymentStatus;
import com.example.demo.cinema.service.MomoService;
import com.example.demo.cinema.service.PaymentService;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class MomoServiceImp implements MomoService {

    private static final Logger log = LoggerFactory.getLogger(MomoServiceImp.class);

    @Autowired
    private PaymentService paymentService;

    private String signHmacSHA256(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return toHexString(rawHmac);
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        try (Formatter formatter = new Formatter(sb)) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
        }
        return sb.toString();
    }
    
    @Override
    public MomoPaymentResponse createPayment(MomoPaymentRequest request) {
        String partnerCode = MomoConfig.PARTNER_CODE;
        String accessKey = MomoConfig.ACCESS_KEY;
        String secretKey = MomoConfig.SECRET_KEY;
        String createOrderUrl = MomoConfig.CREATE_ORDER_URL;
        
        String redirectUrl = MomoConfig.RETURN_URL;
        String ipnUrl = MomoConfig.NOTIFY_URL;

        long longAmount = request.getAmount();
        String orderId = request.getOrderId();
        String orderInfo = request.getOrderInfo();
        String extraData = request.getExtraData();
        String requestId = request.getRequestId();
        String requestType = "captureWallet"; // Sử dụng giá trị chuẩn

        String rawHash = "accessKey=" + accessKey +
                         "&amount=" + longAmount +
                         "&extraData=" + extraData +
                         "&ipnUrl=" + ipnUrl +
                         "&orderId=" + orderId +
                         "&orderInfo=" + orderInfo +
                         "&partnerCode=" + partnerCode +
                         "&redirectUrl=" + redirectUrl +
                         "&requestId=" + requestId +
                         "&requestType=" + requestType;

        String signature;
        try {
            log.info("[MoMo Create Payment] Final Raw hash string: " + rawHash);
            signature = signHmacSHA256(rawHash, secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error creating signature for payment creation: {}", e.getMessage(), e);
            return new MomoPaymentResponse(-1, "Lỗi tạo chữ ký.");
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("partnerCode", partnerCode);
        jsonPayload.put("requestId", requestId);
        jsonPayload.put("amount", String.valueOf(longAmount));
        jsonPayload.put("orderId", orderId);
        jsonPayload.put("orderInfo", orderInfo);
        jsonPayload.put("redirectUrl", redirectUrl);
        jsonPayload.put("ipnUrl", ipnUrl);
        jsonPayload.put("extraData", extraData);
        jsonPayload.put("requestType", requestType);
        jsonPayload.put("signature", signature);
        jsonPayload.put("lang", "vi");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(createOrderUrl);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(jsonPayload.toString(), StandardCharsets.UTF_8));
            log.info("[MoMo Create Payment] Request body: " + jsonPayload.toString());

            try (CloseableHttpResponse response = client.execute(post);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                
                StringBuilder responseStringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseStringBuilder.append(line);
                }
                
                JSONObject apiResponse = new JSONObject(responseStringBuilder.toString());
                log.info("MoMo API Response (Create Payment): {}", apiResponse.toString());

                MomoPaymentResponse momoResponse = new MomoPaymentResponse();
                momoResponse.setResultCode(apiResponse.optInt("resultCode"));

                String message = apiResponse.optString("message");
                if (message != null && message.length() > 250) {
                    log.warn("MoMo message is too long, truncating it. Original: {}", message);
                    message = message.substring(0, 250) + "...";
                }
                momoResponse.setMessage(message);

                if (momoResponse.getResultCode() == 0) {
                    momoResponse.setPayUrl(apiResponse.optString("payUrl"));
                } else {
                     log.error("MoMo payment initiation failed. Code: {}, Message: {}", momoResponse.getResultCode(), momoResponse.getMessage());
                }
                
                momoResponse.setPartnerCode(apiResponse.optString("partnerCode"));
                momoResponse.setRequestId(apiResponse.optString("requestId"));
                momoResponse.setOrderId(apiResponse.optString("orderId"));
                momoResponse.setAmount(apiResponse.optLong("amount"));
                momoResponse.setTransId(apiResponse.optString("transId"));
                
                return momoResponse;
            }
        } catch (Exception e) {
            log.error("Exception during MoMo payment creation: {}", e.getMessage(), e);
            return new MomoPaymentResponse(-1, "Lỗi kết nối hoặc xử lý response từ MoMo: " + e.getMessage());
        }
    }
    
    @Override
    public boolean processIpn(Map<String, String> ipnData) {
        String secretKey = MomoConfig.SECRET_KEY;
        String receivedSignature = ipnData.get("signature");

        String rawHash = "accessKey=" + ipnData.get("accessKey") +
                         "&amount=" + ipnData.get("amount") +
                         "&extraData=" + ipnData.get("extraData") +
                         "&message=" + ipnData.get("message") +
                         "&orderId=" + ipnData.get("orderId") +
                         "&orderInfo=" + ipnData.get("orderInfo") +
                         "&orderType=" + ipnData.get("orderType") +
                         "&partnerCode=" + ipnData.get("partnerCode") +
                         "&payType=" + ipnData.get("payType") +
                         "&requestId=" + ipnData.get("requestId") +
                         "&responseTime=" + ipnData.get("responseTime") +
                         "&resultCode=" + ipnData.get("resultCode") +
                         "&transId=" + ipnData.get("transId");
        
        if (!verifySignature(rawHash, receivedSignature, secretKey)) {
            log.error("IPN Signature verification FAILED for Order ID: {}", ipnData.get("orderId"));
            return false;
        }

        try {
            String orderId = ipnData.get("orderId");
            String transId = ipnData.get("transId");
            String message = ipnData.get("message");
            Integer resultCode = Integer.parseInt(ipnData.get("resultCode"));

            if (resultCode == 0) {
                log.info("Payment for order {} was SUCCESSFUL via IPN. TransId: {}", orderId, transId);
                paymentService.updatePaymentStatus(orderId, transId, PaymentStatus.SUCCESS, resultCode, message, receivedSignature);
            } else {
                log.warn("Payment for order {} FAILED with code: {} - {}. TransId: {}", orderId, resultCode, message, transId);
                paymentService.updatePaymentStatus(orderId, transId, PaymentStatus.FAILED, resultCode, message, receivedSignature);
            }
            return true; 
        } catch (NumberFormatException e) {
            log.error("Error parsing data from IPN: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, String> queryOrderStatus(String orderId, String requestId) {
        String partnerCode = MomoConfig.PARTNER_CODE;
        String accessKey = MomoConfig.ACCESS_KEY;
        String secretKey = MomoConfig.SECRET_KEY;
        String queryStatusUrl = MomoConfig.QUERY_STATUS_URL; 

        String rawHash = "partnerCode=" + partnerCode +
                         "&accessKey=" + accessKey +
                         "&requestId=" + requestId +
                         "&orderId=" + orderId +
                         "&lang=vi";
        
        String signature;
        try {
            log.info("[MoMo Query Status] Raw hash string: " + rawHash);
            signature = signHmacSHA256(rawHash, secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error creating signature for status query: {}", e.getMessage(), e);
            return null;
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("partnerCode", partnerCode);
        jsonPayload.put("requestId", requestId);
        jsonPayload.put("orderId", orderId);
        jsonPayload.put("signature", signature);
        jsonPayload.put("lang", "vi");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(queryStatusUrl);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(jsonPayload.toString(), StandardCharsets.UTF_8));
            log.info("[MoMo Query Status] Request body: " + jsonPayload.toString());

            try (CloseableHttpResponse response = client.execute(post);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                
                StringBuilder responseStringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseStringBuilder.append(line);
                }
                
                JSONObject apiResponse = new JSONObject(responseStringBuilder.toString());
                log.info("MoMo API Response (Query Status): {}", apiResponse.toString());
                
                Map<String, String> responseMap = new HashMap<>();
                apiResponse.toMap().forEach((k, v) -> responseMap.put(k, String.valueOf(v)));
                return responseMap;
            }
        } catch (Exception e) {
            log.error("Exception during MoMo status query: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean verifySignature(String rawData, String signature, String secretKey) {
        try {
            String expectedSignature = signHmacSHA256(rawData, secretKey);
            boolean isValid = expectedSignature.equals(signature);
            if (!isValid) {
                log.warn("Signature mismatch! \n" +
                         "  - Raw Data:   '{}'\n" +
                         "  - Expected:   '{}'\n" +
                         "  - Received:   '{}'", rawData, expectedSignature, signature);
            }
            return isValid;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error during signature verification: {}", e.getMessage(), e);
            return false;
        }
    }
}