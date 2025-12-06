import { Button, Col, Row, Divider } from 'antd';
import styles from '@/styles/client.home.module.scss'; // Import style mới
import SearchClient from '@/components/client/search.client';
import JobCard from '@/components/client/card/job.card';
import CompanyCard from '@/components/client/card/company.card';
import { 
    CodeOutlined, DatabaseOutlined, MobileOutlined, 
    SafetyCertificateOutlined, RocketOutlined, TeamOutlined, 
    CheckCircleOutlined, ArrowRightOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const HomePage = () => {
    const navigate = useNavigate();

    // Dữ liệu cứng cho Categories (Làm đẹp giao diện)
    const categories = [
        { name: 'Lập trình Frontend', count: '1.2k jobs', icon: <CodeOutlined /> },
        { name: 'Lập trình Backend', count: '850 jobs', icon: <DatabaseOutlined /> },
        { name: 'Lập trình Mobile', count: '540 jobs', icon: <MobileOutlined /> },
        { name: 'Bảo mật & Mạng', count: '320 jobs', icon: <SafetyCertificateOutlined /> },
    ];

    return (
        <div className={styles['home-container']}>
            
            {/* 1. HERO SEARCH SECTION */}
            <div className={styles['hero-section']}>
                <SearchClient />
            </div>

            <div className={styles['container']}>
                
                {/* 2. FEATURED CATEGORIES */}
                <div className={styles['categories-section']}>
                    <div className={styles['section-header']}>
                        <h2>Khám Phá <span>Cơ Hội</span> Theo Ngành Nghề</h2>
                        <p>Tìm kiếm công việc mơ ước của bạn theo các lĩnh vực hot nhất hiện nay</p>
                    </div>
                    <Row gutter={[24, 24]}>
                        {categories.map((item, index) => (
                            <Col xs={24} sm={12} md={6} key={index}>
                                <div className={styles['category-card']} onClick={() => navigate('/job')}>
                                    <div className={styles['icon']}>{item.icon}</div>
                                    <h3>{item.name}</h3>
                                    <p>{item.count}</p>
                                </div>
                            </Col>
                        ))}
                    </Row>
                </div>

                {/* 3. TOP EMPLOYERS */}
                <div className={styles['section-header']}>
                    <h2>Nhà Tuyển Dụng <span>Hàng Đầu</span></h2>
                    <p>Làm việc tại các công ty công nghệ tốt nhất Việt Nam</p>
                </div>
                <CompanyCard />

                <div style={{height: 60}}></div>

                {/* 4. LATEST JOBS */}
                <div className={styles['section-header']}>
                    <h2>Việc Làm <span>Mới Nhất</span></h2>
                    <p>Đừng bỏ lỡ các cơ hội việc làm hấp dẫn vừa được cập nhật</p>
                </div>
                <JobCard />

            </div>

            {/* 5. WHY CHOOSE US */}
            <div className={styles['features-section']}>
                <div className={styles['container']}>
                    <div className={styles['section-header']}>
                        <h2>Tại Sao Chọn <span>FindJob</span>?</h2>
                    </div>
                    <Row gutter={[30, 30]}>
                        <Col xs={24} md={8}>
                            <div className={styles['feature-item']}>
                                <div className={styles['feature-icon']}><RocketOutlined /></div>
                                <h3>Cơ hội việc làm tốt nhất</h3>
                                <p>Hàng ngàn công việc IT chất lượng cao được cập nhật mỗi ngày từ các công ty hàng đầu.</p>
                            </div>
                        </Col>
                        <Col xs={24} md={8}>
                            <div className={styles['feature-item']}>
                                <div className={styles['feature-icon']}><TeamOutlined /></div>
                                <h3>Kết nối trực tiếp</h3>
                                <p>Kết nối ứng viên trực tiếp với nhà tuyển dụng mà không qua trung gian.</p>
                            </div>
                        </Col>
                        <Col xs={24} md={8}>
                            <div className={styles['feature-item']}>
                                <div className={styles['feature-icon']}><CheckCircleOutlined /></div>
                                <h3>Hồ sơ chuyên nghiệp</h3>
                                <p>Tạo CV ấn tượng và quản lý hồ sơ ứng tuyển một cách dễ dàng và hiệu quả.</p>
                            </div>
                        </Col>
                    </Row>
                </div>
            </div>

            {/* 6. CALL TO ACTION */}
            <div className={styles['container']}>
                <div className={styles['cta-section']}>
                    <h2>Sẵn Sàng Cho Bước Tiến Sự Nghiệp Mới?</h2>
                    <p>Tạo hồ sơ ngay hôm nay và để các nhà tuyển dụng hàng đầu tìm đến bạn.</p>
                    <Button 
                        type="primary" 
                        size="large" 
                        className={styles['cta-btn']}
                        onClick={() => navigate('/register')}
                    >
                        Đăng Ký Ngay <ArrowRightOutlined />
                    </Button>
                </div>
            </div>
        </div>
    )
}

export default HomePage;