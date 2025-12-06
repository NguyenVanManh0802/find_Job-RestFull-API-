import { Button, Col, Form, Row, Select } from 'antd';
import { EnvironmentOutlined, MonitorOutlined, SearchOutlined } from '@ant-design/icons';
import { LOCATION_LIST } from '@/config/utils';
import { useEffect, useState } from 'react';
import { callFetchAllSkill } from '@/config/api';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import styles from '@/styles/client.module.scss'; // Nhớ import file style

const SearchClient = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [form] = Form.useForm();
    const [searchParams] = useSearchParams();

    const optionsLocations = LOCATION_LIST;
    const [optionsSkills, setOptionsSkills] = useState<{ label: string; value: string }[]>([]);

    // 1. Đồng bộ dữ liệu từ URL vào Form khi reload trang
    useEffect(() => {
        const queryLocation = searchParams.get("location");
        const querySkills = searchParams.get("skills");

        if (queryLocation) {
            form.setFieldValue("location", queryLocation.split(","));
        }
        if (querySkills) {
            form.setFieldValue("skills", querySkills.split(","));
        }
    }, [searchParams, form]);

    // 2. Fetch danh sách kỹ năng (FIX LỖI NO DATA TẠI ĐÂY)
    useEffect(() => {
        const fetchSkill = async () => {
            // Dùng page=0 & size lớn để lấy hết skill hiển thị
            const query = `page=0&size=100&sort=createdAt,desc`;
            
            try {
                const res = await callFetchAllSkill(query);
                
                // LOGIC FIX: Kiểm tra cả 2 trường hợp cấu trúc response
                // Trường hợp 1: Có Interceptor (dữ liệu nằm trong res.result)
              if (res && (res as any).result) {
                // Sửa dòng map: Ép kiểu res thành any để lấy .result
                const arr = (res as any).result.map((item: any) => ({
                    label: item.name,
                    value: item.name
                }));
                
    setOptionsSkills(arr);
}
                // Trường hợp 2: Không có Interceptor (dữ liệu nằm trong res.data.result)
                else if (res && res.data && res.data.result) {
                    const arr = res.data.result.map((item: any) => ({
                        label: item.name,
                        value: item.name
                    }));
                    setOptionsSkills(arr);
                }
            } catch (error) {
                console.error("Lỗi tải kỹ năng:", error);
            }
        }
        fetchSkill();
    }, [])

    // 3. Xử lý Submit Search
    const onFinish = (values: any) => {
        const { skills, location } = values;
        const params = new URLSearchParams();

        if (skills && skills.length > 0) {
            params.append('skills', skills.join(','));
        }
        if (location && location.length > 0) {
            params.append('location', location.join(','));
        }

        navigate(`/job?${params.toString()}`);
    }

    return (
        <div className={styles['search-section']}>
            <div className={styles['search-content']}>
                <h2 className={styles['header-title']}>
                    Việc Làm IT Cho Developer <span>"Chất"</span>
                </h2>

                <div className={styles['search-form-wrapper']}>
                    <Form
                        form={form}
                        onFinish={onFinish}
                        layout="vertical"
                    >
                        <Row gutter={[16, 16]}>
                            {/* Input Kỹ năng */}
                            <Col xs={24} md={10}>
                                <Form.Item
                                    name="skills"
                                    style={{ margin: 0 }}
                                >
                                    <Select
                                        mode="multiple"
                                        allowClear
                                        showArrow={false}
                                        style={{ width: '100%' }}
                                        placeholder={
                                            <div style={{ color: '#8c8c8c' }}>
                                                <MonitorOutlined style={{ marginRight: 8 }} />
                                                Tìm theo kỹ năng...
                                            </div>
                                        }
                                        optionLabelProp="label"
                                        options={optionsSkills}
                                        // Thêm bộ lọc để gõ chữ là tìm thấy
                                        filterOption={(input, option) =>
                                            (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                                        }
                                        maxTagCount="responsive"
                                    />
                                </Form.Item>
                            </Col>

                            {/* Input Địa điểm */}
                            <Col xs={24} md={10}>
                                <Form.Item
                                    name="location"
                                    style={{ margin: 0 }}
                                >
                                    <Select
                                        mode="multiple"
                                        allowClear
                                        showArrow={false}
                                        style={{ width: '100%' }}
                                        placeholder={
                                            <div style={{ color: '#8c8c8c' }}>
                                                <EnvironmentOutlined style={{ marginRight: 8 }} />
                                                Địa điểm...
                                            </div>
                                        }
                                        optionLabelProp="label"
                                        options={optionsLocations}
                                        maxTagCount="responsive"
                                    />
                                </Form.Item>
                            </Col>

                            {/* Nút Tìm kiếm */}
                            <Col xs={24} md={4}>
                                <Button
                                    type='primary'
                                    htmlType="submit"
                                    className={styles['btn-search']}
                                    icon={<SearchOutlined />}
                                >
                                    Search
                                </Button>
                            </Col>
                        </Row>
                    </Form>
                </div>
            </div>
        </div>
    )
}

export default SearchClient;