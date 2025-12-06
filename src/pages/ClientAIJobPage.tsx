import { Row, Col, Tag, Button } from 'antd';
import { 
    DollarOutlined, 
    EnvironmentOutlined, 
    ClockCircleOutlined, 
    FireOutlined,
    RocketOutlined,
    RiseOutlined,
    BankOutlined
} from '@ant-design/icons';
import styles from '@/styles/client.ai.module.scss';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
dayjs.extend(relativeTime);

const ClientAIJobPage = () => {
    const navigate = useNavigate();

    // --- FAKE DATA CHO AI & DATA ---
    const aiJobs = [
        {
            id: 1,
            name: "Senior AI/Machine Learning Engineer (Python, PyTorch)",
            company: "VinAI Research",
            logo: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRzX14K8q20gqCqYg2j4w-x5a5Z-j5b5c5d5A&s", // Ảnh mạng ví dụ
            salary: "3,000 - 5,000 USD",
            location: "Hà Nội",
            skills: ["Python", "PyTorch", "TensorFlow", "Computer Vision"],
            isHot: true,
            updatedAt: dayjs().subtract(2, 'hour')
        },
        {
            id: 2,
            name: "Data Scientist (Big Data, SQL)",
            company: "MoMo (M_Service)",
            logo: "https://upload.wikimedia.org/wikipedia/vi/f/fe/MoMo_Logo.png",
            salary: "Up to 3,500 USD",
            location: "Hồ Chí Minh",
            skills: ["SQL", "Spark", "Hadoop", "Tableau"],
            isHot: true,
            updatedAt: dayjs().subtract(1, 'day')
        },
        {
            id: 3,
            name: "AI Research Scientist (NLP, LLM)",
            company: "FPT Software",
            logo: "https://upload.wikimedia.org/wikipedia/commons/1/11/FPT_logo_2010.svg",
            salary: "Thỏa thuận",
            location: "Đà Nẵng",
            skills: ["NLP", "LLM", "Deep Learning", "Python"],
            isHot: false,
            updatedAt: dayjs().subtract(3, 'day')
        },
        {
            id: 4,
            name: "Data Engineer (ETL, Cloud)",
            company: "Techcombank",
            logo: "https://cdn.haitrieu.com/wp-content/uploads/2022/01/Logo-Techcombank.png",
            salary: "2,000 - 4,000 USD",
            location: "Hà Nội",
            skills: ["ETL", "AWS", "Azure", "Python"],
            isHot: false,
            updatedAt: dayjs().subtract(5, 'day')
        }
    ];

    return (
        <div className={styles['ai-page-container']}>
            {/* 1. HERO SECTION */}
            <div className={styles['hero-section']}>
                <div className={styles['container']}>
                    <h1>Bứt Phá Sự Nghiệp Cùng <br/> <span>AI & Data Science</span></h1>
                    <p>
                        Khám phá các cơ hội việc làm hấp dẫn nhất trong lĩnh vực Trí tuệ nhân tạo và Khoa học dữ liệu.
                        Đón đầu xu hướng công nghệ tương lai ngay hôm nay.
                    </p>
                    
                    <div className={styles['stats-row']}>
                        <div className={styles['stat-item']}>
                            <span className={styles['number']}>1,250+</span>
                            <span className={styles['label']}>Việc làm AI/Data</span>
                        </div>
                        <div className={styles['stat-item']}>
                            <span className={styles['number']}>$3,500</span>
                            <span className={styles['label']}>Mức lương trung bình</span>
                        </div>
                        <div className={styles['stat-item']}>
                            <span className={styles['number']}>Top 1</span>
                            <span className={styles['label']}>Xu hướng 2025</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* 2. MAIN CONTENT */}
            <div className={styles['content-container']}>
                <Row gutter={[30, 30]}>
                    {/* CỘT TRÁI: DANH SÁCH JOB */}
                    <Col xs={24} lg={16}>
                        <div style={{marginBottom: 20, fontWeight: 700, fontSize: 20, display: 'flex', alignItems: 'center', gap: 10}}>
                            <FireOutlined style={{color: 'orange'}}/> Việc làm tuyển gấp
                        </div>
                        
                        {aiJobs.map((job, index) => (
                            <div 
                                key={job.id} 
                                className={styles['ai-job-card']}
                                onClick={() => navigate(`/job/detail/${job.id}`)} // Link giả định
                            >
                                <div className={styles['card-header']}>
                                    <div className={styles['job-info']}>
                                        <img src={job.logo} alt="logo" className={styles['logo']} />
                                        <div className={styles['title-wrap']}>
                                            <div className={styles['title']}>{job.name}</div>
                                            <div className={styles['company']}>{job.company}</div>
                                        </div>
                                    </div>
                                    <div className={styles['tags-corner']}>
                                        {job.isHot && <Tag color="red">HOT</Tag>}
                                    </div>
                                </div>

                                <div className={styles['card-body']}>
                                    <div className={styles['salary']}>
                                        <DollarOutlined /> {job.salary}
                                    </div>
                                    <div className={styles['location']}>
                                        <EnvironmentOutlined /> {job.location}
                                    </div>
                                </div>

                                <div className={styles['card-footer']}>
                                    <div className={styles['skills']}>
                                        {job.skills.map((skill, i) => (
                                            <span key={i} className={styles['skill-tag']}>{skill}</span>
                                        ))}
                                    </div>
                                    <div className={styles['time']}>
                                        <ClockCircleOutlined /> {dayjs(job.updatedAt).fromNow()}
                                    </div>
                                </div>
                            </div>
                        ))}

                        <div style={{textAlign: 'center', marginTop: 40}}>
                            <Button type="primary" size="large" onClick={() => navigate('/job')}>
                                Xem tất cả việc làm IT
                            </Button>
                        </div>
                    </Col>

                    {/* CỘT PHẢI: LÝ DO CHỌN AI */}
                    <Col xs={24} lg={8}>
                         <div className={styles['why-section']} style={{marginTop: 0}}>
                            <h3 style={{marginBottom: 20}}>Tại sao chọn AI & Data?</h3>
                            
                            <Row gutter={[0, 20]}>
                                <Col span={24}>
                                    <div className={styles['feature-card']}>
                                        <RocketOutlined className={styles['icon']} />
                                        <h3>Công nghệ tương lai</h3>
                                        <p>Dẫn đầu xu thế công nghệ với Generative AI, LLM và Big Data.</p>
                                    </div>
                                </Col>
                                <Col span={24}>
                                    <div className={styles['feature-card']}>
                                        <RiseOutlined className={styles['icon']} />
                                        <h3>Thu nhập đột phá</h3>
                                        <p>Mức lương trung bình cao hơn 30-50% so với mặt bằng chung ngành IT.</p>
                                    </div>
                                </Col>
                                <Col span={24}>
                                    <div className={styles['feature-card']}>
                                        <BankOutlined className={styles['icon']} />
                                        <h3>Cơ hội toàn cầu</h3>
                                        <p>Làm việc với các tập đoàn đa quốc gia và dự án quy mô lớn.</p>
                                    </div>
                                </Col>
                            </Row>
                         </div>
                    </Col>
                </Row>
            </div>
        </div>
    )
}

export default ClientAIJobPage;