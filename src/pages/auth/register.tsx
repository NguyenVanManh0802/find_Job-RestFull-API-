import { Button, Col, Divider, Form, Input, Row, Select, message, Checkbox } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, HomeOutlined, PhoneOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import styles from '@/styles/auth.module.scss'; // File CSS riêng cho Auth
import { useState } from 'react';
import { callRegister } from '@/config/api'; // Giả sử bạn có API này

const RegisterPage = () => {
    const navigate = useNavigate();
    const [isSubmit, setIsSubmit] = useState(false);

    const onFinish = async (values: any) => {
        const { name, email, password, age, gender, address } = values;
        setIsSubmit(true);
        
        // Gọi API đăng ký (thay bằng logic thực tế của bạn)
        const res = await callRegister(name, email, password, age, gender, address);
        
        if (res?.data?.id) {
            message.success('Đăng ký tài khoản thành công!');
            navigate('/login');
        } else {
            message.error('Đăng ký thất bại: ' + (res?.message || "Lỗi không xác định"));
        }
        setIsSubmit(false);
    };

    return (
        <div className={styles['register-page-container']}>
            <div className={styles['register-box-wrapper']}>
                <Row style={{ height: '100%' }}>
                    {/* CỘT TRÁI: HÌNH ẢNH & SLOGAN */}
                    <Col xs={0} md={10} className={styles['register-left-side']}>
                        <div className={styles['bg-overlay']}></div>
                        <div className={styles['content-overlay']}>
                            <h2 className={styles['brand']}>FIND<span className={styles['red-text']}>JOB</span></h2>
                            <h1 className={styles['slogan']}>Khởi đầu sự nghiệp<br />vững chắc cùng chúng tôi</h1>
                            <p className={styles['desc']}>Hàng ngàn cơ hội việc làm hấp dẫn đang chờ đón bạn.</p>
                            
                        </div>
                    </Col>
                    {/* CỘT PHẢI: FORM ĐĂNG KÝ */}
                    <Col xs={24} md={14} className={styles['register-right-side']}>
                        <div className={styles['form-container']}>
                            <div className={styles['header-form']}>
                                <h3>Đăng Ký Tài Khoản</h3>
                                <p>Chào mừng bạn đến với cộng đồng FINDJOB</p>
                            </div>

                            <Form
                                name="register"
                                onFinish={onFinish}
                                autoComplete="off"
                                layout="vertical"
                                size="large"
                            >
                                <Row gutter={[16, 0]}>
                                    <Col span={24}>
                                        <Form.Item
                                            name="name"
                                            rules={[{ required: true, message: 'Vui lòng nhập họ tên!' }]}
                                        >
                                            <Input prefix={<UserOutlined className="site-form-item-icon" />} placeholder="Họ và tên" />
                                        </Form.Item> 
                                    </Col>

                                    <Col span={24}>
                                        <Form.Item
                                            name="email"
                                            rules={[
                                                { required: true, message: 'Vui lòng nhập email!' },
                                                { type: 'email', message: 'Email không hợp lệ!' }
                                            ]}
                                        >
                                            <Input prefix={<MailOutlined className="site-form-item-icon" />} placeholder="Email" />
                                        </Form.Item>
                                    </Col>

                                    <Col span={12}>
                                        <Form.Item
                                            name="password"
                                            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }]}
                                        >
                                            <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu" />
                                        </Form.Item>
                                    </Col>
                                    <Col span={12}>
                                        <Form.Item
                                            name="age"
                                            rules={[{ required: true, message: 'Nhập tuổi!' }]}
                                        >
                                            <Input type='number' placeholder="Tuổi" />
                                        </Form.Item>
                                    </Col>

                                    <Col span={12}>
                                        <Form.Item
                                            name="gender"
                                            rules={[{ required: true, message: 'Chọn giới tính!' }]}
                                        >
                                            <Select placeholder="Giới tính">
                                                <Select.Option value="MALE">Nam</Select.Option>
                                                <Select.Option value="FEMALE">Nữ</Select.Option>
                                                <Select.Option value="OTHER">Khác</Select.Option>
                                            </Select>
                                        </Form.Item>
                                    </Col>

                                    <Col span={12}>
                                        <Form.Item
                                            name="address"
                                            rules={[{ required: true, message: 'Nhập địa chỉ!' }]}
                                        >
                                            <Input prefix={<HomeOutlined />} placeholder="Địa chỉ" />
                                        </Form.Item>
                                    </Col>
                                </Row>

                                <Form.Item>
                                    <Button 
                                        type="primary" 
                                        htmlType="submit" 
                                        className={styles['btn-submit']} 
                                        loading={isSubmit}
                                        block
                                    >
                                        Đăng Ký Ngay
                                    </Button>
                                </Form.Item>
                            </Form>

                            <Divider plain>Hoặc</Divider>
                            
                            <div className={styles['footer-form']}>
                                Đã có tài khoản? <Link to="/login">Đăng nhập tại đây</Link>
                            </div>
                        </div>
                    </Col>
                </Row>
            </div>
        </div>
    );
};

export default RegisterPage;