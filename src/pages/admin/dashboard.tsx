import { Card, Col, Row, Table, Tag, Avatar, Spin, Tooltip as AntTooltip, Button } from "antd";
import CountUp from 'react-countup';
import { 
    UserOutlined, 
    BankOutlined, 
    FileTextOutlined, 
    SolutionOutlined,
    ArrowRightOutlined
} from "@ant-design/icons";
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import styles from '@/styles/dashboard.module.scss';
import { useEffect, useState } from "react";
import { callFetchCompany, callFetchJob, callFetchResume, callFetchUser } from "@/config/api";
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/vi';
import { useAppSelector } from "@/redux/hooks";
import { ALL_PERMISSIONS } from "@/config/permissions";
import { useNavigate } from "react-router-dom";

dayjs.extend(relativeTime);
dayjs.locale('vi');

const DashboardPage = () => {
    const navigate = useNavigate();
    const [totalUsers, setTotalUsers] = useState(0);
    const [totalCompanies, setTotalCompanies] = useState(0);
    const [totalJobs, setTotalJobs] = useState(0);
    const [totalResumes, setTotalResumes] = useState(0);
    
    const [recentJobs, setRecentJobs] = useState<any[]>([]);
    const [chartData, setChartData] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    // 1. Lấy thông tin Role của user
    const userRole = useAppSelector(state => state.account.user.role.name);
    const permissions = useAppSelector(state => state.account.user.role.permissions);

    // 2. Biến kiểm tra xem có phải HR không
    const isHR = userRole === 'HUMAN_RESOURCE';

    const hasPermission = (apiPath: string, method: string) => {
        if (!permissions?.length) return false;
        return permissions.some(item => item.apiPath === apiPath && item.method === method);
    }

    const canViewResume = hasPermission(ALL_PERMISSIONS.RESUMES.GET_PAGINATE.apiPath, ALL_PERMISSIONS.RESUMES.GET_PAGINATE.method);
    const canViewUser = hasPermission(ALL_PERMISSIONS.USERS.GET_PAGINATE.apiPath, ALL_PERMISSIONS.USERS.GET_PAGINATE.method);
    const canViewCompany = hasPermission(ALL_PERMISSIONS.COMPANIES.GET_PAGINATE.apiPath, ALL_PERMISSIONS.COMPANIES.GET_PAGINATE.method);
    const canViewJob = hasPermission(ALL_PERMISSIONS.JOBS.GET_PAGINATE.apiPath, ALL_PERMISSIONS.JOBS.GET_PAGINATE.method);

    useEffect(() => {
        const initDashboard = async () => {
            setLoading(true);
            const promises = [];
            
            if (canViewUser) promises.push(callFetchUser("page=1&size=1"));
            else promises.push(Promise.resolve(null)); 

            if (canViewCompany) promises.push(callFetchCompany("page=1&size=1"));
            else promises.push(Promise.resolve(null));

            if (canViewJob) promises.push(callFetchJob("page=1&size=1000&sort=createdAt,desc"));
            else promises.push(Promise.resolve(null));

            if (canViewResume) promises.push(callFetchResume("page=1&size=2000&sort=createdAt,desc"));
            else promises.push(Promise.resolve(null));

            try {
                const [resUser, resCompany, resJob, resResume] = await Promise.all(promises);

                if (resUser?.data?.meta) setTotalUsers(resUser.data.meta.total);
                if (resCompany?.data?.meta) setTotalCompanies(resCompany.data.meta.total);
                if (resJob?.data?.meta) setTotalJobs(resJob.data.meta.total);
                if (resResume?.data?.meta) setTotalResumes(resResume.data.meta.total);

                if (resJob?.data?.result) {
                    const jobs = resJob.data.result;
                    const allResumes = resResume?.data?.result || [];
                    const sevenDaysAgo = dayjs().subtract(7, 'day');
                    
                    const newJobs = jobs.filter((item: any) => dayjs(item.createdAt).isAfter(sevenDaysAgo))
                                        .slice(0, 5)
                                        .map((item: any) => {
                                            const applicantCount = allResumes.filter((r: any) => r.job?.id === item.id).length;
                                            return {
                                                key: item.id,
                                                id: item.id, 
                                                job: item.name,
                                                company: item.company?.name,
                                                salary: item.salary,
                                                status: item.active,
                                                createdAt: item.createdAt,
                                                applicants: applicantCount 
                                            }
                                        });
                    setRecentJobs(newJobs);
                }

                if (resJob?.data?.result) {
                    const monthlyData = processChartData(
                        resJob.data.result, 
                        resResume?.data?.result || []
                    );
                    setChartData(monthlyData);
                }

            } catch (error) {
                console.log("Dashboard error:", error);
            }
            setLoading(false);
        }
        initDashboard();
    }, [permissions]);

    const processChartData = (jobs: any[], resumes: any[]) => {
        const months = [];
        for (let i = 5; i >= 0; i--) {
            months.push(dayjs().subtract(i, 'month').format('MM/YYYY'));
        }
        return months.map(month => {
            const jobsCount = jobs.filter(j => dayjs(j.createdAt).format('MM/YYYY') === month).length;
            const cvCount = resumes.filter(r => dayjs(r.createdAt).format('MM/YYYY') === month).length;
            return { name: `T${month.split('/')[0]}`, jobs: jobsCount, cv: cvCount }
        });
    }

    const stats = [];
    if (canViewUser) stats.push({ title: "Tổng người dùng", value: totalUsers, icon: <UserOutlined />, color: "gradient-purple" });
    if (canViewCompany) stats.push({ title: "Tổng công ty", value: totalCompanies, icon: <BankOutlined />, color: "gradient-blue" });
    if (canViewJob) stats.push({ title: "Công việc đang tuyển", value: totalJobs, icon: <SolutionOutlined />, color: "gradient-orange" });
    if (canViewResume) stats.push({ title: "Hồ sơ ứng tuyển", value: totalResumes, icon: <FileTextOutlined />, color: "gradient-green" });

    const formatter = (value: number | string) => <CountUp end={Number(value)} separator="," duration={2.5} />;

    // --- 3. ĐỊNH NGHĨA CỘT (DYNAMIC) ---
    // Khai báo các cột cơ bản ai cũng thấy
    const baseColumns = [
        {
            title: 'Tên công việc & Công ty', 
            key: 'job_company',
            width: 220, 
            render: (_: any, record: any) => (
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <AntTooltip title={record.job}>
                        <a 
                            onClick={() => navigate(`/admin/job/upsert?id=${record.id}`)} 
                            style={{ fontWeight: 600, color: '#1890ff', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', cursor: 'pointer' }}
                        >
                            {record.job}
                        </a>
                    </AntTooltip>
                    <AntTooltip title={record.company}>
                        <span style={{ fontSize: '12px', color: '#888', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            {record.company}
                        </span>
                    </AntTooltip>
                </div>
            )
        },
        {
            title: 'Lương & Ngày đăng', 
            key: 'salary_date',
            width: 140,
            render: (_: any, record: any) => (
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontWeight: 600, color: '#52c41a' }}>
                        {`${(record.salary + "").replace(/\B(?=(\d{3})+(?!\d))/g, ',')} đ`}
                    </span>
                    <span style={{ fontSize: '11px', color: '#aaa' }}>
                        {dayjs(record.createdAt).fromNow()}
                    </span>
                </div>
            )
        },
    ];

    // Cột trạng thái (để cuối cùng)
    const statusColumn = {
        title: 'Trạng thái',
        dataIndex: 'status',
        key: 'status',
        width: 80,
        align: 'center' as const,
        render: (status: boolean) => (
            <Tag color={status ? 'success' : 'error'} style={{ marginRight: 0 }}>
                {status ? 'Active' : 'Hidden'}
            </Tag>
        )
    };

    // Cột Ứng viên (Chỉ dành cho HR)
    const applicantColumn = {
        title: 'Ứng viên',
        dataIndex: 'applicants',
        key: 'applicants',
        width: 90,
        align: 'center' as const,
        render: (val: number) => (
            <AntTooltip title={`${val} hồ sơ ứng tuyển`}>
                <Avatar 
                    shape="square" 
                    size="small"
                    style={{ 
                        backgroundColor: val > 0 ? '#fff7e6' : '#f5f5f5', 
                        color: val > 0 ? '#fa8c16' : '#d9d9d9',
                        borderColor: val > 0 ? '#ffd591' : '#d9d9d9',
                        border: '1px solid',
                        fontWeight: 'bold'
                    }}
                >
                    {val}
                </Avatar>
            </AntTooltip>
        )
    };

    // --- 4. GỘP CỘT ---
    let columns = [...baseColumns];
    // Chỉ push cột Ứng viên nếu là HR
    if (isHR) {
        columns.push(applicantColumn);
    }
    // Push cột trạng thái cuối cùng
    columns.push(statusColumn);

    return (
        <div className={styles['dashboard-container']}>
            <Spin spinning={loading}>
                {/* SECTION 1: STATS */}
                <Row gutter={[24, 24]}>
                    {stats.map((item, index) => (
                        <Col xs={24} sm={12} md={24 / stats.length} key={index}>
                            <Card className={`${styles['stat-card']} ${styles[item.color]}`} bordered={false}>
                                <div className={styles['stat-icon-wrapper']}>{item.icon}</div>
                                <div className={styles['stat-content']}>
                                    <div className={styles['stat-title']}>{item.title}</div>
                                    <div className={styles['stat-value']}>{formatter(item.value)}</div>
                                </div>
                            </Card>
                        </Col>
                    ))}
                </Row>

                <div style={{ height: 24 }}></div>

                {/* SECTION 2: CHART & TABLE */}
                <Row gutter={[24, 24]}>
                    <Col xs={24} lg={14}>
                        <Card title="Thống kê Tuyển dụng (6 tháng)" className={styles['chart-card']} bordered={false}>
                            <div style={{ width: '100%', height: 380 }}>
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
                                        {canViewResume && <Area type="monotone" dataKey="cv" stroke="#82ca9d" fillOpacity={1} fill="url(#colorCv)" name="CV Ứng tuyển" />}
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </Card>
                    </Col>

                    <Col xs={24} lg={10}>
                        <Card 
                            title="Việc làm mới đăng (7 ngày qua)" 
                            className={styles['chart-card']} 
                            bordered={false}
                            extra={<Button type="link" size="small" onClick={() => navigate('/admin/job')}>Xem tất cả <ArrowRightOutlined /></Button>}
                            bodyStyle={{ padding: '0 10px 10px 10px' }} 
                        >
                             <Table 
                                dataSource={recentJobs} 
                                columns={columns} // Sử dụng biến columns đã được xử lý logic
                                pagination={false} 
                                size="middle" 
                                className={styles['recent-table']}
                                locale={{ emptyText: 'Chưa có job mới trong tuần này' }}
                                scroll={{ x: 'max-content' }} 
                            />
                        </Card>
                    </Col>
                </Row>
            </Spin>
        </div>
    )
}

export default DashboardPage;