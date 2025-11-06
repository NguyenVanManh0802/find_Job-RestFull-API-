package vn.manh.findJob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.exception.ResourceNotFoundException;

import vn.manh.findJob.repository.RoleRepository;



@Slf4j
@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private Role findRoleById(long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }


    public Role getRoleById(long id) {
        log.info("Fetching role with id={}", id);
        Role role = this.findRoleById(id);
        return role;
    }


}