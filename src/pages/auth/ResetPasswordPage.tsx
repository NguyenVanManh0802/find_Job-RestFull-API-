import { Button, Form, Input, message, Row, Col, Typography, theme } from 'antd';
import { LockOutlined, CheckCircleOutlined, ArrowLeftOutlined, KeyOutlined } from '@ant-design/icons';
import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { callResetPassword } from '@/config/api';
import { motion } from 'framer-motion';

const { Title, Text } = Typography;

const ResetPasswordPage = () => {
    const [isSubmit, setIsSubmit] = useState(false);
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    
    // Ant Design Hooks để lấy màu chủ đạo của app
    const { token: themeToken } = theme.useToken();
    
    // Lấy token từ URL
    const token = searchParams.get("token");

    useEffect(() => {
        if (!token) {
            message.error("Đường dẫn không hợp lệ hoặc đã hết hạn!");
            navigate('/login');
        }
    }, [token, navigate]);

    const onFinish = async (values: any) => {
        if (!token) return;
        setIsSubmit(true);
        
        const res = await callResetPassword(token, values.newPassword, values.confirmPassword);
        
        if (res && +res.status === 200) {
            message.success("Đặt lại mật khẩu thành công! Đang chuyển hướng...", 2);
            setTimeout(() => {
                navigate('/login');
            }, 2000);
        } else {
            message.error(res?.message || "Token không hợp lệ hoặc đã hết hạn.");
        }
        setIsSubmit(false);
    };

    return (
        <div style={{ minHeight: '100vh', background: '#fff', overflow: 'hidden' }}>
            <Row style={{ minHeight: '100vh' }}>
                
                {/* --- CỘT TRÁI: FORM --- */}
                {/* Trên mobile chiếm hết (24), trên PC chiếm 8-10 phần */}
                <Col xs={24} md={10} lg={8} style={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    alignItems: 'center', 
                    padding: '40px',
                    position: 'relative',
                    backgroundColor: '#ffffff'
                }}>
                    {/* Nút quay lại trang chủ */}
                    <div style={{ position: 'absolute', top: 30, left: 30 }}>
                        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#666', fontWeight: 500, textDecoration: 'none' }}>
                            <ArrowLeftOutlined /> Trang chủ
                        </Link>
                    </div>

                    <motion.div
                        initial={{ opacity: 0, x: -50 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.6, ease: "easeOut" }}
                        style={{ width: '100%', maxWidth: 400 }}
                    >
                        <div style={{ marginBottom: 40 }}>
                            {/* Logo Icon */}
                            <div style={{ 
                                width: 56, height: 56, 
                                background: '#e6f4ff', // Màu xanh rất nhạt
                                borderRadius: 16, 
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                marginBottom: 24,
                                color: themeToken.colorPrimary // Dùng màu chủ đạo của Antd
                            }}>
                                <KeyOutlined style={{ fontSize: 28 }} />
                            </div>
                            
                            <Title level={2} style={{ margin: '0 0 12px 0', fontWeight: 700, color: '#1f1f1f' }}>
                                Đặt lại mật khẩu
                            </Title>
                            <Text type="secondary" style={{ fontSize: 16, lineHeight: 1.5 }}>
                                Tạo mật khẩu mới để bảo vệ tài khoản của bạn an toàn hơn.
                            </Text>
                        </div>

                        <Form onFinish={onFinish} layout="vertical" size="large">
                            <Form.Item
                                label={<span style={{ fontWeight: 500 }}>Mật khẩu mới</span>}
                                name="newPassword"
                                rules={[
                                    { required: true, message: 'Vui lòng nhập mật khẩu mới!' }, 
                                    { min: 6, message: 'Mật khẩu phải từ 6 ký tự!' }
                                ]}
                            >
                                <Input.Password 
                                    prefix={<LockOutlined style={{ color: '#bfbfbf', marginRight: 5 }} />} 
                                    placeholder="••••••••" 
                                    style={{ borderRadius: 8, padding: '10px 12px' }}
                                />
                            </Form.Item>

                            <Form.Item
                                label={<span style={{ fontWeight: 500 }}>Xác nhận mật khẩu</span>}
                                name="confirmPassword"
                                dependencies={['newPassword']}
                                rules={[
                                    { required: true, message: 'Vui lòng nhập lại mật khẩu!' },
                                    ({ getFieldValue }) => ({
                                        validator(_, value) {
                                            if (!value || getFieldValue('newPassword') === value) {
                                                return Promise.resolve();
                                            }
                                            return Promise.reject(new Error('Mật khẩu xác nhận không khớp!'));
                                        },
                                    }),
                                ]}
                            >
                                <Input.Password 
                                    prefix={<CheckCircleOutlined style={{ color: '#bfbfbf', marginRight: 5 }} />} 
                                    placeholder="••••••••" 
                                    style={{ borderRadius: 8, padding: '10px 12px' }}
                                />
                            </Form.Item>

                            <Form.Item style={{ marginTop: 32 }}>
                                <Button 
                                    type="primary" 
                                    htmlType="submit" 
                                    loading={isSubmit} 
                                    block 
                                    style={{ 
                                        height: 50, 
                                        borderRadius: 8, 
                                        fontSize: 16, 
                                        fontWeight: 600,
                                        boxShadow: '0 4px 14px 0 rgba(22, 119, 255, 0.3)' // Đổ bóng nhẹ cho nút
                                    }}
                                >
                                    Xác nhận thay đổi
                                </Button>
                            </Form.Item>
                            
                            <div style={{ textAlign: 'center', marginTop: 24 }}>
                                <Text type="secondary" style={{ fontSize: 15 }}>Bạn nhớ mật khẩu rồi? </Text>
                                <Link to="/login" style={{ fontWeight: 600, color: themeToken.colorPrimary }}>
                                    Đăng nhập ngay
                                </Link>
                            </div>
                        </Form>
                    </motion.div>
                </Col>

                {/* --- CỘT PHẢI: BANNER/IMAGE --- */}
                {/* Ẩn trên mobile (xs), hiện trên PC */}
                <Col xs={0} md={14} lg={16} style={{ 
                    background: 'linear-gradient(135deg, #1677ff 0%, #69c0ff 100%)', // Gradient xanh hiện đại
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    position: 'relative',
                    overflow: 'hidden'
                }}>
                    {/* Họa tiết trang trí nền */}
                    <div style={{ position: 'absolute', width: 600, height: 600, background: 'rgba(255,255,255,0.08)', borderRadius: '50%', top: -100, right: -150 }}></div>
                    <div style={{ position: 'absolute', width: 400, height: 400, background: 'rgba(255,255,255,0.08)', borderRadius: '50%', bottom: 50, left: -100 }}></div>

                    <div style={{ textAlign: 'center', color: '#fff', padding: 60, zIndex: 1, maxWidth: 600 }}>
                        <motion.div
                            initial={{ opacity: 0, scale: 0.9, y: 20 }}
                            animate={{ opacity: 1, scale: 1, y: 0 }}
                            transition={{ duration: 0.8, delay: 0.2 }}
                        >
                            <img 
                                // Sử dụng ảnh minh họa 3D hoặc Flat bảo mật chất lượng cao
                                src="https://cdni.iconscout.com/illustration/premium/thumb/secure-data-concept-2975820-2476896.png" 
                                alt="Security Illustration" 
                                style={{ width: '100%', maxWidth: '480px', marginBottom: 30, filter: 'drop-shadow(0 10px 20px rgba(0,0,0,0.15))' }}
                            />
                        </motion.div>
                        
                        <motion.div
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.8, delay: 0.4 }}
                        >
                            <Title level={1} style={{ color: '#fff', margin: '0 0 15px 0', fontSize: 36 }}>
                                Bảo mật tuyệt đối
                            </Title>
                            <Text style={{ color: 'rgba(255,255,255,0.9)', fontSize: 18, fontWeight: 300 }}>
                                Chúng tôi áp dụng các tiêu chuẩn bảo mật cao nhất để đảm bảo dữ liệu của bạn luôn an toàn.
                            </Text>
                        </motion.div>
                    </div>
                </Col>
            </Row>
        </div>
    );
};
export default ResetPasswordPage;