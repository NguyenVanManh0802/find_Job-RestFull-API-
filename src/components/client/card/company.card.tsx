import { callFetchCompany } from '@/config/api';
import { convertSlug } from '@/config/utils';
import { ICompany } from '@/types/backend';
import { Card, Col, Divider, Empty, Pagination, Row, Spin } from 'antd';
import { useState, useEffect } from 'react';
import { isMobile } from 'react-device-detect';
import { Link, useNavigate } from 'react-router-dom';
import styles from '@/styles/client.company.module.scss'; // Import file SCSS mới
import { EnvironmentOutlined, TeamOutlined } from '@ant-design/icons';

interface IProps {
    showPagination?: boolean;
}

const CompanyCard = (props: IProps) => {
    const { showPagination = false } = props;

    const [displayCompany, setDisplayCompany] = useState<ICompany[] | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(8); // Tăng số lượng hiển thị cho đẹp
    const [total, setTotal] = useState(0);
    const [filter, setFilter] = useState("");
    const [sortQuery, setSortQuery] = useState("sort=updatedAt,desc");
    const navigate = useNavigate();

    useEffect(() => {
        fetchCompany();
    }, [current, pageSize, filter, sortQuery]);

    const fetchCompany = async () => {
        setIsLoading(true)
        let query = `page=${current}&size=${pageSize}`;
        if (filter) query += `&${filter}`;
        if (sortQuery) query += `&${sortQuery}`;

        const res = await callFetchCompany(query);
        if (res && res.data) {
            setDisplayCompany(res.data.result);
            setTotal(res.data.meta.total)
        }
        setIsLoading(false)
    }

    const handleOnchangePage = (pagination: { current: number, pageSize: number }) => {
        if (pagination && pagination.current !== current) setCurrent(pagination.current)
        if (pagination && pagination.pageSize !== pageSize) {
            setPageSize(pagination.pageSize)
            setCurrent(1);
        }
    }

    const handleViewDetailJob = (item: ICompany) => {
        if (item.name) {
            const slug = convertSlug(item.name);
            navigate(`/company/${slug}?id=${item.id}`)
        }
    }

    return (
        <div className={styles["company-section"]}>
            <div className={styles["company-content"]}>
                <div className={styles["header-title"]}>
                    <span className={styles["title"]}>Nhà Tuyển Dụng Hàng Đầu</span>
                    {!showPagination && <Link to="/company">Xem tất cả</Link>}
                </div>

                <Spin spinning={isLoading} tip="Đang tải...">
                    <Row gutter={[24, 24]}>
                        {displayCompany?.map(item => (
                            <Col xs={24} sm={12} md={8} lg={6} key={item.id}>
                                <div 
                                    className={styles["company-card"]} 
                                    onClick={() => handleViewDetailJob(item)}
                                >
                                    <div className={styles["card-body"]}>
                                        {/* LOGO */}
                                        <div className={styles["card-logo"]}>
                                            <img
                                                alt={item.name}
                                                src={`${import.meta.env.VITE_BACKEND_URL}/storage/company/${item?.logo}`}
                                                onError={(e) => {
                                                    e.currentTarget.src = '/images/company-default.png'; // Ảnh mặc định nếu lỗi
                                                }}
                                            />
                                        </div>
                                        
                                        {/* NAME */}
                                        <div className={styles["company-name"]} title={item.name}>
                                            {item.name}
                                        </div>

                                        {/* ADDRESS (Optional) */}
                                        <div className={styles["company-address"]}>
                                            <EnvironmentOutlined style={{ marginRight: 5 }} />
                                            {item.address || "Vietnam"}
                                        </div>

                                        {/* FOOTER INFO */}
                                        <div className={styles["footer-card"]}>
                                            Xem chi tiết &rarr;
                                        </div>
                                    </div>
                                </div>
                            </Col>
                        ))}

                        {(!displayCompany || displayCompany.length === 0) && !isLoading && (
                            <Col span={24}>
                                <Empty description="Chưa có công ty nào" />
                            </Col>
                        )}
                    </Row>

                    {showPagination && (
                        <div style={{ marginTop: 40, textAlign: 'center' }}>
                            <Pagination
                                current={current}
                                total={total}
                                pageSize={pageSize}
                                responsive
                                onChange={(p, s) => handleOnchangePage({ current: p, pageSize: s })}
                            />
                        </div>
                    )}
                </Spin>
            </div>
        </div>
    )
}

export default CompanyCard;