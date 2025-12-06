import React from 'react';
import { Row, Col, Button, Card, Anchor } from 'antd';
import { DownloadOutlined, ArrowRightOutlined } from '@ant-design/icons';
import { 
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, 
    PieChart, Pie, Cell 
} from 'recharts';
import styles from '@/styles/client.blog.module.scss';

const BlogPage = () => {
    
    // Dữ liệu biểu đồ lương theo ngôn ngữ (Triệu VND)
    const salaryData = [
        { name: 'Java', Fresher: 12, Junior: 22, Senior: 45, Lead: 65 },
        { name: 'NodeJS', Fresher: 11, Junior: 20, Senior: 42, Lead: 60 },
        { name: 'ReactJS', Fresher: 10, Junior: 18, Senior: 38, Lead: 55 },
        { name: '.NET', Fresher: 11, Junior: 19, Senior: 40, Lead: 58 },
        { name: 'Python', Fresher: 13, Junior: 24, Senior: 48, Lead: 70 },
    ];

    // Dữ liệu biểu đồ tròn (Nhu cầu tuyển dụng theo mảng)
    const demandData = [
        { name: 'Backend', value: 35 },
        { name: 'Frontend', value: 25 },
        { name: 'Fullstack', value: 20 },
        { name: 'Mobile', value: 10 },
        { name: 'QA/QC', value: 10 },
    ];

    const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8'];

    return (
        <div className={styles['blog-container']}>
            
            {/* HERO SECTION */}
            <div className={styles['hero-section']}>
                <div className={styles['hero-content']}>
                    <span className={styles['tag']}>Báo cáo thị trường 2024-2025</span>
                    <h1>Báo cáo Lương & <br/> Thị trường <span>IT Việt Nam</span></h1>
                    <p>Khám phá bức tranh toàn cảnh về mức lương, xu hướng tuyển dụng và <br/> những kỹ năng được săn đón nhất trong ngành công nghệ.</p>
                    <button className={styles['download-btn']}>
                        <DownloadOutlined /> Tải Báo Cáo Đầy Đủ (PDF)
                    </button>
                </div>
            </div>
            <div className={styles['content-wrapper']}>
                
                {/* MAIN CONTENT */}
                <div className={styles['main-content']}>
                    
                    <div id="highlight" className={styles['highlights-grid']}>
                        <div className={styles['highlight-card']}>
                            <div className={styles['number']}>20-30%</div>
                            <div className={styles['label']}>Mức tăng lương trung bình khi nhảy việc</div>
                        </div>
                        <div className={styles['highlight-card']}>
                            <div className={styles['number']}>Java</div>
                            <div className={styles['label']}>Ngôn ngữ được trả lương cao nhất</div>
                        </div>
                        <div className={styles['highlight-card']}>
                            <div className={styles['number']}>AI/ML</div>
                            <div className={styles['label']}>Lĩnh vực có tốc độ tăng trưởng số 1</div>
                        </div>
                    </div>

                    <section id="salary-trend">
                        <h2>1. Xu hướng Lương IT theo Ngôn ngữ</h2>
                        <p>Theo khảo sát từ hơn 2,000 lập trình viên và 500 nhà tuyển dụng, mức lương ngành IT tại Việt Nam vẫn giữ đà tăng trưởng ổn định bất chấp những biến động kinh tế. <strong>Java</strong> và <strong>Python</strong> tiếp tục dẫn đầu bảng xếp hạng về thu nhập, đặc biệt ở các vị trí Senior và Lead.</p>       
                        <div className={styles['chart-container']}>
                            <h3>Mức lương trung bình theo Ngôn ngữ & Cấp bậc (Triệu VND)</h3>
                            <ResponsiveContainer width="100%" height={400}>
                                <BarChart data={salaryData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="Fresher" fill="#8884d8" />
                                    <Bar dataKey="Junior" fill="#82ca9d" />
                                    <Bar dataKey="Senior" fill="#ffc658" />
                                    <Bar dataKey="Lead" fill="#ea1e30" />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </section>
                    <section id="recruitment-demand">
                        <h2>2. Nhu cầu Tuyển dụng theo Mảng</h2>
                        <p>Năm nay chứng kiến sự bùng nổ nhu cầu tuyển dụng các vị trí liên quan đến <strong>Backend</strong> và <strong>Fullstack</strong>. Các doanh nghiệp đang ưu tiên tìm kiếm nhân sự đa năng, có khả năng xử lý cả hai phía Client và Server để tối ưu hóa nguồn lực.</p>  
                        <div className={styles['chart-container']}>
                            <h3>Tỷ trọng nhu cầu tuyển dụng 2024</h3>
                            <ResponsiveContainer width="100%" height={350}>
                                <PieChart>
                                    <Pie
                                        data={demandData}
                                        cx="50%"
                                        cy="50%"
                                        labelLine={false}
                                        label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                        outerRadius={120}
                                        fill="#8884d8"
                                        dataKey="value"
                                    >
                                        {demandData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                    </section>
                    <section id="conclusion">
                        <h2>3. Kết luận & Dự báo</h2>
                        <p>Thị trường IT Việt Nam đang bước vào giai đoạn "thanh lọc" mạnh mẽ. Các lập trình viên không chỉ cần giỏi kỹ năng chuyên môn (Hard skills) mà còn phải trau dồi kỹ năng mềm (Soft skills) và khả năng ngoại ngữ để cạnh tranh.</p>
                        <p>Dự báo trong năm tới, các vị trí liên quan đến <strong>AI, Cloud Computing (AWS/Azure)</strong> và <strong>Cyber Security</strong> sẽ có mức tăng trưởng lương đột biến.</p>
                    </section>
                </div>
                {/* SIDEBAR - MỤC LỤC */}
                <div className={styles['sidebar']}>
                    <div className={styles['toc-card']}>
                        <h4>Mục lục</h4>
                        <Anchor
                            targetOffset={80}
                            items={[
                                { key: 'highlight', href: '#highlight', title: 'Điểm nhấn báo cáo' },
                                { key: 'salary-trend', href: '#salary-trend', title: '1. Xu hướng Lương IT' },
                                { key: 'recruitment-demand', href: '#recruitment-demand', title: '2. Nhu cầu Tuyển dụng' },
                                { key: 'conclusion', href: '#conclusion', title: '3. Kết luận & Dự báo' },
                            ]}
                        />
                    </div>
                </div>

            </div>
        </div>
    );
};


export default BlogPage;