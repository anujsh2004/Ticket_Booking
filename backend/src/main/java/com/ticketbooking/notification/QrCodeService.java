package com.ticketbooking.notification;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@Slf4j
public class QrCodeService {

    public byte[] generateQrCodeBytes(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate QR code for text: {}", text, e);
            throw new RuntimeException("Could not generate QR code", e);
        }
    }

    public String generateQrCodeBase64(String text, int width, int height) {
        byte[] bytes = generateQrCodeBytes(text, width, height);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
