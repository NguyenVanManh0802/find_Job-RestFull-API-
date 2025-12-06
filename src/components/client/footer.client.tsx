import styles from '@/styles/client.module.scss';
import { Row, Col } from 'antd';
import { FaFacebookF, FaLinkedinIn, FaYoutube } from 'react-icons/fa';
import { PhoneOutlined, MailOutlined, SendOutlined } from '@ant-design/icons';

const Footer = () => {
    return (
        <>
        <br />
        <br />
        <br />
        <br />
        <hr />
        <footer className={styles['footer-section']}>
            <div className={styles['container']}>
                <Row gutter={[30, 30]}>
                    {/* CỘT 1: LOGO & SOCIAL */}
                    <Col xs={24} sm={24} md={6} lg={5}>
                        <div className={styles['brand-wrapper']}>
                            <div className={styles['logo']}>
                                <span className={styles['circle']}>Find</span>
                                <span className={styles['text']}>Job</span>
                            </div>
                            <p className={styles['slogan']}>Ít nhưng mà chất</p>
                            <div className={styles['social-icons']}>
                                <a href="#" className={styles['icon']}><FaLinkedinIn /></a>
                                <a href="#" className={styles['icon']}><FaFacebookF /></a>
                                <a href="#" className={styles['icon']}><FaYoutube /></a>
                            </div>
                        </div>
                    </Col>

                    {/* CỘT 2: VỀ ITVIEC */}
                    <Col xs={24} sm={12} md={6} lg={4}>
                        <h3 className={styles['footer-heading']}>Về Find Job</h3>
                        <ul className={styles['footer-links']}>
                            <li><a href="#">Trang Chủ</a></li>
                            <li><a href="#">Về findjob.com</a></li>
                            <li><a href="#">Dịch vụ gợi ý ứng viên</a></li>
                            <li><a href="#">Liên Hệ</a></li>
                            <li><a href="#">Việc Làm IT</a></li>
                            <li><a href="#">Câu hỏi thường gặp</a></li>
                        </ul>
                    </Col>

                    {/* CỘT 3: CHƯƠNG TRÌNH */}
                    <Col xs={24} sm={12} md={6} lg={4}>
                        <h3 className={styles['footer-heading']}>Chương trình</h3>
                        <ul className={styles['footer-links']}>
                            <li><a href="#">Chuyện IT</a></li>
                            <li><a href="#">Cuộc thi viết</a></li>
                            <li><a href="#">Việc làm IT nổi bật</a></li>
                            <li><a href="#">Khảo sát thường niên</a></li>
                        </ul>
                    </Col>

                    {/* CỘT 4: ĐIỀU KHOẢN */}
                    <Col xs={24} sm={12} md={6} lg={4}>
                        <h3 className={styles['footer-heading']}>Điều khoản chung</h3>
                        <ul className={styles['footer-links']}>
                            <li><a href="#">Quy định bảo mật</a></li>
                            <li><a href="#">Quy chế hoạt động</a></li>
                            <li><a href="#">Giải quyết khiếu nại</a></li>
                            <li><a href="#">Thoả thuận sử dụng</a></li>
                            <li><a href="#">Thông cáo báo chí</a></li>
                        </ul>
                    </Col>

                    {/* CỘT 5: LIÊN HỆ */}
                    <Col xs={24} sm={12} md={12} lg={7}>
                        <h3 className={styles['footer-heading']}>Liên hệ để đăng tin tuyển dụng tại:</h3>
                        <ul className={styles['footer-contact']}>
                            <li>
                                <PhoneOutlined className={styles['contact-icon']} />
                                <span>Hồ Chí Minh: (+84) 938 083 469</span>
                            </li>
                            <li>
                                <PhoneOutlined className={styles['contact-icon']} />
                                <span>Hà Nội: (+84) 364 894 014</span>
                            </li>
                            <li>
                                <MailOutlined className={styles['contact-icon']} />
                                <span>Email: love@findjob.com</span>
                            </li>
                            <li>
                                <SendOutlined className={styles['contact-icon']} />
                                <span>Gửi thông tin liên hệ</span>
                            </li>
                        </ul>
                    </Col>
                </Row>

                {/* DÒNG KẺ NGANG & COPYRIGHT */}
                <div className={styles['footer-bottom']}>
                    <p>Copyright © FIND JOB JSC | MST: 0312192258</p>
                </div>
            </div>
            
        </footer>
        </>
    )
}

export default Footer;