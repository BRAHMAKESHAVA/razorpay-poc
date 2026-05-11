package org.backend.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import org.backend.exception.OtpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service class for sending SMS messages using Twilio.
 * Provides functionality to send OTP codes via SMS, with error handling for rate limits and provider failures.
 */
@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.sms.from}")
    private String twilioPhoneNumber;

    @Value("${twilio.whatsapp.sandbox}")
    private String whatsappSandbox;

    /**
     * Sends an OTP code to the specified phone number via SMS using Twilio.
     * Handles Twilio API exceptions, including daily message limits and general provider errors.
     * Note: Currently configured for SMS; WhatsApp functionality is commented out.
     *
     * @param phoneNumber the recipient's phone number (without country code, assumes +91 for India)
     * @param otp the OTP code to send
     * @throws OtpException if OTP sending fails due to rate limits or provider errors
     */
    public void sendOtp(String phoneNumber, String otp) {
        try {
            Twilio.init(accountSid, authToken);
            String phnoeNumberr = "+91" + phoneNumber;
            Message.creator(
                    new com.twilio.type.PhoneNumber(phnoeNumberr),
                    new com.twilio.type.PhoneNumber(twilioPhoneNumber),
                     "<#> Your OTP code is: " + otp + "\n 2sOU1BmwXXR"
                    //"Your OTP code is: " + otp + " 2sOU1BmwXXR"
                    ).create();
            //WhatsApp
//            Message.creator(
//                    new com.twilio.type.PhoneNumber("whatsapp:+91" + phoneNumber),
//                    new com.twilio.type.PhoneNumber("whatsapp:" + whatsappSandbox),
//                    "Your OTP code is: " + otp
//            ).create();

        } catch (ApiException ex) {
            //Twilio daily limit exceeded
            if (ex.getMessage() != null &&
                    ex.getMessage().contains("daily messages limit")) {
                throw new OtpException(
                        "OTP_LIMIT_EXCEEDED",
                        "OTP service temporarily unavailable. Please try again later.Account exceeded the 50 daily messages limit",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }
        } catch (Exception ex){
            //Any other Twilio failure
            throw new OtpException(
                    "OTP_PROVIDER_ERROR",
                    "Failed to send OTP. Please try again.",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }
}
