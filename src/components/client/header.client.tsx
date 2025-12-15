import { useState, useEffect } from 'react';
import {
    CodeOutlined,
    LogoutOutlined,
    MenuFoldOutlined,
    DownOutlined,
    UserOutlined,
    DashboardOutlined,
    ProfileOutlined,
    ApiOutlined,
    EnvironmentOutlined,
    BankOutlined,
    ThunderboltOutlined
} from '@ant-design/icons';

import { Avatar, Drawer, Dropdown, MenuProps, message, Badge, Row, Col, Menu } from 'antd';
import styles from '@/styles/client.module.scss';
import { isMobile } from 'react-device-detect';
import { 
    FaReact, FaJava, FaPython, FaNodeJs, FaPhp, FaDocker, 
    FaAngular, FaVuejs, FaAws 
} from 'react-icons/fa';
import { 
    SiMysql, SiDotnet, SiSpring, SiTypescript, SiPostgresql, SiMongodb 
} from "react-icons/si";
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/redux/hooks';
import { callLogout, callFetchAllSkill, callFetchCompany, callFetchJob } from '@/config/api'; // Import thêm API Company và Job
import { setLogoutAction } from '@/redux/slice/accountSlide';
import ManageAccount from './modal/manage.account';
import BlogPage from '../../pages/BlogPage'
import { getLocationName } from '@/config/utils'; // Hàm helper chuyển đổi tên địa điểm (nếu có)

const Header = (props: any) => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const isAuthenticated = useAppSelector(state => state.account.isAuthenticated);
    const user = useAppSelector(state => state.account.user);
    const [openMobileMenu, setOpenMobileMenu] = useState<boolean>(false);
    const [current, setCurrent] = useState('home');
    const location = useLocation();
    const [openMangeAccount, setOpenManageAccount] = useState<boolean>(false);

    const [activeCategory, setActiveCategory] = useState<string>('skill');
    
    // --- STATE LƯU DỮ LIỆU ĐỘNG ---
    const [dynamicSkills, setDynamicSkills] = useState<any[]>([]);
    const [dynamicCompanies, setDynamicCompanies] = useState<any[]>([]);
    const [dynamicLevels, setDynamicLevels] = useState<any[]>([]);
    const [dynamicLocations, setDynamicLocations] = useState<any[]>([]);

    useEffect(() => {
        setCurrent(location.pathname);
    }, [location])

    // --- 1. HÀM MAP ICON SKILL ---
    const getSkillIcon = (skillName: string) => {
        const name = skillName?.toLowerCase() || "";
        if (name.includes('java') && !name.includes('script')) return <FaJava style={{ color: '#e76f00' }} />;
        if (name.includes('react')) return <FaReact style={{ color: '#61dafb' }} />;
        if (name.includes('node')) return <FaNodeJs style={{ color: '#68a063' }} />;
        if (name.includes('php')) return <FaPhp style={{ color: '#777bb3' }} />;
        if (name.includes('python')) return <FaPython style={{ color: '#306998' }} />;
        if (name.includes('sql') || name.includes('database')) return <SiMysql style={{ color: '#00758f' }} />;
        if (name.includes('net') || name.includes('c#')) return <SiDotnet style={{ color: '#512bd4' }} />;
        if (name.includes('spring')) return <SiSpring style={{ color: '#6db33f' }} />;
        if (name.includes('angular')) return <FaAngular style={{ color: '#dd0031' }} />;
        if (name.includes('vue')) return <FaVuejs style={{ color: '#42b883' }} />;
        if (name.includes('aws')) return <FaAws style={{ color: '#ff9900' }} />;
        if (name.includes('script')) return <SiTypescript style={{ color: '#3178c6' }} />;
        if (name.includes('docker')) return <FaDocker style={{ color: '#2496ed' }} />;
        
        return <CodeOutlined />;
    }

    // --- 2. GỌI API LẤY DỮ LIỆU ---
    useEffect(() => {
        // 2.1 Lấy Skills
        const fetchSkills = async () => {
            const query = `page=0&size=24&sort=createdAt,desc`;
            try {
                const res = await callFetchAllSkill(query);
                const data = (res as any)?.result ?? res?.data?.result ?? [];
                if (data.length > 0) {
                    setDynamicSkills(data.map((item: any) => ({
                        name: item.name,
                        icon: getSkillIcon(item.name)
                    })));
                }
            } catch (error) {}
        }

        // 2.2 Lấy Companies
        const fetchCompanies = async () => {
            const query = `page=0&size=12&sort=updatedAt,desc`; // Lấy 12 công ty mới cập nhật
            try {
                const res = await callFetchCompany(query);
                const data = (res as any)?.result ?? res?.data?.result ?? [];
                if (data.length > 0) {
                    setDynamicCompanies(data.map((item: any) => ({
                        name: item.name,
                        id: item.id,
                        // Nếu có logo thì hiển thị logo, không thì icon Building
                        icon: item.logo ? 
                            <Avatar shape="square" size={20} src={`${import.meta.env.VITE_BACKEND_URL}/storage/company/${item.logo}`} /> 
                            : <BankOutlined style={{ color: '#ea1e30' }} />
                    })));
                }
            } catch (error) {}
        }

     // ... các import giữ nguyên

// 2.3 Lấy Jobs để trích xuất Levels và Locations
const fetchJobsMetadata = async () => {
    const query = `page=0&size=100&sort=createdAt,desc`;
    try {
        const res = await callFetchJob(query);
        
        // DEBUG: Xem dữ liệu trả về trong Console trình duyệt
        console.log(">>> Check Jobs Response:", res);

        // Lấy mảng jobs từ result hoặc data.result tùy cấu trúc response
        const data = (res as any)?.result ?? res?.data?.result ?? [];
        
        if (data.length > 0) {
            // --- XỬ LÝ LEVEL ---
            // Lọc các level khác null/undefined/rỗng
            const levelsRaw = data.map((j: any) => j.level).filter((l: any) => l);
            // Lấy unique (duy nhất)
            const uniqueLevels = [...new Set(levelsRaw)];

            console.log(">>> Unique Levels Found:", uniqueLevels); // Kiểm tra xem có INTERN, MIDDLE... không

            if (uniqueLevels.length > 0) {
                const levelsMapped = uniqueLevels.map((lvl: any) => ({
                    name: lvl, 
                    label: mapLevelName(lvl), 
                    icon: <ThunderboltOutlined style={{ color: 'gold' }} />
                }));
                setDynamicLevels(levelsMapped);
            }

            // --- XỬ LÝ LOCATION ---
            const locationsRaw = data.map((j: any) => j.location).filter((l: any) => l);
            const uniqueLocations = [...new Set(locationsRaw)];
            
            if (uniqueLocations.length > 0) {
                const locationsMapped = uniqueLocations.map((loc: any) => ({
                    name: loc, 
                    label: getLocationName(loc), 
                    icon: <EnvironmentOutlined style={{ color: '#58aaab' }} />
                }));
                setDynamicLocations(locationsMapped);
            }
        }
    } catch (error) {
        console.error("Failed to fetch jobs metadata:", error);
    }
}

        fetchSkills();
        fetchCompanies();
        fetchJobsMetadata();
    }, []);

    // Helper map tên Level cho đẹp
    const mapLevelName = (level: string) => {
        switch(level) {
            case 'INTERN': return 'Intern / Thực tập sinh';
            case 'FRESHER': return 'Fresher';
            case 'JUNIOR': return 'Junior';
            case 'MIDDLE': return 'Middle';
            case 'SENIOR': return 'Senior';
            case 'LEAD': return 'Team Leader';
            case 'MANAGER': return 'Project Manager';
            default: return level;
        }
    }
const handleLogout = async () => {
    try {
        // 1. Gọi API để Backend xóa Cookie (nếu được)
        await callLogout(); 
    } catch (error) {
        // Nếu API lỗi (token hết hạn, server lỗi...), ta chỉ log ra console chứ không chặn người dùng
        console.log("Lỗi khi gọi API logout:", error);
    } finally {
        // 2. QUAN TRỌNG: Luôn luôn xóa data ở Frontend dù API thành công hay thất bại
        dispatch(setLogoutAction({})); 
        message.success('Đăng xuất thành công');
        navigate('/');
    }
}

    // --- DỮ LIỆU MEGA MENU (ĐÃ GHÉP DỮ LIỆU ĐỘNG) ---
    const megaMenuData: Record<string, any[]> = {
        // 1. Kỹ năng (Dynamic)
        skill: dynamicSkills.length > 0 ? dynamicSkills : [
            { name: 'Java', icon: <FaJava /> }, { name: 'ReactJS', icon: <FaReact /> }
        ],
        
        // 2. Chuyên môn (Giữ tĩnh vì DB không có bảng này, hoặc map theo skill)
        specialization: [
            { name: 'Lập trình viên Backend', icon: <CodeOutlined /> }, 
            { name: 'Lập trình viên Frontend', icon: <CodeOutlined /> },
            { name: 'Fullstack Developer', icon: <CodeOutlined /> },
            { name: 'Mobile Developer', icon: <CodeOutlined /> },
            { name: 'Tester / QC', icon: <CodeOutlined /> },
            { name: 'DevOps Engineer', icon: <CodeOutlined /> },
        ],

        // 3. Cấp bậc (Dynamic từ Job)
        level: dynamicLevels.length > 0 ? dynamicLevels : [
            { name: 'FRESHER', label: 'Fresher', icon: <UserOutlined /> },
            { name: 'JUNIOR', label: 'Junior', icon: <UserOutlined /> }
        ],

        // 4. Công ty (Dynamic từ Company)
        company: dynamicCompanies.length > 0 ? dynamicCompanies : [
            { name: 'FPT Software', icon: <BankOutlined /> }
        ],

        // 5. Thành phố (Dynamic từ Job)
        location: dynamicLocations.length > 0 ? dynamicLocations : [
            { name: 'HOCHIMINH', label: 'Hồ Chí Minh', icon: <EnvironmentOutlined /> },
            { name: 'HANOI', label: 'Hà Nội', icon: <EnvironmentOutlined /> }
        ]
    };

    // Render nội dung cột phải của Mega Menu
    const renderMegaMenuRight = () => {
        const items = megaMenuData[activeCategory] || [];
        return (
            <div className={styles['tech-grid']}>
                {items.map((item, index) => {
                    // Xử lý URL Search thông minh
                    let url = `/job?query=${item.name}`; // Mặc định
                    
                    if (activeCategory === 'skill') url = `/job?skills=${item.name}`;
                    if (activeCategory === 'location') url = `/job?location=${item.name}`; // Gửi mã Enum (VD: HOCHIMINH)
                    if (activeCategory === 'level') url = `/job?level=${item.name}`;       // Gửi mã Enum (VD: INTERN)
                    if (activeCategory === 'company') url = `/job?query=${item.name}`;     // Search theo tên cty

                    return (
                        <Link 
                            to={url}
                            key={index} 
                            className={styles['tech-item']}
                        >
                            <span className={styles['icon']}>{item.icon || <ApiOutlined />}</span>
                            {/* Ưu tiên hiển thị Label (tên đẹp) nếu có, không thì dùng Name */}
                            {item.label || item.name} 
                        </Link>
                    )
                })}
            </div>
        );
    };

    // --- CÁC PHẦN MENU KHÁC GIỮ NGUYÊN ---
    const megaMenuContent = (
        <div className={styles['mega-menu-container']}>
            <Row style={{ height: '100%' }}>
                {/* Cột trái: Danh mục */}
                <Col span={9} className={styles['mega-menu-left']}>
                    <div 
                        className={`${styles['menu-cat']} ${activeCategory === 'skill' ? styles['active'] : ''}`}
                        onMouseEnter={() => setActiveCategory('skill')}
                    >
                        Việc làm IT theo kỹ năng <span className={styles['arrow']}>{'>'}</span>
                    </div>
                    <div 
                        className={`${styles['menu-cat']} ${activeCategory === 'specialization' ? styles['active'] : ''}`}
                        onMouseEnter={() => setActiveCategory('specialization')}
                    >
                        Việc làm IT theo chuyên môn <span className={styles['arrow']}>{'>'}</span>
                    </div>
                    <div 
                        className={`${styles['menu-cat']} ${activeCategory === 'level' ? styles['active'] : ''}`}
                        onMouseEnter={() => setActiveCategory('level')}
                    >
                        Việc làm IT theo cấp bậc <span className={styles['arrow']}>{'>'}</span>
                    </div>
                    <div 
                        className={`${styles['menu-cat']} ${activeCategory === 'company' ? styles['active'] : ''}`}
                        onMouseEnter={() => setActiveCategory('company')}
                    >
                        Việc làm IT theo công ty <span className={styles['arrow']}>{'>'}</span>
                    </div>
                    <div 
                        className={`${styles['menu-cat']} ${activeCategory === 'location' ? styles['active'] : ''}`}
                        onMouseEnter={() => setActiveCategory('location')}
                    >
                        Việc làm IT theo thành phố <span className={styles['arrow']}>{'>'}</span>
                    </div>
                </Col>
                {/* Cột phải: Nội dung động */}
                <Col span={15} className={styles['mega-menu-right']}>
                    {renderMegaMenuRight()}
                    <div className={styles['view-all']}>
                        <Link to="/job">Xem tất cả {'>'}</Link>
                    </div>
                </Col>
            </Row>
        </div>
    );

    const blogItems: MenuProps['items'] = [
        { key: 'salary', label: <Link to="/blog/salary">Báo Cáo Lương IT</Link> },
        { key: 'career', label: <Link to="/blog/career">Sự Nghiệp IT</Link> },
        { key: 'promotion', label: <Link to="/blog/promotion">Ứng Tuyển & Thăng Tiến</Link> },
        { key: 'expert', label: <Link to="/blog/expert">Chuyên Môn IT</Link> },
    ];

    const itemsDropdown: MenuProps['items'] = [
        {
            label: <div onClick={() => setOpenManageAccount(true)} style={{ cursor: 'pointer' }}>Quản lý tài khoản</div>,
            key: 'manage-account',
            icon: <ProfileOutlined />
        },
      ...(user?.role?.name && user.role.name !== 'USER' ? [{
        label: <Link to={"/admin"}>Trang Quản Trị</Link>,
        key: 'admin',
        icon: <DashboardOutlined />
    }] : []),
        { type: 'divider' },
        {
            label: <div onClick={() => handleLogout()} style={{ cursor: 'pointer' }}>Đăng xuất</div>,
            key: 'logout',
            icon: <LogoutOutlined />,
            danger: true
        },
    ];

    const itemsMobiles: MenuProps['items'] = [
        { label: <Link to={'/'}>Trang Chủ</Link>, key: '/' },
        { label: <Link to={'/job'}>Việc Làm IT</Link>, key: '/job' },
        { label: <Link to={'/company'}>Top Công ty IT</Link>, key: '/company' },
        { label: <Link to={'/blog'}>Blog IT</Link>, key: '/blog' },
    ];

    return (
        <>
            <div className={styles["header-section"]}>
                <div className={styles["container"]}>
                    {!isMobile ?
                        <div className={styles["desktop-nav-wrapper"]}>
                            <div className={styles["nav-left"]}>
                                <div className={styles['brand']} onClick={() => navigate('/')}>
                                    <FaReact className={styles['logo-icon']} />
                                    <span className={styles['brand-text']}>FIND<span className={styles['red-text']}>JOB</span></span>
                                </div>

                                <nav className={styles['main-menu']}>
                                    <Dropdown
                                        dropdownRender={() => megaMenuContent}
                                        trigger={['hover']}
                                        overlayClassName={styles['mega-menu-overlay']}
                                    >
                                        <div className={`${styles['menu-item']} ${location.pathname.includes('/job') ? styles['active'] : ''}`}>
                                            Việc Làm IT <DownOutlined style={{ fontSize: '10px', marginLeft: '4px' }} />
                                        </div>
                                    </Dropdown>

                                    <Link to={'/company'} className={`${styles['menu-item']} ${location.pathname === '/company' ? styles['active'] : ''}`}>
                                        Top Công ty IT
                                    </Link>

                                    <Dropdown menu={{ items: blogItems }} overlayClassName={styles['blog-dropdown-overlay']}>
                                        <div className={styles['menu-item']}>
                                            Blog <DownOutlined style={{ fontSize: '10px', marginLeft: '4px' }} />
                                        </div>
                                    </Dropdown>
                                    <Link to={'/cv-template'} className={styles['menu-item']}>
                                        Mẫu CV IT
                                        <Badge 
                                            style={{ backgroundColor: '#ea1e30', marginLeft: 8, fontSize: 10, lineHeight: '14px', boxShadow: 'none' }} 
                                            size="small" 
                                        />
                                    </Link>
                                   

                                    <Link to={'/job/ai-data'} className={styles['menu-item']}>
                                        Việc làm AI & Data
                                        <Badge 
                                            count={"HOT"} 
                                            style={{ backgroundColor: '#ea1e30', marginLeft: 8, fontSize: 10, lineHeight: '14px', boxShadow: 'none' }} 
                                            size="small" 
                                        />
                                    </Link>
                                </nav>
                            </div>

                            <div className={styles['nav-right']}>
                                {isAuthenticated === false ?
                                    <div className={styles['auth-buttons']}>
                                        <Link to={'/login'} className={styles['login-link']}>Đăng Nhập / Đăng Ký</Link>
                                        <span className={styles['divider']}>|</span>
                                        <Link to={'/employer'} className={styles['employer-link']}>
                                            Nhà Tuyển Dụng
                                        </Link>
                                    </div>
                                    :
                                    <Dropdown menu={{ items: itemsDropdown }} trigger={['click']} placement="bottomRight">
                                        <div className={styles['user-profile']}>
                                       <Avatar

                                                    style={{ backgroundColor: '#ea1e30', verticalAlign: 'middle' }}

                                                    size="large">

                                                    {user?.name?.substring(0, 1)?.toUpperCase()}

                                            </Avatar>
                                            <span className={styles['username']}>{user?.name}</span>
                                            <DownOutlined style={{ fontSize: '10px', color: '#888' }} />
                                        </div>
                                    </Dropdown>
                                }
                            </div>
                        </div>
                        :
                        <div className={styles['header-mobile']}>
                            <div className={styles['brand-mobile']} onClick={() => navigate('/')}>
                                <FaReact color='#ea1e30' /> <span>FIND JOB</span>
                            </div>
                            <MenuFoldOutlined className={styles['burger-icon']} onClick={() => setOpenMobileMenu(true)} />
                        </div>
                    }
                </div>
            </div>

            <Drawer
                title="Menu"
                placement="right"
                onClose={() => setOpenMobileMenu(false)}
                open={openMobileMenu}
                width={280}
            >
                <Menu
                    onClick={(e) => {
                        setCurrent(e.key);
                        setOpenMobileMenu(false);
                    }}
                    selectedKeys={[current]}
                    mode="inline"
                    items={[...itemsMobiles, ...(isAuthenticated ? itemsDropdown : [])]}
                />
            </Drawer>

            <ManageAccount
                open={openMangeAccount}
                onClose={setOpenManageAccount}
            />
        </>
    )
};

export default Header;