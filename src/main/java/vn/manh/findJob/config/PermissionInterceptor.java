package vn.manh.findJob.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import vn.manh.findJob.domain.Permission;
import vn.manh.findJob.domain.Role;
import vn.manh.findJob.domain.User;
import vn.manh.findJob.exception.PermissionException;
import vn.manh.findJob.service.SecurityUtil;
import vn.manh.findJob.service.UserService;


import java.util.List;

public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;
    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);
        //check permission
        boolean  isAllow=false;
        String email= SecurityUtil.getCurrentUserLogin().isPresent()==true
                ? SecurityUtil.getCurrentUserLogin().get() : "";
        if(email!=null && !email.isEmpty())
        {
            User user=userService.handleGetUserByUserName(email);
            if(user!=null)
            {
                Role role = user.getRole();
                if(role!=null)
                {
                    List<Permission> permissionList=role.getPermissions();
                    isAllow=permissionList.stream().anyMatch(item->item.getApiPath().equals(path)
                    && item.getMethod().equals(httpMethod));
                    if(!isAllow)
                    {
                        throw new PermissionException("Bạn không có quyền truy cập tài nguyên");
                    }
                }
                else{
                    if(path.equals("/api/v1/resumes/by-users") || path.equals("/api/v1/resumes"))
                    {
                        isAllow=true;
                    }
                    else
                        throw new PermissionException("Bạn không có quyền truy cập tài nguyên");
                }

            }
        }
        return isAllow ;
    }
}
