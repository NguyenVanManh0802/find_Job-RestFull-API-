import { IBackendRes,ISubscriber, ICompany, IAccount, IUser, IModelPaginate, IGetAccount, IJob, IResume, IPermission, IRole, ISkill } from '@/types/backend';
import axios from 'config/axios-customize';


/**
 * search
 * 
 */



/**
 * Module Auth
 */
export const callRegister = (user: any) => {
    // user chính là object { name, email, password, ... }
    return axios.post('/api/v1/auth/register', user);
}

export const callLogin = (username: string, password: string) => {
    return axios.post<IBackendRes<IAccount>>('/api/v1/auth/login', { username, password })
}

export const callFetchAccount = () => {
    return axios.get<IBackendRes<IGetAccount>>('/api/v1/auth/account')
}

export const callRefreshToken = () => {
    return axios.get<IBackendRes<IAccount>>('/api/v1/auth/refresh')
}

export const callLogout = () => {
    return axios.post<IBackendRes<string>>('/api/v1/auth/logout')
}

/**
 * Upload single file
 */
export const callUploadSingleFile = (file: any, folderType: string) => {
    const bodyFormData = new FormData();
    bodyFormData.append('file', file);
    bodyFormData.append('folder', folderType);

    return axios<IBackendRes<{ fileName: string }>>({
        method: 'post',
        url: '/api/v1/files',
        data: bodyFormData,
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });
}

/**
 * Module Company
 */
export const callCreateCompany = (name: string, address: string, description: string, logo: string) => {
    return axios.post<IBackendRes<ICompany>>('/api/v1/companies', { name, address, description, logo })
}

// --- FIX: Thêm /{id} vào URL ---
export const callUpdateCompany = (id: string, name: string, address: string, description: string, logo: string) => {
    return axios.put<IBackendRes<ICompany>>(`/api/v1/companies/${id}`, { id, name, address, description, logo })
}

export const callDeleteCompany = (id: string) => {
    return axios.delete<IBackendRes<ICompany>>(`/api/v1/companies/${id}`);
}

export const callFetchCompany = (query: string) => {
    return axios.get<IBackendRes<IModelPaginate<ICompany>>>(`/api/v1/companies?${query}`);
}

export const callFetchCompanyById = (id: string) => {
    return axios.get<IBackendRes<ICompany>>(`/api/v1/companies/${id}`);
}

/**
 * Module Skill
 */
export const callCreateSkill = (name: string) => {
    return axios.post<IBackendRes<ISkill>>('/api/v1/skills', { name })
}

// --- FIX: Thêm /{id} vào URL (Dự phòng nếu backend yêu cầu) ---
export const callUpdateSkill = (id: string, name: string) => {
    return axios.put<IBackendRes<ISkill>>(`/api/v1/skills/${id}`, { id, name })
}

export const callDeleteSkill = (id: string) => {
    return axios.delete<IBackendRes<ISkill>>(`/api/v1/skills/${id}`);
}

export const callFetchAllSkill = (query: string) => {
    return axios.get<IBackendRes<IModelPaginate<ISkill>>>(`/api/v1/skills?${query}`);
}

/**
 * Module User
 */
export const callCreateUser = (user: IUser) => {
    return axios.post<IBackendRes<IUser>>('/api/v1/users', { ...user })
}

// --- FIX: Thêm /{id} vào URL ---
export const callUpdateUser = (user: IUser) => {
    return axios.put<IBackendRes<IUser>>(`/api/v1/users/${user.id}`, { ...user })
}

export const callDeleteUser = (id: string) => {
    return axios.delete<IBackendRes<IUser>>(`/api/v1/users/${id}`);
}

export const callFetchUser = (query: string) => {
    return axios.get<IBackendRes<IModelPaginate<IUser>>>(`/api/v1/users?${query}`);
}

/**
 * Module Job
 */
export const callCreateJob = (job: IJob) => {
    return axios.post<IBackendRes<IJob>>('/api/v1/jobs', { ...job })
}

// --- FIX: Thêm /{id} vào URL ---
export const callUpdateJob = (job: IJob, id: string) => {
    return axios.put<IBackendRes<IJob>>(`/api/v1/jobs/${id}`, { id, ...job })
}

export const callDeleteJob = (id: string) => {
    return axios.delete<IBackendRes<IJob>>(`/api/v1/jobs/${id}`);
}

export const callFetchJob = (query: string) => {
    return axios.get<IBackendRes<IModelPaginate<IJob>>>(`/api/v1/jobs?${query}`);
}

export const callFetchJobById = (id: string) => {
    return axios.get<IBackendRes<IJob>>(`/api/v1/jobs/${id}`);
}


export const callCreateSubscriber = (companyId: number | string) => {
    return axios.post<IBackendRes<ISubscriber>>('/api/v1/subscribers/subscribe', { companyId });
}
/**
 * Module Resume
 */
export const callCreateResume = (url: string, jobId: any, email: string, userId: string | number) => {
    const body = {
        url: url,
        email: email,
        status: "PENDING",
        job: { id: jobId },
        user: { id: userId },
    };
    return axios.post<IBackendRes<IResume>>('/api/v1/resumes', body);
}

// --- FIX: Thêm /{id} vào URL (Dự phòng) ---
export const callUpdateResumeStatus = (id: any, status: string) => {
    return axios.put<IBackendRes<IResume>>(`/api/v1/resumes/${id}`, { status })
}

export const callDeleteResume = (id: string) => {
    return axios.delete<IBackendRes<IResume>>(`/api/v1/resumes/${id}`);
}

export const callFetchResume = (query: string) => {
    return axios.get<IBackendRes<IModelPaginate<IResume>>>(`/api/v1/resumes?${query}`);
}

export const callFetchResumeById = (id: string) => {
    return axios.get<IBackendRes<IResume>>(`/api/v1/resumes/${id}`);
}

export const callFetchResumeByUser = () => {
    // FIX: Đảm bảo đường dẫn có chữ 's' => /by-users
    return axios.post<IBackendRes<IModelPaginate<IResume>>>(`/api/v1/resumes/by-users`);
}

/**
 * Module Permission
 */
export const callCreatePermission = (permission: IPermission) => {
    return axios.post<IBackendRes<IPermission>>('/api/v1/permissions', { ...permission })
}

// --- FIX: Thêm /{id} vào URL ---
export const callUpdatePermission = (permission: IPermission, id: string) => {
    return axios.put<IBackendRes<IPermission>>(`/api/v1/permissions/${id}`, { id, ...permission })
}

export const callDeletePermission = (id: string) => {
    return axios.delete<IBackendRes<IPermission>>(`/api/v1/permissions/${id}`);
}

export const callFetchPermission = (query: string) => {
    return axios.get<IBackendRes<IModelPaginate<IPermission>>>(`/api/v1/permissions?${query}`);
}

export const callFetchPermissionById = (id: string) => {
    return axios.get<IBackendRes<IPermission>>(`/api/v1/permissions/${id}`);
}

/**
 * Module Role
 */
export const callCreateRole = (role: IRole) => {
    return axios.post<IBackendRes<IRole>>('/api/v1/roles', { ...role })
}

// --- FIX: Thêm /{id} vào URL ---
export const callUpdateRole = (role: IRole, id: string) => {
    return axios.put<IBackendRes<IRole>>(`/api/v1/roles/${id}`, { ...role })
}

export const callDeleteRole = (id: string) => {
    return axios.delete<IBackendRes<IRole>>(`/api/v1/roles/${id}`);
}

export const callFetchRole = (query: string) => {
    return axios.get<IBackendRes<IModelPaginate<IRole>>>(`/api/v1/roles?${query}`);
}

export const callFetchRoleById = (id: string) => {
    return axios.get<IBackendRes<IRole>>(`/api/v1/roles/${id}`);
}

// Đổi mật khẩu (Giả định bạn có endpoint này, nếu chưa có phải viết thêm ở Backend)
// Nếu Backend chưa có, bạn có thể tạm dùng endpoint Update User để set password mới (nhưng cách này không an toàn vì không check pass cũ)
export const callChangePassword = (currentPassword: string, newPassword: string, confirmPassword: string) => {
    return axios.post('/api/v1/users/change-password', {
        currentPassword, // Backend chờ field này
        newPassword,
        confirmPassword
    });
}


// Gửi yêu cầu quên mật khẩu (Nhập email)
export const callForgotPassword = (email: string) => {
    return axios.post<IBackendRes<any>>('/api/v1/auth/forgot-password', { email });
}

// Đặt lại mật khẩu (Gửi token + mật khẩu mới)
export const callResetPassword = (token: string, newPassword: string, confirmPassword: string) => {
    return axios.post<IBackendRes<any>>('/api/v1/auth/reset-password', { 
        token, 
        newPassword, 
        confirmPassword 
    });
}

//api đăng nhập băng gg
export const callLoginGoogle = (idToken: string) => {
    // Body gửi lên phải có key là "idToken" để khớp với DTO Java GoogleLoginRequest
    return axios.post<IBackendRes<IAccount>>('/api/v1/auth/login-google', { 
        idToken: idToken 
    });
}