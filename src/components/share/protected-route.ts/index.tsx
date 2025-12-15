import { Navigate, useLocation } from "react-router-dom";
import { useAppSelector } from "@/redux/hooks";
import NotPermitted from "./not-permitted";
import Loading from "../loading";

const RoleBaseRoute = (props: any) => {
    const user = useAppSelector(state => state.account.user);
    const userRole = user?.role?.name;
    const location = useLocation();

    // LOGIC CHECK QUYỀN:
    if (location.pathname.startsWith('/admin')) {
        // Trường hợp 1: Dữ liệu Role chưa tải xong -> Coi như không có quyền -> Chặn hoặc Loading
        if (!userRole) {
             // Tốt nhất là hiện Loading hoặc NotPermitted tạm thời
             return <NotPermitted /> 
        }

        // Trường hợp 2: Đã có Role, nhưng là USER -> Chặn
        if (userRole === 'USER') {
            return <NotPermitted />
        }
    }

    // Các trường hợp khác -> Cho qua
    return (<>{props.children}</>)
}

const ProtectedRoute = (props: any) => {
    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated)
    const isLoading = useAppSelector(state => state.account.isLoading)

    return (
        <>
            {/* Quan trọng: Phải chờ isLoading = false mới render nội dung bên trong */}
            {isLoading === true ?
                <Loading />
                :
                <>
                    {isAuthenticated === true ?
                        <RoleBaseRoute>
                            {props.children}
                        </RoleBaseRoute>
                        :
                        <Navigate to='/login' replace />
                    }
                </>
            }
        </>
    )
}

export default ProtectedRoute;