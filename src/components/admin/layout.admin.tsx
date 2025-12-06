import React, { useState, useEffect } from 'react';
import {
    AppstoreOutlined,
    ExceptionOutlined,
    ApiOutlined,
    UserOutlined,
    BankOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    AliwangwangOutlined,
    BugOutlined,
    ScheduleOutlined,
    LogoutOutlined,
    DashboardOutlined
} from '@ant-design/icons';
import { Layout, Menu, Dropdown, Space, message, Avatar, ConfigProvider } from 'antd';
import { Outlet, useLocation, useNavigate, Link } from "react-router-dom";
import { callLogout } from '@/config/api';
import { useAppDispatch, useAppSelector } from '@/redux/hooks';
import { isMobile } from 'react-device-detect';
import type { MenuProps } from 'antd';
import { setLogoutAction } from '@/redux/slice/accountSlide';
import { ALL_PERMISSIONS } from '@/config/permissions';
import styles from '@/styles/layout.admin.module.scss'; // Import style mới
import { FaReact } from 'react-icons/fa'; // Icon logo (cần cài react-icons nếu chưa có)

const { Header, Content, Sider } = Layout;

const LayoutAdmin = () => {
    const location = useLocation();
    const [collapsed, setCollapsed] = useState(false);
    const [activeMenu, setActiveMenu] = useState('');
    const user = useAppSelector(state => state.account.user);
    const permissions = useAppSelector(state => state.account.user.role.permissions);
    const [menuItems, setMenuItems] = useState<MenuProps['items']>([]);

    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    useEffect(() => {
        const ACL_ENABLE = import.meta.env.VITE_ACL_ENABLE;
        if (permissions?.length || ACL_ENABLE === 'false') {
            
            // Helper function để kiểm tra quyền
            const hasPermission = (module: any) => {
                if (ACL_ENABLE === 'false') return true;
                return permissions?.some(item => 
                    item.apiPath === module.GET_PAGINATE.apiPath && 
                    item.method === module.GET_PAGINATE.method
                );
            }

            const full = [
                {
                    label: <Link to='/admin'>Dashboard</Link>,
                    key: '/admin',
                    icon: <DashboardOutlined />
                },
                ...(hasPermission(ALL_PERMISSIONS.COMPANIES) ? [{
                    label: <Link to='/admin/company'>Company</Link>,
                    key: '/admin/company',
                    icon: <BankOutlined />,
                }] : []),

                ...(hasPermission(ALL_PERMISSIONS.USERS) ? [{
                    label: <Link to='/admin/user'>User</Link>,
                    key: '/admin/user',
                    icon: <UserOutlined />
                }] : []),

                ...(hasPermission(ALL_PERMISSIONS.JOBS) ? [{
                    label: <Link to='/admin/job'>Job</Link>,
                    key: '/admin/job',
                    icon: <ScheduleOutlined />
                }] : []),

                ...(hasPermission(ALL_PERMISSIONS.RESUMES) ? [{
                    label: <Link to='/admin/resume'>Resume</Link>,
                    key: '/admin/resume',
                    icon: <AliwangwangOutlined />
                }] : []),

                ...(hasPermission(ALL_PERMISSIONS.PERMISSIONS) ? [{
                    label: <Link to='/admin/permission'>Permission</Link>,
                    key: '/admin/permission',
                    icon: <ApiOutlined />
                }] : []),

                ...(hasPermission(ALL_PERMISSIONS.ROLES) ? [{
                    label: <Link to='/admin/role'>Role</Link>,
                    key: '/admin/role',
                    icon: <ExceptionOutlined />
                }] : []),
            
            ];
            setMenuItems(full);
        }
    }, [permissions]);

    useEffect(() => {
        setActiveMenu(location.pathname);
    }, [location]);

  const handleLogout = async () => {
        try {
            // 1. Gọi API logout (để Backend xóa cookie httpOnly nếu có)
            await callLogout();
        } catch (error) {
            // Nếu token lỗi/hết hạn, API sẽ báo lỗi. 
            // Ta chỉ log ra console, không chặn người dùng.
            console.log("Lỗi API Logout:", error);
        } finally {
            // 2. QUAN TRỌNG NHẤT: Luôn luôn chạy vào đây để xóa state Frontend
            dispatch(setLogoutAction({})); 
            message.success('Đăng xuất thành công');
            navigate('/');
        }
    }
    const itemsDropdown: MenuProps['items'] = [
        {
            label: <Link to={'/'}>Trang chủ</Link>,
            key: 'home',
        },
        {
            type: 'divider',
        },
        {
            label: <span style={{ cursor: 'pointer', color: '#ff4d4f' }} onClick={handleLogout}>Đăng xuất</span>,
            key: 'logout',
            icon: <LogoutOutlined />,
        },
    ];

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: '#1890ff', // Màu chủ đạo
                    fontFamily: "'Roboto', sans-serif",
                },
            }}
        >
            <Layout className={styles['layout-container']}>
                {/* --- SIDER --- */}
                {!isMobile &&
                    <Sider
                        width={250}
                        theme='dark'
                        collapsible
                        collapsed={collapsed}
                        onCollapse={(value) => setCollapsed(value)}
                        className={styles['admin-sider']}
                    >
                        {/* LOGO AREA */}
                        <div className={`${styles['logo-wrapper']} ${collapsed ? styles.collapsed : ''}`}>
                            <FaReact style={{ fontSize: 28, color: '#1890ff' }} />
                            {!collapsed && <span className={styles['logo-text']}>JOB<span>HUNTER</span></span>}
                        </div>

                        <Menu
                            theme="dark"
                            mode="inline"
                            selectedKeys={[activeMenu]}
                            items={menuItems}
                            onClick={(e) => setActiveMenu(e.key)}
                            style={{ borderRight: 0, marginTop: 10 }}
                        />
                    </Sider>
                }

                {/* --- MAIN LAYOUT --- */}
                <Layout style={{ backgroundColor: '#f0f2f5' }}>
                    {/* HEADER */}
                    {!isMobile &&
                        <Header className={styles['admin-header']}>
                            {/* Toggle Button */}
                            <span className={styles['trigger-btn']} onClick={() => setCollapsed(!collapsed)}>
                                {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                            </span>

                            {/* User Dropdown */}
                            <div className={styles['header-right']}>
                                <Dropdown menu={{ items: itemsDropdown }} trigger={['click']} placement="bottomRight">
                                    <div className={styles['user-dropdown']}>
                                        <Avatar 
                                            style={{ backgroundColor: '#1890ff', verticalAlign: 'middle' }} 
                                            size="default"
                                        >
                                            {user?.name?.substring(0, 1)?.toUpperCase()}
                                        </Avatar>
                                        <span className={styles['username']}>
                                            {user?.name?.length > 15 ? user?.name?.substring(0, 15) + "..." : user?.name}
                                        </span>
                                    </div>
                                </Dropdown>
                            </div>
                        </Header>
                    }

                    {/* CONTENT */}
                    <Content className={styles['admin-content']}>
                         <Outlet />
                    </Content>

                    {/* FOOTER (Optional) */}
                    {/* <Footer style={{ textAlign: 'center', color: '#888' }}>
                         JobHunter ©2025 Created by You
                    </Footer> */}
                </Layout>
            </Layout>
        </ConfigProvider>
    );
};

export default LayoutAdmin;