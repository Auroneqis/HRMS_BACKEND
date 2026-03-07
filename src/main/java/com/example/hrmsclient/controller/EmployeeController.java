package com.example.hrmsclient.controller;

import com.example.hrmsclient.dto.*;
import com.example.hrmsclient.entity.EmploymentStatus;
import com.example.hrmsclient.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")

public class EmployeeController {

    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService) {
    	  this.employeeService=employeeService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> create(
            @Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.status(201)
            .body(ApiResponse.success(employeeService.createEmployee(dto), "Employee created"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getById(id), "Success"));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<EmployeeResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String dir) {
        return ResponseEntity.ok(
            ApiResponse.success(employeeService.getAll(page, size, sortBy, dir), "Success"));
    }

    // GET /api/v1/employees/search?q=john
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponseDTO<EmployeeResponseDTO>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            ApiResponse.success(employeeService.search(q, page, size), "Success"));
    }

    @GetMapping("/filter/department")
    public ResponseEntity<ApiResponse<PageResponseDTO<EmployeeResponseDTO>>> filterByDept(
            @RequestParam String dept,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            ApiResponse.success(employeeService.filterByDepartment(dept, page, size), "Success"));
    }

    // GET /api/v1/employees/filter/status?status=ACTIVE
    @GetMapping("/filter/status")
    public ResponseEntity<ApiResponse<PageResponseDTO<EmployeeResponseDTO>>> filterByStatus(
            @RequestParam EmploymentStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            ApiResponse.success(employeeService.filterByStatus(status, page, size), "Success"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> update(
            @PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        return ResponseEntity.ok(
            ApiResponse.success(employeeService.updateEmployee(id, dto), "Employee updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted"));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<String>>> getDepartments() {
        return ResponseEntity.ok(
            ApiResponse.success(employeeService.getAllDepartments(), "Success"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard() {
        return ResponseEntity.ok(
            ApiResponse.success(employeeService.getDashboardSummary(), "Success"));
    }
}