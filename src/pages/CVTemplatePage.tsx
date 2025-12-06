import React, { useState } from 'react';
import { Button, Modal, Tooltip, message, Tag } from 'antd';
import { EyeOutlined, DownloadOutlined, CheckCircleFilled, StarFilled } from '@ant-design/icons';
import styles from '@/styles/client.cv.module.scss';

interface ICVTemplate {
    id: number;
    title: string;
    type: string; // Frontend, Backend, Fullstack...
    image: string;
    isPremium?: boolean;
}

const CVTemplatePage = () => {
    const [activeFilter, setActiveFilter] = useState('All');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedCV, setSelectedCV] = useState<ICVTemplate | null>(null);
    const [downloading, setDownloading] = useState(false);

    // Dữ liệu mẫu (Bạn có thể thay link ảnh thật)
    const cvList: ICVTemplate[] = [
              { 
            id: 5, 
            title: "Product Manager Pro", 
            type: "Manager", 
            image: "https://marketplace.canva.com/EAFRuCp3DcY/1/0/1131w/canva-black-white-minimalist-cv-resume-f5JNR-K5jjw.jpg",
            isPremium: true 
        },
    { 
            id: 4, 
            title: "Modern Frontend Dev", 
            type: "Frontend", 
            // Ảnh mẫu từ Novoresume (Link public)
            image: "https://d.novoresume.com/images/doc/functional-resume-template.png",
            isPremium: true 
        },
    { 
            id: 1, 
            title: "Modern Minimalist (Frontend)", 
            type: "Frontend", 
            image: "https://d.novoresume.com/images/doc/minimalist-resume-template.png",
            isPremium: true 
        },
{ 
            id: 3, 
            title: "Professional Corporate (Manager)", 
            type: "Manager", 
            image: "https://d.novoresume.com/images/doc/professional-resume-template.png" 
        },
     { 
            id: 2, 
            title: "Fresher / Intern Simple", 
            type: "Fresher", 
            // Ảnh mẫu Simple
          image: "https://d.novoresume.com/images/doc/creative-resume-template.png",
            isPremium: true
        },
  
       { 
            id: 6, 
            title: "QA / Tester Detailed", 
            type: "Tester", 
            // Ảnh mẫu Tester
            image: "https://d.novoresume.com/images/doc/simple-resume-template.png" 
        },
      
    ];

    const filters = ['All', 'Frontend', 'Backend', 'Fullstack', 'Fresher', 'Manager', 'Tester'];

    const filteredCVs = activeFilter === 'All' 
        ? cvList 
        : cvList.filter(cv => cv.type === activeFilter);

    const handlePreview = (cv: ICVTemplate) => {
        setSelectedCV(cv);
        setIsModalOpen(true);
    };

    const handleDownload = () => {
        setDownloading(true);
        // Giả lập quá trình tải
        setTimeout(() => {
            message.success("Tải xuống thành công! File đã được lưu.");
            setDownloading(false);
            setIsModalOpen(false);
        }, 1500);
    }

    return (
        <div className={styles['cv-page-container']}>
            
            {/* HERO SECTION */}
            <div className={styles['hero-section']}>
                <div className={styles['container']}>
                    <h1>Thư Viện Mẫu <span>CV IT</span> Chuyên Nghiệp</h1>
                    <p>
                        Tăng 80% cơ hội được gọi phỏng vấn với các mẫu CV chuẩn ATS (Applicant Tracking System).
                        <br/>Thiết kế dành riêng cho Developer, Tester và PM.
                    </p>
                </div>
            </div>

            {/* MAIN CONTENT */}
            <div className={styles['container']}>
                
                {/* FILTER */}
                <div className={styles['filter-bar']}>
                    {filters.map(filter => (
                        <div 
                            key={filter}
                            className={`${styles['filter-btn']} ${activeFilter === filter ? styles['active'] : ''}`}
                            onClick={() => setActiveFilter(filter)}
                        >
                            {filter}
                        </div>
                    ))}
                </div>

                {/* LIST CV */}
                <div className={styles['cv-grid']}>
                    {filteredCVs.map(cv => (
                        <div key={cv.id} className={styles['cv-card']}>
                            <div className={styles['image-wrapper']}>
                                <img src={cv.image} alt={cv.title} className={styles['cv-image']} />
                                
                                {/* Overlay Actions */}
                                <div className={styles['card-overlay']}>
                                    <Button 
                                        type="primary" 
                                        shape="round" 
                                        size="large"
                                        icon={<EyeOutlined />}
                                        onClick={() => handlePreview(cv)}
                                        style={{ minWidth: 140 }}
                                    >
                                        Xem trước
                                    </Button>
                                    <Button 
                                        shape="round" 
                                        size="large"
                                        icon={<DownloadOutlined />}
                                        onClick={handleDownload}
                                        style={{ minWidth: 140 }}
                                    >
                                        Tải về
                                    </Button>
                                </div>

                                {/* Badge Premium nếu có */}
                                {cv.isPremium && (
                                    <div style={{ position: 'absolute', top: 10, right: 10 }}>
                                        <Tag color="gold" icon={<StarFilled />}>Premium</Tag>
                                    </div>
                                )}
                            </div>

                            <div className={styles['card-info']}>
                                <h3>{cv.title}</h3>
                                <p>
                                    <CheckCircleFilled style={{color: '#52c41a'}}/> 
                                    Chuẩn ATS • {cv.type}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* MODAL PREVIEW */}
            <Modal
                title={selectedCV?.title}
                open={isModalOpen}
                onCancel={() => setIsModalOpen(false)}
                footer={[
                    <Button key="back" onClick={() => setIsModalOpen(false)} size="large">
                        Đóng
                    </Button>,
                    <Button 
                        key="download" 
                        type="primary" 
                        icon={<DownloadOutlined />} 
                        loading={downloading}
                        onClick={handleDownload}
                        size="large"
                        style={{ backgroundColor: '#ea1e30', borderColor: '#ea1e30' }}
                    >
                        Tải mẫu này ngay
                    </Button>,
                ]}
                width={700}
                centered
                className="cv-preview-modal"
            >
                <div className={styles['preview-modal-content']}>
                    {selectedCV && <img src={selectedCV.image} alt="Preview" />}
                </div>
            </Modal>

        </div>
    );
};

export default CVTemplatePage;