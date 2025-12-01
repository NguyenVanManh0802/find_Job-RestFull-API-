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
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.dto.ResponseData;
import vn.manh.findJob.dto.ResultPaginationDTO;
import vn.manh.findJob.service.RoleService;


@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ResponseData<Role>> createRole(@Valid @RequestBody Role role) {
        log.info("Request create Role: {}", role.getName());
        Role createdRole = roleService.createRole(role);
        ResponseData<Role> responseData = new ResponseData<>(
                HttpStatus.CREATED.value(), "Role created successfully", createdRole);
        // No location header needed for non-resource specific creation like role
        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @GetMapping
    public ResponseEntity<ResponseData<ResultPaginationDTO>> getAllRoles(
            @Filter Specification<Role> specification, Pageable pageable) {
        log.info("Request get all Roles");
        ResultPaginationDTO result = roleService.getAllRoles(specification, pageable);
        ResponseData<ResultPaginationDTO> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Fetch all roles successfully", result);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<Role>> getRoleById(@PathVariable long id) {
        log.info("Request get Role by id: {}", id);
        Role roleDTO = roleService.getRoleById(id);
        ResponseData<Role> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Fetch role successfully", roleDTO);
        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<Role>> updateRole(
            @PathVariable long id, @Valid @RequestBody Role role) {
        log.info("Request update Role id: {}", id);
        Role updatedRole = roleService.updateRole(id, role);
        ResponseData<Role> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Role updated successfully", updatedRole);
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> deleteRole(@PathVariable long id) {
        log.info("Request delete Role id: {}", id);
        roleService.deleteRole(id);
        ResponseData<Void> responseData = new ResponseData<>(
                HttpStatus.OK.value(), "Role deleted successfully");
        return ResponseEntity.ok(responseData);
    }
}