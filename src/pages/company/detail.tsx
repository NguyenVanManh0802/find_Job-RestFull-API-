import { useLocation, useNavigate } from "react-router-dom";
import { useState, useEffect } from 'react';
import { ICompany } from "@/types/backend";
import { callFetchCompanyById } from "@/config/api";
import styles from '@/styles/client.company.module.scss'; // Import style mới
import parse from 'html-react-parser';
import { Col, Row, Skeleton, Button, Tag } from "antd";
import { EnvironmentOutlined, TeamOutlined, GlobalOutlined, CheckCircleOutlined } from "@ant-design/icons";

const ClientCompanyDetailPage = (props: any) => {
    const [companyDetail, setCompanyDetail] = useState<ICompany | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    let location = useLocation();
    let params = new URLSearchParams(location.search);
    const id = params?.get("id"); 

    useEffect(() => {
        const init = async () => {
            if (id) {
                setIsLoading(true)
                const res = await callFetchCompanyById(id);
                if (res?.data) {
                    setCompanyDetail(res.data)
                }
                setIsLoading(false)
            }
        }
        init();
    }, [id]);

    return (
        <div className={styles["company-detail-container"]}>
            {isLoading ? (
                <div style={{ padding: '50px 100px' }}><Skeleton active avatar paragraph={{ rows: 4 }} /></div>
            ) : (
                <>
                    {/* 1. COVER IMAGE (Gradient nền) */}
                    <div className={styles["cover-wrapper"]}></div>

                    <div className={styles["container-content"]}>
                        {/* 2. HEADER CARD: Logo + Tên */}
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
                                            <span>50-100 nhân viên</span> {/* Dữ liệu giả lập nếu DB chưa có */}
                                        </div>
                                        <div className={styles["item"]}>
                                            <GlobalOutlined className={styles["icon"]} />
                                            <span>Vietnam</span>
                                        </div>
                                    </div>

                                    <Button className={styles["follow-btn"]}>Theo dõi công ty</Button>
                                </div>
                            </div>
                        )}

                        {/* 3. BODY CONTENT: 2 Cột */}
                        <div className={styles["body-content"]}>
                            {/* CỘT TRÁI: Giới thiệu chi tiết */}
                            <div className={styles["main-column"]}>
                                <div className={styles["section-card"]}>
                                    <h2>Giới thiệu công ty</h2>
                                    <div className={styles["html-content"]}>
                                        {parse(companyDetail?.description ?? "")}
                                    </div>
                                </div>

                                {/* Có thể thêm phần "Tại sao bạn thích làm việc ở đây" nếu có dữ liệu */}
                                <div className={styles["section-card"]}>
                                    <h2>Tại sao bạn sẽ yêu thích làm việc tại đây?</h2>
                                    <div className={styles["html-content"]}>
                                        <ul>
                                            <li><CheckCircleOutlined style={{color:'green', marginRight:8}}/> Môi trường làm việc năng động, sáng tạo</li>
                                            <li><CheckCircleOutlined style={{color:'green', marginRight:8}}/> Cơ hội thăng tiến rõ ràng</li>
                                            <li><CheckCircleOutlined style={{color:'green', marginRight:8}}/> Chế độ đãi ngộ hấp dẫn, thưởng dự án</li>
                                            {/* Nội dung này nên lấy từ DB nếu có */}
                                        </ul>
                                    </div>
                                </div>
                            </div>

                            {/* CỘT PHẢI: Thông tin liên hệ / Map */}
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
                                    {/* Google Map iframe (Demo) */}
                                    <iframe 
                                        src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.4946681007846!2d106.61953031533454!3d10.773374292323754!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752c28714f4e43%3A0x596812457e647b5e!2zNTIgw5B0IFThu4tjaCwgUGjGsOG7nW5nIDQsIFTDom4gQsOsbmgsIFRow6BuaCBwaOG7kSBI4buTIENow60gTWluaA!5e0!3m2!1svi!2s!4v1653278567854!5m2!1svi!2s" 
                                        width="100%" 
                                        height="200" 
                                        style={{border:0, borderRadius: 8}} 
                                        allowFullScreen 
                                        loading="lazy" 
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