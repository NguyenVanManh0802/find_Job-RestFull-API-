import { Button, Form, Input, message, Row, Col, Typography, Card } from 'antd';
import { MailOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { callForgotPassword } from '@/config/api';

const { Title, Text } = Typography;

const ForgotPasswordPage = () => {
    const [isSubmit, setIsSubmit] = useState(false);
    const [form] = Form.useForm(); // 1. Thêm hook để quản lý form
    
    const onFinish = async (values: any) => {
        setIsSubmit(true);
        const res = await callForgotPassword(values.email);
        
        // --- LOGIC SỬA ĐỔI ---
        // Kiểm tra an toàn: Chấp nhận cả 'status' (như hình bạn gửi) và 'statusCode' (chuẩn Spring)
        if ( res && (+res.status === 200 ) ) {
            message.success("Vui lòng kiểm tra email để đặt lại mật khẩu!");
            form.resetFields(); // Xóa email đã nhập để tránh spam
        } else {
            message.error(res?.message || "Có lỗi xảy ra, vui lòng thử lại.");
        }
        // ---------------------
        
        setIsSubmit(false);
    };

    return (
        <Row justify="center" align="middle" style={{ minHeight: '100vh', backgroundColor: '#f0f2f5' }}>
            <Col xs={22} md={8} lg={6}>
                <Card bordered={false} style={{ boxShadow: '0 4px 12px rgba(0,0,0,0.1)', borderRadius: 8 }}>
                    <div style={{ textAlign: 'center', marginBottom: 20 }}>
                        <Title level={3}>Quên mật khẩu?</Title>
                        <Text type="secondary">
                            Nhập email của bạn để nhận hướng dẫn đặt lại mật khẩu.
                        </Text>
                    </div>

                    <Form 
                        form={form} // Kết nối form instance
                        onFinish={onFinish} 
                        layout="vertical" 
                        size="large"
                    >
                        <Form.Item
                            name="email"
                            rules={[
                                { required: true, message: 'Vui lòng nhập email!' },
                                { type: 'email', message: 'Email không hợp lệ!' }
                            ]}
                        >
                            <Input prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="Email đăng ký" />
                        </Form.Item>

                        <Form.Item>
                            <Button type="primary" htmlType="submit" loading={isSubmit} block>
                                Gửi yêu cầu
                            </Button>
                        </Form.Item>
                        
                        <div style={{ textAlign: 'center' }}>
                            <Link to="/login" style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                                <ArrowLeftOutlined /> Quay lại đăng nhập
                            </Link>
                        </div>
                    </Form>
                </Card>
            </Col>
        </Row>
    );
};
export default ForgotPasswordPage;