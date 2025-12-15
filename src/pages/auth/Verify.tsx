import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Result, Button, Spin } from 'antd';
import axios from '@/config/axios-customize'; // Chú ý đường dẫn import axios của bạn
import styles from '@/styles/auth.module.scss'; // Hoặc file css bạn đã tạo

const VerifyPage = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const navigate = useNavigate();

    // Các trạng thái: 'loading' | 'success' | 'failed'
    const [status, setStatus] = useState("loading");
    const [message, setMessage] = useState("");

    // Hàm gọi API
    const verifyAccount = async () => {
        try {
            // Gọi API
            const res = await axios.get(`/api/v1/auth/verify?token=${token}`);
            
            // Nếu thành công (Status 200)
            if (res && res.data) {
                setStatus("success");
                setMessage("Tài khoản của bạn đã được kích hoạt thành công!");
            } else {
                // Trường hợp lạ (200 nhưng không có data)
                 setStatus("success"); // Vẫn cho là success để user không hoang mang
            }
        } catch (error: any) {
            // --- ĐÂY LÀ ĐOẠN QUAN TRỌNG NHẤT ---
            // Lấy thông báo lỗi từ Backend trả về
            const errorMsg = error?.response?.data?.message || error?.message || "Lỗi không xác định";

            // Kiểm tra: Nếu lỗi chứa từ khóa "đã được kích hoạt" (như trong hình 3 của bạn)
            if (errorMsg.toLowerCase().includes("đã được kích hoạt") || errorMsg.includes("active")) {
                setStatus("success");
                setMessage("Tài khoản đã được kích hoạt trước đó. Bạn có thể đăng nhập ngay.");
            } else {
                // Các lỗi khác (Token sai, hết hạn...)
                setStatus("failed");
                setMessage(errorMsg);
            }
        }
    };

    useEffect(() => {
        if (token) {
            verifyAccount();
        } else {
            setStatus("failed");
            setMessage("Đường dẫn không hợp lệ (Thiếu token).");
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token]);

    // --- PHẦN GIAO DIỆN (UI) ---
    return (
        // Style container để căn giữa và đẩy footer xuống
        <div style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center', 
            minHeight: '80vh', 
            backgroundColor: '#f5f5f5' 
        }}>
            <div style={{ 
                background: 'white', 
                padding: '40px', 
                borderRadius: '10px', 
                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                maxWidth: '600px',
                width: '100%'
            }}>
                
                {/* TRƯỜNG HỢP 1: ĐANG XOAY (LOADING) */}
                {status === 'loading' && (
                    <Result
                        icon={<Spin size="large" />}
                        title="Đang xác thực..."
                        subTitle="Hệ thống đang kiểm tra thông tin kích hoạt của bạn."
                    />
                )}

                {/* TRƯỜNG HỢP 2: THÀNH CÔNG (SUCCESS) */}
                {status === 'success' && (
                    <Result
                        status="success"
                        title="Kích hoạt thành công!"
                        subTitle={message}
                        extra={[
                            <Button type="primary" key="login" onClick={() => navigate('/login')}>
                                Đăng nhập ngay
                            </Button>
                        ]}
                    />
                )}

                {/* TRƯỜNG HỢP 3: THẤT BẠI (FAILED) */}
                {status === 'failed' && (
                    <Result
                        status="error"
                        title="Xác thực thất bại"
                        subTitle={message} // Hiển thị lý do lỗi cụ thể
                        extra={[
                            <Button type="primary" key="retry" onClick={() => navigate('/register')}>
                                Đăng ký lại
                            </Button>,
                            <Button key="home" onClick={() => navigate('/')}>
                                Về trang chủ
                            </Button>
                        ]}
                    />
                )}
            </div>
        </div>
    );
};

export default VerifyPage;