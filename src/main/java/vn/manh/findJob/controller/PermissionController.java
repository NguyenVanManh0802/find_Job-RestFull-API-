package vn.manh.findJob.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.manh.findJob.domain.Permission;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.service.PermissionService;


@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<ResponseData<Permission>> createPermission(@Valid @RequestBody Permission permission) {
        log.info("Request create Permission: {}", permission.getName());
        Permission createdPermission = permissionService.createPermission(permission);
        ResponseData<Permission> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(), "Permission created successfully", createdPermission);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @GetMapping
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllPermissions(
            @Filter Specification<Permission> specification, Pageable pageable) {
        log.info("Request get all Permissions");
        ResultPaginationDTO result = permissionService.getAllPermissions(specification, pageable);
        ResponseData<ResultPaginationDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Fetch all permissions successfully", result);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<Permission>> getPermissionById(@PathVariable long id) {
        log.info("Request get Permission by id: {}", id);
        Permission permissionDTO = permissionService.getPermissionById(id);
        ResponseData<Permission> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Fetch permission successfully", permissionDTO);
        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Permission>> updatePermission(
            @PathVariable long id, @Valid @RequestBody Permission permission) {
        log.info("Request update Permission id: {}", id);
        Permission updatedPermission = permissionService.updatePermission(id, permission);
        ResponseData<Permission> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Permission updated successfully", updatedPermission);
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deletePermission(@PathVariable long id) {
        log.info("Request delete Permission id: {}", id);
        permissionService.deletePermission(id);
        ResponseData<Void> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Permission deleted successfully");
        return ResponseEntity.ok(responseData);
    }
}