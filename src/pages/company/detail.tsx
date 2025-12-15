import { useLocation, useNavigate } from "react-router-dom";
import { useState, useEffect } from 'react';
import { ICompany } from "@/types/backend";
import { callFetchCompanyById, callCreateSubscriber } from "@/config/api";
import styles from '@/styles/client.company.module.scss';
import parse from 'html-react-parser';
import { Col, Row, Skeleton, Button, Tag, message, Tooltip } from "antd"; // Import thêm Tooltip
import { EnvironmentOutlined, TeamOutlined, GlobalOutlined, CheckCircleOutlined, PlusOutlined, MinusOutlined } from "@ant-design/icons";
import { useAppSelector } from "@/redux/hooks";

const ClientCompanyDetailPage = (props: any) => {
    const [companyDetail, setCompanyDetail] = useState<ICompany | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
    const [isFollowed, setIsFollowed] = useState<boolean>(false);

    const location = useLocation();
    const navigate = useNavigate();
    let params = new URLSearchParams(location.search);
    const id = params?.get("id"); 

    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated);
    
    // 1. Lấy thông tin user để check quyền
    const user = useAppSelector(state => state.account.user);
    // Biến này trả về true nếu user là Admin hoặc HR
    const isAdminOrHR = user?.role?.name === 'SUPER_ADMIN' || user?.role?.name === 'HUMAN_RESOURCE';

    useEffect(() => {
        const init = async () => {
            if (id) {
                setIsLoading(true)
                const res = await callFetchCompanyById(id);
                if (res?.data) {
                    setCompanyDetail(res.data);
                    if (res.data.isFollowed) {
                        setIsFollowed(true);
                    } else {
                        setIsFollowed(false);
                    }
                }
                setIsLoading(false)
            }
        }
        init();
    }, [id, isAuthenticated]);

    const handleFollowCompany = async () => {
        // 2. Chặn thêm 1 lớp logic cho chắc chắn
        if (isAdminOrHR) {
            message.error("Admin và HR không thể thực hiện chức năng này!");
            return;
        }

        if (!isAuthenticated) {
            message.error("Vui lòng đăng nhập để theo dõi công ty!");
            navigate(`/login?callback=${window.location.pathname}${window.location.search}`);
            return;
        }

        if (companyDetail?.id) {
            setIsSubmitting(true);
            const res = await callCreateSubscriber(companyDetail.id);
            
            if (res && res.data) {
                const newStatus = !isFollowed;
                setIsFollowed(newStatus);
                
                if (newStatus) {
                    message.success("Đã theo dõi công ty thành công!");
                } else {
                    message.success("Đã hủy theo dõi công ty!");
                }
            } else {
                message.warning(res.message || "Có lỗi xảy ra, vui lòng thử lại.");
            }
            setIsSubmitting(false);
        }
    }

    return (
        <div className={styles["company-detail-container"]}>
            {isLoading ? (
                <div style={{ padding: '50px 100px' }}><Skeleton active avatar paragraph={{ rows: 4 }} /></div>
            ) : (
                <>
                    <div className={styles["cover-wrapper"]}></div>

                    <div className={styles["container-content"]}>
                        {companyDetail && (
                            <div className={styles["header-card"]}>
                                <div className={styles["logo-wrapper"]}>
                                    <img
                                        alt="logo"
                                        src={`${import.meta.env.VITE_BACKEND_URL}/storage/company/${companyDetail?.logo}`}
                                    />
                                </div>
                                <div className={styles["company-info"]}>
                                    <h1 className={styles["title"]}>{companyDetail.name}</h1>
                                    
                                    <div className={styles["meta-info"]}>
                                        <div className={styles["item"]}>
                                            <EnvironmentOutlined className={styles["icon"]} />
                                            <span>{companyDetail.address}</span>
                                        </div>
                                        <div className={styles["item"]}>
                                            <TeamOutlined className={styles["icon"]} />
                                            <span>50-100 nhân viên</span>
                                        </div>
                                        <div className={styles["item"]}>
                                            <GlobalOutlined className={styles["icon"]} />
                                            <span>Vietnam</span>
                                        </div>
                                    </div>

                                    {/* --- 3. CẬP NHẬT NÚT BẤM (Disable + Tooltip) --- */}
                                    <Tooltip title={isAdminOrHR ? "Tài khoản quản trị không thể theo dõi" : ""}>
                                        <Button 
                                            type={isFollowed ? "default" : "primary"} 
                                            danger={!isFollowed}
                                            icon={isFollowed ? <MinusOutlined /> : <PlusOutlined />} 
                                            
                                            loading={isSubmitting}
                                            className={styles["follow-btn"]}
                                            onClick={handleFollowCompany}
                                            
                                            // Disable nút nếu là Admin hoặc HR
                                            disabled={isAdminOrHR}
                                            
                                            style={{ marginTop: 20, minWidth: 150, fontWeight: 600 }}
                                        >
                                            {isFollowed ? "Hủy theo dõi" : "Theo dõi công ty"}
                                        </Button>
                                    </Tooltip>
                                    {/* ----------------------------------------------- */}
                                </div>
                            </div>
                        )}

                        <div className={styles["body-content"]}>
                            <div className={styles["main-column"]}>
                                <div className={styles["section-card"]}>
                                    <h2>Giới thiệu công ty</h2>
                                    <div className={styles["html-content"]}>
                                        {parse(companyDetail?.description ?? "")}
                                    </div>
                                </div>

                                <div className={styles["section-card"]}>
                                    <h2>Tại sao bạn sẽ yêu thích làm việc tại đây?</h2>
                                    <div className={styles["html-content"]}>
                                        <ul>
                                            <li><CheckCircleOutlined style={{color:'green', marginRight:8}}/> Môi trường làm việc năng động, sáng tạo</li>
                                            <li><CheckCircleOutlined style={{color:'green', marginRight:8}}/> Cơ hội thăng tiến rõ ràng</li>
                                            <li><CheckCircleOutlined style={{color:'green', marginRight:8}}/> Chế độ đãi ngộ hấp dẫn, thưởng dự án</li>
                                        </ul>
                                    </div>
                                </div>
                            </div>

                            <div className={styles["side-column"]}>
                                <div className={styles["info-box"]}>
                                    <h3>Thông tin liên hệ</h3>
                                    <div className={styles["info-item"]}>
                                        <EnvironmentOutlined className={styles["icon"]} />
                                        <span>{companyDetail?.address}</span>
                                    </div>
                                    <div className={styles["info-item"]}>
                                        <GlobalOutlined className={styles["icon"]} />
                                        <a href="#" target="_blank" rel="noreferrer">Website công ty</a>
                                    </div>
                                </div>

                                <div className={styles["info-box"]}>
                                    <h3>Địa điểm</h3>
                                    <iframe 
                                        src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.4946681007846!2d106.69896731458902!3d10.773374292323565!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752f40a3b49e59%3A0xa1bd14e483a602db!2sBen%20Thanh%20Market!5e0!3m2!1sen!2s!4v1626079942721!5m2!1sen!2s"
                                        width="100%" 
                                        height="200" 
                                        style={{border:0, borderRadius: 8}} 
                                        allowFullScreen 
                                        loading="lazy" 
                                        title="map"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}
export default ClientCompanyDetailPage;