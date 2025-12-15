import { LockOutlined, KeyOutlined, CheckCircleOutlined, SafetyCertificateOutlined } from "@ant-design/icons";
import { Button, Form, Input, message, notification, Row, Col, Alert, Typography } from "antd";
import { useState } from "react";
import { callChangePassword } from "@/config/api"; 

const { Title, Text } = Typography;

const ChangePassword = () => {
    const [form] = Form.useForm();
    const [isSubmit, setIsSubmit] = useState(false);

    const onFinish = async (values: any) => {
        const { currentPassword, newPassword, confirmPassword } = values;
        setIsSubmit(true);
        
        // Gọi API đổi mật khẩu
        const res = await callChangePassword(currentPassword, newPassword, confirmPassword);
        
        if (res && +res.status === 200) {
            message.success("Đổi mật khẩu thành công");
            form.resetFields();
        } else {
            notification.error({
                message: "Đổi mật khẩu thất bại",
                description: res?.message || "Mật khẩu hiện tại không chính xác"
            });
        }
        setIsSubmit(false);
    };

    return (
        <div style={{ padding: '20px 0' }}>
            {/* Căn giữa Form trên màn hình */}
            <Row gutter={[20, 20]} justify="center">
                
                {/* Cột 1: Form đổi mật khẩu */}
                <Col xs={24} md={14}>
                    <Form
                        form={form}
                        onFinish={onFinish}
                        layout="vertical"
                        size="large" // Form to rõ dễ nhìn
                    >
                        <Form.Item
                            label="Mật khẩu hiện tại"
                            name="currentPassword"
                            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu hiện tại!' }]}
                        >
                            <Input.Password 
                                prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} 
                                placeholder="Nhập mật khẩu cũ..." 
                            />
                        </Form.Item>

                        <Form.Item
                            label="Mật khẩu mới"
                            name="newPassword"
                            rules={[
                                { required: true, message: 'Vui lòng nhập mật khẩu mới!' },
                                { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' }
                            ]}
                        >
                            <Input.Password 
                                prefix={<KeyOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} 
                                placeholder="Nhập mật khẩu mới..." 
                            />
                        </Form.Item>

                        <Form.Item
                            label="Xác nhận mật khẩu mới"
                            name="confirmPassword"
                            dependencies={['newPassword']}
                            rules={[
                                { required: true, message: 'Vui lòng xác nhận mật khẩu!' },
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
                                prefix={<CheckCircleOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} 
                                placeholder="Nhập lại mật khẩu mới..." 
                            />
                        </Form.Item>

                        <Form.Item style={{ marginTop: 30 }}>
                            <Button type="primary" htmlType="submit" loading={isSubmit} block>
                                Xác nhận thay đổi
                            </Button>
                        </Form.Item>
                    </Form>
                </Col>

                {/* Cột 2: Gợi ý bảo mật (Chỉ hiện trên màn hình to) */}
                <Col xs={0} md={10} style={{ paddingLeft: 20 }}>
                    <div style={{ backgroundColor: '#f9f9f9', padding: 20, borderRadius: 8 }}>
                        <Title level={5} style={{ marginTop: 0 }}>
                            <SafetyCertificateOutlined style={{ color: '#52c41a', marginRight: 8 }} />
                            Lưu ý bảo mật
                        </Title>
                        <ul style={{ paddingLeft: 20, color: '#666', fontSize: 13, lineHeight: '1.8' }}>
                            <li>Mật khẩu phải có ít nhất 6 ký tự.</li>
                            <li>Nên sử dụng kết hợp chữ hoa, chữ thường, số và ký tự đặc biệt.</li>
                            <li>Không nên sử dụng mật khẩu dễ đoán như ngày sinh, số điện thoại.</li>
                            <li>Đổi mật khẩu định kỳ để bảo vệ tài khoản tốt hơn.</li>
                        </ul>
                        
                        <Alert
                            message="An toàn"
                            description="Tài khoản của bạn đang được bảo vệ bằng các tiêu chuẩn mã hóa mới nhất."
                            type="info"
                            showIcon
                            style={{ marginTop: 20 }}
                        />
                    </div>
                </Col>
            </Row>
        </div>
    );
};

export default ChangePassword;