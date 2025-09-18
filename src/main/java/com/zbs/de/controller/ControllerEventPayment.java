package com.zbs.de.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.dto.DtoPayment;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.service.ServiceEventPayment;

@RestController
@RequestMapping("/api/event-payments")
public class ControllerEventPayment{

    @Autowired
    private ServiceEventPayment serviceEventPayment;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DtoResult> savePayment(@RequestBody DtoPayment dto) {
        return ResponseEntity.ok(serviceEventPayment.savePayment(dto));
    }

    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DtoResult> savePaymentWithFiles(
            @RequestPart("payment") DtoPayment dto,
            @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(serviceEventPayment.savePaymentWithFiles(dto, files));
    }

    @GetMapping("/by-budget/{budgetId}")
    public ResponseEntity<List<DtoPayment>> getByBudget(@PathVariable Integer budgetId) {
        return ResponseEntity.ok(serviceEventPayment.getPaymentsByBudgetId(budgetId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DtoResult> deletePayment(@PathVariable Integer id) {
        return ResponseEntity.ok(serviceEventPayment.deletePayment(id));
    }
}
