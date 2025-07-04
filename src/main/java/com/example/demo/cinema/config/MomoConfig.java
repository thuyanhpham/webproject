package com.example.demo.cinema.config; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MomoConfig {
    public static final String PARTNER_CODE = "MOMOYTHI20210518";
    public static final String ACCESS_KEY = "pgmjAretwF7LguLT";
    public static final String SECRET_KEY = "MvQRof5M7xqZ7r2WgWJNEo616ElPsktv";
    public static final String RETURN_URL = "http://localhost:8080/payment/momo"; 
    public static final String NOTIFY_URL = "https://05f2-123-25-25-248.ngrok-free.app/momo/ipn";             
    public static final String CREATE_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    public static final String QUERY_STATUS_URL = "https://test-payment.momo.vn/v2/gateway/api/query";
   }
 
