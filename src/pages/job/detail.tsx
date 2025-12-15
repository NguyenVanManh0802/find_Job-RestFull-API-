import { useLocation, useNavigate } from "react-router-dom";
import { useState, useEffect } from 'react';
import { IJob } from "@/types/backend";
import { callFetchJobById } from "@/config/api";
import styles from '@/styles/client.job.detail.module.scss';
import parse from 'html-react-parser';
import { Col, Row, Skeleton, Button, Tooltip } from "antd"; // Thêm Tooltip
import { DollarOutlined, EnvironmentOutlined, ClockCircleOutlined, CheckCircleOutlined } from "@ant-design/icons";
import { getLocationName } from "@/config/utils";
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import ApplyModal from "@/components/client/modal/apply.modal";
import { useAppSelector } from "@/redux/hooks"; // Import hook Redux

dayjs.extend(relativeTime)

const ClientJobDetailPage = (props: any) => {
    const [jobDetail, setJobDetail] = useState<IJob | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

    let location = useLocation();
    let params = new URLSearchParams(location.search);
    const id = params?.get("id"); 

    // 1. Lấy thông tin user từ Redux
    const user = useAppSelector(state => state.account.user);
    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated);
    // 2. Logic kiểm tra quyền (Admin hoặc HR thì true)
    // Điều kiện: Role là SUPER_ADMIN HOẶC có thông tin company (tức là HR)
    const isAdminOrHR = user?.role?.name === 'SUPER_ADMIN' || user?.role?.name === 'HUMAN_RESOURCE';
    useEffect(() => {
        const init = async () => {
            if (id) {
                setIsLoading(true)
                const res = await callFetchJobById(id);
                if (res?.data) {
                    setJobDetail(res.data)
                }
                setIsLoading(false)
            }
        }
        init();
    }, [id]);

    return (
        <div className={styles["detail-job-section"]}>
            <div className={styles["container"]}>
                {isLoading ? (
                    <div style={{ padding: '50px' }}><Skeleton active avatar paragraph={{ rows: 6 }} /></div>
                ) : (
                    <Row gutter={[24, 24]}>
                        {jobDetail && jobDetail.id && (
                            <>
                                {/* CỘT TRÁI: CHI TIẾT CÔNG VIỆC */}
                                <Col span={24} md={16}>
                                    {/* 1. Header Job */}
                                    <div className={styles["header-job-card"]}>
                                        <h1 className={styles["job-title"]}>{jobDetail.name}</h1>
                                        
                                        <div className={styles["job-meta"]}>
                                            <div className={styles["salary"]}>
                                                <DollarOutlined />
                                                {(jobDetail.salary + "")?.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} đ
                                            </div>
                                            <div className={styles["location"]}>
                                                <EnvironmentOutlined />
                                                {getLocationName(jobDetail.location)}
                                            </div>
                                            <div className={styles["updated-at"]}>
                                                <ClockCircleOutlined />
                                                {dayjs(jobDetail.updatedAt || jobDetail.createdAt).fromNow()}
                                            </div>
                                        </div>

                                        <div className={styles["skills-wrapper"]}>
                                            {jobDetail?.skills?.map((item, index) => (
                                                <span key={index} className={styles["skill-tag"]}>
                                                    {item.name}
                                                </span>
                                            ))}
                                        </div>

                                        <div style={{ marginTop: 20 }}>
                                            {/* LOGIC HIỂN THỊ NÚT APPLY */}
                                            {isAuthenticated === false ? (
                                                // Chưa đăng nhập -> Hiện nút để đăng nhập
                                                <Button 
                                                    type="primary" 
                                                    size="large" 
                                                    className={styles["apply-btn"]}
                                                    onClick={() => setIsModalOpen(true)} // Modal sẽ handle việc bắt login
                                                >
                                                    Đăng nhập để Ứng tuyển
                                                </Button>
                                            ) : isAdminOrHR ? (
                                                // Đã đăng nhập nhưng là Admin/HR -> Disable nút
                                                <Tooltip title="Tài khoản Quản trị/Nhà tuyển dụng không thể ứng tuyển">
                                                    <Button 
                                                        type="primary" 
                                                        size="large" 
                                                        disabled 
                                                        style={{ background: '#ccc', borderColor: '#ccc', color: '#666', fontWeight: 600 }}
                                                    >
                                                        Không thể ứng tuyển
                                                    </Button>
                                                </Tooltip>
                                            ) : (
                                                // Là User bình thường -> Hiện nút Apply
                                                <Button 
                                                    type="primary" 
                                                    size="large" 
                                                    className={styles["apply-btn"]}
                                                    onClick={() => setIsModalOpen(true)}
                                                >
                                                    Ứng tuyển ngay
                                                </Button>
                                            )}
                                        </div>
                                    </div>

                                    {/* 2. Job Description Body */}
                                    <div className={styles["job-description-card"]}>
                                        <h3>3 Lý do để gia nhập công ty</h3>
                                        <ul style={{listStyle: 'none', padding: 0}}>
                                            <li><CheckCircleOutlined style={{color: 'green', marginRight: 8}}/>Môi trường làm việc quốc tế năng động</li>
                                            <li><CheckCircleOutlined style={{color: 'green', marginRight: 8}}/>Lương thưởng hấp dẫn, review 2 lần/năm</li>
                                            <li><CheckCircleOutlined style={{color: 'green', marginRight: 8}}/>Cơ hội onsite tại Nhật, Mỹ, Châu Âu</li>
                                        </ul>
                                        <br/>
                                        {parse(jobDetail.description ?? "")}
                                    </div>
                                </Col>

                                {/* CỘT PHẢI: THÔNG TIN CÔNG TY */}
                                <Col span={24} md={8}>
                                    <div className={styles["company-card"]}>
                                        <div className={styles["logo-wrapper"]}>
                                            <img
                                                alt="company logo"
                                                src={`${import.meta.env.VITE_BACKEND_URL}/storage/company/${jobDetail.company?.logo}`}
                                                onError={(e) => { e.currentTarget.src = '/images/company-default.png' }}
                                            />
                                        </div>
                                        <div className={styles["company-name"]}>
                                            {jobDetail.company?.name}
                                        </div>
                                        
                                        <div style={{marginTop: 15}}>
                                            <a href="#" className={styles["view-company-link"]}>Xem hồ sơ công ty &rarr;</a>
                                        </div>
                                    </div>
                                </Col>
                            </>
                        )}
                    </Row>
                )}
                <ApplyModal
                    isModalOpen={isModalOpen}
                    setIsModalOpen={setIsModalOpen}
                    jobDetail={jobDetail}
                />
            </div>
        </div>
    )
}

export default ClientJobDetailPage;