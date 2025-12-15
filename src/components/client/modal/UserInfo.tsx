import { AntDesignOutlined, UploadOutlined, UserOutlined, MailOutlined, EnvironmentOutlined, ManOutlined, WomanOutlined, CameraOutlined } from "@ant-design/icons";
import { Avatar, Button, Col, Form, Input, Row, Select, message, Upload, notification, InputNumber } from "antd";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { useEffect, useState } from "react";
import { callUpdateUser } from "@/config/api"; 
import { doUpdateUserInfoAction } from "@/redux/slice/accountSlide"; 

const UserInfo = () => {
    const user = useAppSelector(state => state.account.user);
    const [form] = Form.useForm();
    const dispatch = useAppDispatch();
    const [isSubmit, setIsSubmit] = useState(false);

    // Fill dữ liệu từ Redux vào Form
    useEffect(() => {
        if (user) {
            form.setFieldsValue({
                id: user.id,
                email: user.email,
                name: user.name,
                age: user.age,
                gender: user.gender,
                address: user.address,
            });
        }
    }, [user, form]);

    const onFinish = async (values: any) => {
        const { name, age, gender, address } = values;
        setIsSubmit(true);

        const res = await callUpdateUser({
            id: user.id, 
            name, 
            age, 
            gender, 
            address, 
            company: { id: user.company?.id }, 
            // Lưu ý: Backend nên bỏ qua cập nhật Role để tránh lỗi. 
            // Nhưng nếu backend yêu cầu phải gửi lại role cũ thì giữ dòng dưới:
            role: { id: user.role?.id }       
        } as any);

        if (res && res.data) {
            message.success("Cập nhật thông tin thành công");
            dispatch(doUpdateUserInfoAction({ name, age, gender, address }));
        } else {
            notification.error({
                message: "Đã có lỗi xảy ra",
                description: res.message
            });
        }
        setIsSubmit(false);
    };

    return (
        <div style={{ padding: '20px 0' }}>
            <Row gutter={[40, 0]}>
                {/* CỘT TRÁI: AVATAR */}
                <Col xs={24} md={8}>
                    <div style={{ 
                        display: 'flex', 
                        flexDirection: 'column', 
                        alignItems: 'center', 
                        justifyContent: 'center',
                        marginBottom: 20
                    }}>
                        <div style={{ position: 'relative', marginBottom: 15 }}>
                            <Avatar 
                                size={140} 
                                icon={<UserOutlined />} 
                                src={`${import.meta.env.VITE_BACKEND_URL}/storage/avatar/${user?.id}`} 
                                style={{ 
                                    border: '4px solid #f0f0f0', 
                                    boxShadow: '0 4px 10px rgba(0,0,0,0.1)' 
                                }}
                            />
                            {/* Nút giả lập Upload ảnh */}
                            <Upload showUploadList={false} disabled>
                                <Button 
                                    shape="circle" 
                                    type="primary"
                                    icon={<CameraOutlined />} 
                                    style={{ 
                                        position: 'absolute', 
                                        bottom: 5, 
                                        right: 5, 
                                        boxShadow: '0 2px 8px rgba(0,0,0,0.2)' 
                                    }}
                                />
                            </Upload>
                        </div>
                        <h3 style={{ margin: 0, fontSize: 18 }}>{user?.name}</h3>
                        <span style={{ color: '#888' }}>{user?.email}</span>
                    </div>
                </Col>
                
                {/* CỘT PHẢI: FORM */}
                <Col xs={24} md={16}>
                    <Form
                        form={form}
                        onFinish={onFinish}
                        layout="vertical"
                        size="large" // Form to rõ hơn
                    >
                        <Form.Item label="Email" name="email">
                            <Input disabled prefix={<MailOutlined style={{ color: '#bfbfbf' }} />} style={{ backgroundColor: '#f5f5f5', color: '#888' }} />
                        </Form.Item>

                        <Form.Item 
                            label="Tên hiển thị" 
                            name="name" 
                            rules={[{ required: true, message: 'Vui lòng nhập tên!' }]}
                        >
                            <Input prefix={<UserOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="Nhập tên của bạn" />
                        </Form.Item>

                        <Row gutter={20}>
                            <Col span={12}>
                                <Form.Item label="Tuổi" name="age">
                                    <InputNumber 
                                        min={1} 
                                        max={100} 
                                        style={{ width: '100%' }} 
                                        placeholder="Nhập tuổi" 
                                    />
                                </Form.Item>
                            </Col>
                            <Col span={12}>
                                <Form.Item label="Giới tính" name="gender">
                                    <Select placeholder="Chọn giới tính">
                                        <Select.Option value="MALE"><ManOutlined /> Nam</Select.Option>
                                        <Select.Option value="FEMALE"><WomanOutlined /> Nữ</Select.Option>
                                        <Select.Option value="OTHER">Khác</Select.Option>
                                    </Select>
                                </Form.Item>
                            </Col>
                        </Row>

                        <Form.Item label="Địa chỉ" name="address">
                            <Input prefix={<EnvironmentOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="Nhập địa chỉ sinh sống" />
                        </Form.Item>

                        <Form.Item style={{ marginTop: 20 }}>
                            <Button type="primary" htmlType="submit" loading={isSubmit} block>
                                Lưu thay đổi
                            </Button>
                        </Form.Item>
                    </Form>
                </Col>
            </Row>
        </div>
    );
};

export default UserInfo;