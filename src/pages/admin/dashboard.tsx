import { Card, Col, Row, Statistic, Table, Tag, Avatar, Spin } from "antd";
import CountUp from 'react-countup';
import { 
    UserOutlined, 
    BankOutlined, 
    FileTextOutlined, 
    SolutionOutlined
} from "@ant-design/icons";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import styles from '@/styles/dashboard.module.scss';
import { useEffect, useState } from "react";
import { callFetchCompany, callFetchJob, callFetchResume, callFetchUser } from "@/config/api";
import dayjs from 'dayjs';

const DashboardPage = () => {
    // State lưu dữ liệu thống kê
    const [totalUsers, setTotalUsers] = useState(0);
    const [totalCompanies, setTotalCompanies] = useState(0);
    const [totalJobs, setTotalJobs] = useState(0);
    const [totalResumes, setTotalResumes] = useState(0);
    const [recentJobs, setRecentJobs] = useState<any[]>([]);
    const [chartData, setChartData] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const initDashboard = async () => {
            setLoading(true);
            
            // Gọi API song song để tối ưu
            const [resUser, resCompany, resJob, resResume] = await Promise.all([
                callFetchUser("page=1&size=1"),
                callFetchCompany("page=1&size=1"),
                callFetchJob("page=1&size=1000&sort=createdAt,desc"), // Lấy nhiều job để vẽ biểu đồ
                callFetchResume("page=1&size=1000&sort=createdAt,desc") // Lấy nhiều resume để vẽ biểu đồ
            ]);

            // 1. Cập nhật số liệu tổng quan
            if (resUser?.data?.meta) setTotalUsers(resUser.data.meta.total);
            if (resCompany?.data?.meta) setTotalCompanies(resCompany.data.meta.total);
            if (resJob?.data?.meta) setTotalJobs(resJob.data.meta.total);
            if (resResume?.data?.meta) setTotalResumes(resResume.data.meta.total);

            // 2. Xử lý danh sách Job mới nhất (trong 7 ngày)
            if (resJob?.data?.result) {
                const jobs = resJob.data.result;
                const sevenDaysAgo = dayjs().subtract(7, 'day');
                
                const newJobs = jobs.filter(item => dayjs(item.createdAt).isAfter(sevenDaysAgo))
                                    .slice(0, 5) // Lấy 5 job mới nhất
                                    .map((item: any, index) => ({
                                        key: item.id,
                                        job: item.name,
                                        company: item.company?.name,
                                        salary: `${(item.salary + "").replace(/\B(?=(\d{3})+(?!\d))/g, ',')} đ`,
                                        status: item.active ? 'ACTIVE' : 'INACTIVE',
                                        applicants: Math.floor(Math.random() * 10) // Giả lập số ứng viên vì API Job chưa trả về count resume
                                    }));
                setRecentJobs(newJobs);
            }

            // 3. Xử lý dữ liệu Biểu đồ (Chart) theo 6 tháng gần nhất
            if (resJob?.data?.result && resResume?.data?.result) {
                const monthlyData = processChartData(resJob.data.result, resResume.data.result);
                setChartData(monthlyData);
            }

            setLoading(false);
        }
        initDashboard();
    }, []);

    // Hàm xử lý dữ liệu biểu đồ
    const processChartData = (jobs: any[], resumes: any[]) => {
        const months = [];
        for (let i = 5; i >= 0; i--) {
            months.push(dayjs().subtract(i, 'month').format('MM/YYYY'));
        }

        return months.map(month => {
            const jobsCount = jobs.filter(j => dayjs(j.createdAt).format('MM/YYYY') === month).length;
            const cvCount = resumes.filter(r => dayjs(r.createdAt).format('MM/YYYY') === month).length;
            return {
                name: `T${month.split('/')[0]}`, // Hiển thị T1, T2...
                jobs: jobsCount,
                cv: cvCount
            }
        });
    }

    const stats = [
        { title: "Tổng người dùng", value: totalUsers, icon: <UserOutlined />, color: "gradient-purple" },
        { title: "Tổng công ty", value: totalCompanies, icon: <BankOutlined />, color: "gradient-blue" },
        { title: "Công việc đang tuyển", value: totalJobs, icon: <SolutionOutlined />, color: "gradient-orange" },
        { title: "Hồ sơ ứng tuyển", value: totalResumes, icon: <FileTextOutlined />, color: "gradient-green" },
    ];

    const columns = [
        {
            title: 'Tên công việc',
            dataIndex: 'job',
            key: 'job',
            render: (text: string) => <b style={{ color: '#1890ff' }}>{text}</b>
        },
        {
            title: 'Công ty',
            dataIndex: 'company',
            key: 'company',
        },
        {
            title: 'Mức lương',
            dataIndex: 'salary',
            key: 'salary',
            render: (text: string) => <span style={{ color: '#52c41a', fontWeight: 600 }}>{text}</span>
        },
        {
            title: 'Trạng thái',
            dataIndex: 'status',
            key: 'status',
            render: (status: string) => {
                let color = status === 'ACTIVE' ? 'green' : 'red';
                return <Tag color={color}>{status}</Tag>
            }
        },
        {
            title: 'Ứng viên', // Giả lập
            dataIndex: 'applicants',
            key: 'applicants',
            render: (val: number) => <div style={{ textAlign: 'center' }}><Avatar style={{ backgroundColor: '#fde3cf', color: '#f56a00' }}>{val}</Avatar></div>
        },
    ];

    const formatter = (value: number | string) => {
        return <CountUp end={Number(value)} separator="," duration={2.5} />;
    };

    return (
        <div className={styles['dashboard-container']}>
            <Spin spinning={loading} tip="Đang tải dữ liệu thống kê...">
                {/* --- SECTION 1: STATS CARDS --- */}
                <Row gutter={[24, 24]}>
                    {stats.map((item, index) => (
                        <Col xs={24} sm={12} md={6} key={index}>
                            <Card className={`${styles['stat-card']} ${styles[item.color]}`} bordered={false}>
                                <div className={styles['stat-icon-wrapper']}>{item.icon}</div>
                                <div className={styles['stat-content']}>
                                    <div className={styles['stat-title']}>{item.title}</div>
                                    <div className={styles['stat-value']}>
                                        {formatter(item.value)}
                                    </div>
                                </div>
                            </Card>
                        </Col>
                    ))}
                </Row>

                <div style={{ height: 24 }}></div>

                {/* --- SECTION 2: CHART & TABLE --- */}
                <Row gutter={[24, 24]}>
                    {/* Chart */}
                    <Col xs={24} lg={14}>
                        <Card title="Thống kê Tuyển dụng & Ứng tuyển (6 tháng gần nhất)" className={styles['chart-card']} bordered={false}>
                            <div style={{ width: '100%', height: 350 }}>
                                <ResponsiveContainer>
                                    <AreaChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                                        <defs>
                                            <linearGradient id="colorJobs" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8}/>
                                                <stop offset="95%" stopColor="#8884d8" stopOpacity={0}/>
                                            </linearGradient>
                                            <linearGradient id="colorCv" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="5%" stopColor="#82ca9d" stopOpacity={0.8}/>
                                                <stop offset="95%" stopColor="#82ca9d" stopOpacity={0}/>
                                            </linearGradient>
                                        </defs>
                                        <XAxis dataKey="name" />
                                        <YAxis />
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <Tooltip />
                                        <Area type="monotone" dataKey="jobs" stroke="#8884d8" fillOpacity={1} fill="url(#colorJobs)" name="Việc làm mới" />
                                        <Area type="monotone" dataKey="cv" stroke="#82ca9d" fillOpacity={1} fill="url(#colorCv)" name="CV Ứng tuyển" />
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </Card>
                    </Col>

                    {/* Table Recent Jobs */}
                    <Col xs={24} lg={10}>
                        <Card title="Việc làm mới đăng (7 ngày qua)" className={styles['chart-card']} bordered={false}>
                             <Table
                                dataSource={recentJobs}
                                columns={columns} 
                                pagination={false} 
                                size="small"
                                className={styles['recent-table']}
                                locale={{ emptyText: 'Không có job mới nào trong tuần qua' }}
                            />
                        </Card>
                    </Col>
                </Row>
            </Spin>
        </div>
    )
}
export default DashboardPage;