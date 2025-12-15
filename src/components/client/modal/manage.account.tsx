import { Modal, Table, Tabs, Tag, Button, Card, Tooltip, Avatar, Typography } from "antd";
import { isMobile } from "react-device-detect";
import type { TabsProps } from 'antd';
import { IResume } from "@/types/backend";
import { useState, useEffect } from 'react';
import { callFetchResumeByUser } from "@/config/api";
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { 
    FileTextOutlined, 
    IdcardOutlined, 
    LockOutlined, 
    EyeOutlined,
    UserOutlined,
    BuildOutlined
} from '@ant-design/icons';
import { motion, AnimatePresence } from "framer-motion"; 
import { useAppSelector } from "@/redux/hooks";
import UserInfo from './UserInfo';
import ChangePassword from './ChangePassword';

const { Title, Text } = Typography;

interface IProps {
    open: boolean;
    onClose: (v: boolean) => void;
}

// --- 1. CẤU HÌNH ANIMATION ---
const tabVariants = {
    hidden: { opacity: 0, x: -20, y: 0 },
    enter: { opacity: 1, x: 0, y: 0, transition: { duration: 0.3, type: "spring" } },
    exit: { opacity: 0, x: 20, y: 0, transition: { duration: 0.2 } },
};

const AnimatedTabContent = ({ children }: { children: React.ReactNode }) => (
    <motion.div
        initial="hidden"
        animate="enter"
        exit="exit"
        variants={tabVariants}
        style={{ width: '100%', height: '100%' }}
    >
        {children}
    </motion.div>
);

// --- 2. COMPONENT CON: UserResume (Đã tối ưu hiển thị bảng) ---
const UserResume = () => {
    const [listCV, setListCV] = useState<IResume[]>([]);
    const [isFetching, setIsFetching] = useState<boolean>(false);

    useEffect(() => {
        const init = async () => {
            setIsFetching(true);
            try {
                const res = await callFetchResumeByUser();
                if (res && res.data) {
                    setListCV(res.data.result as IResume[]);
                }
            } catch (error) {
                console.error("Error fetching resumes:", error);
            } finally {
                setIsFetching(false);
            }
        }
        init();
    }, [])

    const columns: ColumnsType<IResume> = [
        {
            title: 'STT',
            key: 'index',
            width: 50,
            align: "center",
            render: (text, record, index) => <span style={{ color: '#888' }}>{index + 1}</span>
        },
        {
            title: 'Thông tin công việc',
            key: 'jobInfo',
            // Gộp Công ty và Job Title vào 1 cột để tiết kiệm diện tích và đẹp hơn
            render: (text, record) => (
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                    {/* Tên Job: In đậm, cắt bớt nếu quá dài */}
                    <Tooltip title={record?.job?.name}>
                        <Text strong style={{ 
                            fontSize: 14, 
                            whiteSpace: 'nowrap', 
                            overflow: 'hidden', 
                            textOverflow: 'ellipsis', 
                            maxWidth: 280,
                            color: '#333'
                        }}>
                            {record?.job?.name}
                        </Text>
                    </Tooltip>

                    {/* Tên Công ty: Nhạt hơn, có icon */}
                    <Tooltip title={record?.companyName}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 5, color: '#666', fontSize: 13, marginTop: 4 }}>
                            <BuildOutlined style={{ fontSize: 11 }} />
                            <div style={{ 
                                whiteSpace: 'nowrap', 
                                overflow: 'hidden', 
                                textOverflow: 'ellipsis', 
                                maxWidth: 260 
                            }}>
                                {record?.companyName}
                            </div>
                        </div>
                    </Tooltip>
                </div>
            )
        },
        {
            title: 'Trạng thái',
            dataIndex: "status",
            width: 120,
            align: 'center',
            render: (status) => {
                let color = 'default';
                let label = 'Đang chờ';
                if (status === 'PENDING') { color = 'orange'; label = 'Đang chờ'; }
                else if (status === 'REVIEWING') { color = 'processing'; label = 'Đang xem'; }
                else if (status === 'APPROVED') { color = 'success'; label = 'Đã nhận'; }
                else if (status === 'REJECTED') { color = 'error'; label = 'Từ chối'; }
                
                return <Tag color={color} style={{ borderRadius: 12, padding: '0 10px', fontWeight: 500 }}>{label}</Tag>
            }
        },
        {
            title: 'Ngày gửi',
            dataIndex: "createdAt",
            width: 110,
            align: 'center',
            render(value) {
                return (
                    <div style={{ display: 'flex', flexDirection: 'column', fontSize: 13 }}>
                        <span>{dayjs(value).format('DD/MM/YYYY')}</span>
                        <span style={{ color: '#aaa', fontSize: 11 }}>{dayjs(value).format('HH:mm')}</span>
                    </div>
                )
            },
        },
        {
            title: '',
            width: 70,
            align: 'center',
            render(value, record) {
                return (
                    <Tooltip title="Xem CV">
                        <Button 
                            type="text"
                            shape="circle"
                            icon={<EyeOutlined style={{ color: '#1677ff' }} />} 
                            style={{ background: '#f0f5ff' }}
                            href={`${import.meta.env.VITE_BACKEND_URL}/storage/resume/${record?.url}`}
                            target="_blank"
                            rel="noreferrer"
                        />
                    </Tooltip>
                )
            },
        },
    ];

    return (
        <Card 
            title={
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <FileTextOutlined style={{ color: '#1677ff' }}/> 
                    <span>Lịch sử ứng tuyển</span>
                </div>
            } 
            bordered={false} 
            style={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)', borderRadius: 8 }}
        >
            <Table<IResume>
                columns={columns}
                dataSource={listCV}
                loading={isFetching}
                pagination={{ pageSize: 5, showSizeChanger: false }} 
                rowKey="id"
                size="middle"
                scroll={{ x: 600 }} // Cho phép scroll ngang trên mobile
            />
        </Card>
    )
}

// --- 3. COMPONENT CHÍNH: ManageAccount ---
const ManageAccount = (props: IProps) => {
    const { open, onClose } = props;
    const user = useAppSelector(state => state.account.user);
    
    // Logic xác định quyền
    const isCandidate = user?.role?.name !== 'SUPER_ADMIN' && user?.role?.name !== 'HUMAN_RESOURCE';
    const showResumeTab = isCandidate;

    // State quản lý tab đang chọn (Tách biệt khỏi Antd Tabs items)
    const [activeTab, setActiveTab] = useState(showResumeTab ? 'user-resume' : 'user-update-info');

    // Reset tab về mặc định khi mở modal
    useEffect(() => {
        if (open) {
            setActiveTab(showResumeTab ? 'user-resume' : 'user-update-info');
        }
    }, [open, showResumeTab]);

    // Danh sách Menu (Chỉ dùng để hiển thị Sidebar)
    const items: TabsProps['items'] = [
        ...(showResumeTab ? [{
            key: 'user-resume',
            label: (
                <span style={{ fontSize: 15, padding: '4px 0', display: 'flex', alignItems: 'center', gap: 8 }}>
                    <FileTextOutlined /> Hồ sơ ứng tuyển
                </span>
            ),
        }] : []),
        {
            key: 'user-update-info',
            label: (
                <span style={{ fontSize: 15, padding: '4px 0', display: 'flex', alignItems: 'center', gap: 8 }}>
                    <IdcardOutlined /> Thông tin cá nhân
                </span>
            ),
        },
        {
            key: 'user-password',
            label: (
                <span style={{ fontSize: 15, padding: '4px 0', display: 'flex', alignItems: 'center', gap: 8 }}>
                    <LockOutlined /> Đổi mật khẩu
                </span>
            ),
        },
    ];

    return (
        <Modal
            open={open}
            onCancel={() => onClose(false)}
            maskClosable={false}
            footer={null}
            destroyOnClose={true}
            width={isMobile ? "100%" : 950}
            styles={{
                body: { padding: 0, height: isMobile ? 'auto' : '550px', overflow: 'hidden' },
                content: { borderRadius: 16, overflow: 'hidden', padding: 0 }
            }}
            centered 
        >
            <div style={{ display: 'flex', height: '100%', flexDirection: isMobile ? 'column' : 'row' }}>
                
                {/* --- SIDEBAR TRÁI --- */}
                <div style={{ 
                    width: isMobile ? '100%' : 260, 
                    background: 'linear-gradient(180deg, #f0f5ff 0%, #f7f9fc 100%)', 
                    borderRight: '1px solid #f0f0f0',
                    padding: '30px 0',
                    display: 'flex',
                    flexDirection: 'column'
                }}>
                    <div style={{ textAlign: 'center', marginBottom: 24, padding: '0 20px' }}>
                        <Avatar size={80} icon={<UserOutlined />} style={{ backgroundColor: '#1677ff', marginBottom: 16, boxShadow: '0 4px 10px rgba(22, 119, 255, 0.3)' }} />
                        <Title level={4} style={{ margin: 0, fontSize: 18 }}>{user?.name}</Title>
                        <Text type="secondary" style={{ fontSize: 13 }}>{user?.email}</Text>
                    </div>

                    {/* MENU ĐIỀU HƯỚNG: Khi click sẽ set state activeTab */}
                    <Tabs
                        activeKey={activeTab} 
                        onChange={setActiveTab} 
                        items={items}
                        tabPosition={isMobile ? 'top' : 'left'}
                        style={{ flex: 1, width: '100%' }}
                        tabBarStyle={{ border: 'none', width: '100%' }}
                        size="large"
                        indicator={{ size: (origin) => origin - 20, align: 'center' }}
                    />
                </div>

                {/* --- CONTENT PHẢI (Hiển thị nội dung thủ công) --- */}
                <div style={{ flex: 1, padding: '30px', background: '#fff', overflowY: 'auto', height: '100%' }}>
                    
                    <div style={{ marginBottom: 20 }}>
                         <Title level={3} style={{ margin: 0, fontWeight: 700 }}>Cài đặt tài khoản</Title>
                         <Text type="secondary">Quản lý thông tin hồ sơ và bảo mật</Text>
                    </div>

                    <AnimatePresence mode="wait">
                        {activeTab === 'user-resume' && (
                            <AnimatedTabContent key="resume"><UserResume /></AnimatedTabContent>
                        )}
                        {activeTab === 'user-update-info' && (
                            <AnimatedTabContent key="info">
                                <Card bordered={false} style={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)', borderRadius: 8 }}>
                                    <UserInfo />
                                </Card>
                            </AnimatedTabContent>
                        )}
                        {activeTab === 'user-password' && (
                            <AnimatedTabContent key="pass">
                                <Card bordered={false} style={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)', borderRadius: 8 }}>
                                    <ChangePassword />
                                </Card>
                            </AnimatedTabContent>
                        )}
                    </AnimatePresence>

                </div>
            </div>
        </Modal>
    )
}
export default ManageAccount;