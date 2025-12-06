import { Button, Col, Divider, Form, Input, Row, message, notification, Checkbox } from 'antd';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { callLogin } from '@/config/api';
import { useState, useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { setUserLoginInfo } from '@/redux/slice/accountSlide';
import styles from '@/styles/auth.module.scss'; // Dùng chung file SCSS với Register
import { useAppSelector } from '@/redux/hooks';
import { UserOutlined, LockOutlined, GoogleOutlined, FacebookOutlined } from '@ant-design/icons';

const LoginPage = () => {
    const navigate = useNavigate();
    const [isSubmit, setIsSubmit] = useState(false);
    const dispatch = useDispatch();
    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated);

    let location = useLocation();
    let params = new URLSearchParams(location.search);
    const callback = params?.get("callback");

    useEffect(() => {
        if (isAuthenticated) {
            navigate('/');
        }
    }, [])

    const onFinish = async (values: any) => {
        const { username, password } = values;
        setIsSubmit(true);
        const res = await callLogin(username, password);
        setIsSubmit(false);

        if (res?.data) {
            localStorage.setItem('access_token', res.data.access_token);
            dispatch(setUserLoginInfo(res.data.user))
            message.success('Đăng nhập tài khoản thành công!');
            window.location.href = callback ? callback : '/';
        } else {
            notification.error({
                message: "Có lỗi xảy ra",
                description:
                    res.message && Array.isArray(res.message) ? res.message[0] : res.message,
                duration: 5
            })
        }
    };

    return (
        <div className={styles["register-page-container"]}> {/* Tái sử dụng container của Auth */}
            <div className={styles["register-box-wrapper"]}>
                <Row style={{ height: '100%' }}>
                    {/* CỘT TRÁI: HÌNH ẢNH & SLOGAN */}
                    <Col xs={0} md={10} className={styles['register-left-side']}>
                        <div className={styles['bg-overlay']}></div>
                        <div className={styles['content-overlay']}>
                            <h2 className={styles['brand']}>FIND<span className={styles['red-text']}>JOB</span></h2>
                            <h1 className={styles['slogan']}>Chào mừng trở lại!</h1>
                            <p className={styles['desc']}>Tiếp tục hành trình tìm kiếm công việc mơ ước của bạn ngay hôm nay.</p>
                            <img 
                                src="https://cdni.iconscout.com/illustration/premium/thumb/login-3305943-2757111.png" 
                                alt="Login" 
                                className={styles['hero-image']}
                            />
                        </div>
                    </Col>

                    {/* CỘT PHẢI: FORM ĐĂNG NHẬP */}
                    <Col xs={24} md={14} className={styles['register-right-side']}>
                        <div className={styles['form-container']}>
                            <div className={styles['header-form']}>
                                <h3>Đăng Nhập</h3>
                                <p>Vui lòng nhập thông tin tài khoản của bạn</p>
                            </div>

                            <Form
                                name="login"
                                onFinish={onFinish}
                                autoComplete="off"
                                layout="vertical"
                                size="large"
                            >
                                <Form.Item
                                    name="username"
                                    rules={[
                                        { required: true, message: 'Vui lòng nhập Email!' },
                                        { type: 'email', message: 'Email không hợp lệ!' }
                                    ]}
                                >
                                    <Input 
                                        prefix={<UserOutlined className="site-form-item-icon" />} 
                                        placeholder="Email" 
                                    />
                                </Form.Item>

                                <Form.Item
                                    name="password"
                                    rules={[{ required: true, message: 'Vui lòng nhập mật khẩu!' }]}
                                >
                                    <Input.Password 
                                        prefix={<LockOutlined />} 
                                        placeholder="Mật khẩu" 
                                    />
                                </Form.Item>

                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
                                    <Form.Item name="remember" valuePropName="checked" noStyle>
                                        <Checkbox>Ghi nhớ đăng nhập</Checkbox>
                                    </Form.Item>
                                    <a className={styles['forgot-password']} href="">Quên mật khẩu?</a>
                                </div>

                                <Form.Item>
                                    <Button 
                                        type="primary" 
                                        htmlType="submit" 
                                        className={styles['btn-submit']} 
                                        loading={isSubmit}
                                        block
                                    >
                                        Đăng Nhập
                                    </Button>
                                </Form.Item>
                            </Form>

                            <Divider plain>Hoặc đăng nhập bằng</Divider>
                            
                            <div className={styles['social-login']}>
                                <Button icon={<GoogleOutlined />} className={styles['btn-social']}>Google</Button>
                                <Button icon={<FacebookOutlined />} className={styles['btn-social']}>Facebook</Button>
                            </div>

                            <div className={styles['footer-form']} style={{ marginTop: 20 }}>
                                Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
                            </div>
                        </div>
                    </Col>
                </Row>
            </div>
        </div>
    )
}

export default LoginPage;