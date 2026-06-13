package com.restaurant.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.RestaurantTable;
import com.restaurant.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRService {

    private final RestaurantTableRepository tableRepository;

    @Value("${app.qr.frontend-base-url}")
    private String frontendBaseUrl;

    /**
     * Generates a PNG QR code for a given table number.
     * If the table doesn't have a qrToken yet, one is created and persisted.
     * The encoded URL points the customer's browser at the menu page.
     */
    @Transactional
    public byte[] generateQRCode(Integer tableNumber) {
        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RestaurantTable", "tableNumber", tableNumber));

        // Assign a stable UUID token the first time
        if (table.getQrToken() == null || table.getQrToken().isBlank()) {
            table.setQrToken(UUID.randomUUID().toString());
            tableRepository.save(table);
        }

        String url = frontendBaseUrl + "/menu?table=" + table.getQrToken();

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 2
            );
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code for table " + tableNumber, e);
        }
    }
}
