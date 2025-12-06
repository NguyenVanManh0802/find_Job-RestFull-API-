import DataTable from "@/components/client/data-table";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { IJob } from "@/types/backend";
import { DeleteOutlined, EditOutlined, PlusOutlined, EyeOutlined } from "@ant-design/icons";
import { ActionType, ProColumns, ProFormSelect } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, Tag, Tooltip, message, notification } from "antd";
import { useRef, useEffect } from 'react';
import dayjs from 'dayjs';
import { callDeleteJob } from "@/config/api";
import queryString from 'query-string';
import { useNavigate } from "react-router-dom";
import { fetchJob } from "@/redux/slice/jobSlide";
import Access from "@/components/share/access";
import { ALL_PERMISSIONS } from "@/config/permissions";
import { sfIn } from "spring-filter-query-builder";
import styles from '@/styles/admin.module.scss';

const JobPage = () => {
    const tableRef = useRef<ActionType>();

    const isFetching = useAppSelector(state => state.job.isFetching);
    const meta = useAppSelector(state => state.job.meta);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    useEffect(() => {
        const query = buildQuery({ current: 1, pageSize: 10 }, null, null);
        dispatch(fetchJob({ query }));
    }, []);

    const handleDeleteJob = async (id: string | undefined) => {
        if (id) {
            const res = await callDeleteJob(id);
            if (res && res.data) {
                message.success('Xóa Job thành công');
                reloadTable();
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: res.message
                });
            }
        }
    }

    const reloadTable = () => {
        tableRef?.current?.reload();
    }

    // --- HÀM CHỌN MÀU (Đã nâng cấp bảng màu dịu mắt hơn) ---
    const getColorLevel = (level: string) => {
        switch (level) {
            case 'INTERN': return '#87d068'; // Green
            case 'FRESHER': return '#2db7f5'; // Cyan
            case 'JUNIOR': return '#108ee9'; // Blue
            case 'MIDDLE': return '#722ed1'; // Purple
            case 'SENIOR': return '#eb2f96'; // Magenta
            case 'LEAD': return '#f50';      // Orange/Red
            case 'MANAGER': return '#fa8c16'; // Sunset Orange
            default: return 'default';
        }
    }

    const columns: ProColumns<IJob>[] = [
        {
            title: 'STT',
            key: 'index',
            width: 50,
            align: "center",
            render: (text, record, index) => {
                return (
                    <span style={{ color: '#888' }}>
                        {(index + 1) + ((tableRef.current?.pageInfo?.current || 1) - 1) * (tableRef.current?.pageInfo?.pageSize || 10)}
                    </span>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Tên Job',
            dataIndex: 'name',
            sorter: true,
            render: (dom, entity) => (
                <span className={styles['job-name']} onClick={() => navigate(`/admin/job/upsert?id=${entity.id}`)}>
                    {entity.name}
                </span>
            )
        },
        {
            title: 'Công ty',
            sorter: true,
            hideInSearch: true,
            render: (dom, entity) => {
                return <span className={styles['company-name']}>{entity.company?.name}</span>
            }
        },
        {
            title: 'Mức lương',
            dataIndex: 'salary',
            sorter: true,
            render(dom, entity, index, action, schema) {
                const str = "" + entity.salary;
                return <span className={styles['salary-text']}>{str?.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} đ</span>
            },
        },
        {
            title: 'Level',
            dataIndex: 'level',
            valueType: 'select',
            valueEnum: {
                INTERN: { text: 'INTERN' },
                FRESHER: { text: 'FRESHER' },
                JUNIOR: { text: 'JUNIOR' },
                MIDDLE: { text: 'MIDDLE' },
                SENIOR: { text: 'SENIOR' },
            },
            fieldProps: {
                mode: 'multiple',
                showSearch: true,
                allowClear: true,
                placeholder: 'Lọc theo level'
            },
            render: (_, entity) => {
                if (!entity.level) return <></>;
                const levels = Array.isArray(entity.level) ? entity.level : [entity.level];
                return (
                    <Space wrap>
                        {levels.map((item: any, i: number) => (
                            <Tag 
                                color={getColorLevel(item)} 
                                key={i} 
                                className={styles['level-tag']} // Áp dụng class bo tròn
                            >
                                {item}
                            </Tag>
                        ))}
                    </Space>
                )
            },
        },
        {
            title: 'Trạng thái',
            dataIndex: 'active',
            render(dom, entity, index, action, schema) {
                return (
                    <Tag 
                        bordered={false} 
                        color={entity.active ? "success" : "error"}
                        style={{ fontWeight: 500 }}
                    >
                        {entity.active ? "ACTIVE" : "INACTIVE"}
                    </Tag>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Ngày tạo', // Đổi tên cho thân thiện
            dataIndex: 'createdAt',
            width: 150,
            sorter: true,
            render: (text, record, index, action) => {
                return <span style={{ color: '#666' }}>{record.createdAt ? dayjs(record.createdAt).format('DD/MM/YYYY') : ""}</span>
            },
            hideInSearch: true,
        },
        {
            title: 'Thao tác', // Đổi tên cho thân thiện
            hideInSearch: true,
            width: 100,
            align: 'center',
            render: (_value, entity, _index, _action) => (
                <Space size="middle">
                    <Access permission={ALL_PERMISSIONS.JOBS.UPDATE} hideChildren>
                        <Tooltip title="Chỉnh sửa">
                            <EditOutlined
                                className={styles['action-btn']}
                                style={{ fontSize: 18, color: '#ffa500', cursor: 'pointer' }}
                                onClick={() => navigate(`/admin/job/upsert?id=${entity.id}`)}
                            />
                        </Tooltip>
                    </Access>

                    <Access permission={ALL_PERMISSIONS.JOBS.DELETE} hideChildren>
                        <Popconfirm
                            placement="leftTop"
                            title={"Xác nhận xóa"}
                            description={"Bạn có chắc chắn muốn xóa job này ?"}
                            onConfirm={() => handleDeleteJob(entity.id)}
                            okText="Xóa"
                            cancelText="Hủy"
                            okButtonProps={{ danger: true }}
                        >
                            <Tooltip title="Xóa">
                                <DeleteOutlined
                                    className={styles['action-btn']}
                                    style={{ fontSize: 18, color: '#ff4d4f', cursor: 'pointer' }}
                                />
                            </Tooltip>
                        </Popconfirm>
                    </Access>
                </Space>
            ),
        },
    ];

    // ... (Hàm buildQuery giữ nguyên) ...
    const buildQuery = (params: any, sort: any, filter: any) => {
        const clone = { ...params };
        let parts = [];
        if (clone.name) parts.push(`name ~ '${clone.name}'`);
        if (clone.salary) parts.push(`salary ~ '${clone.salary}'`);
        if (clone?.level?.length) {
            parts.push(`${sfIn("level", clone.level).toString()}`);
        }

        clone.filter = parts.join(' and ');
        if (!clone.filter) delete clone.filter;

        clone.page = clone.current;
        clone.size = clone.pageSize;

        delete clone.current;
        delete clone.pageSize;
        delete clone.name;
        delete clone.salary;
        delete clone.level;

        let temp = queryString.stringify(clone);

        let sortBy = "";
        const fields = ["name", "salary", "createdAt", "updatedAt"];
        if (sort) {
            for (const field of fields) {
                if (sort[field]) {
                    sortBy = `sort=${field},${sort[field] === 'ascend' ? 'asc' : 'desc'}`;
                    break;
                }
            }
        }

        if (!sortBy) {
            temp = `${temp}&sort=updatedAt,desc`;
        } else {
            temp = `${temp}&${sortBy}`;
        }

        return temp;
    }

    return (
        <div className={styles['page-container']}> {/* Áp dụng class container mới */}
            <Access permission={ALL_PERMISSIONS.JOBS.GET_PAGINATE}>
                <DataTable<IJob>
                    actionRef={tableRef}
                    headerTitle={<span style={{ fontWeight: 'bold', fontSize: '18px', color: '#333' }}>Quản lý Công Việc</span>}
                    rowKey="id"
                    columns={columns}
                    
                    // Cấu hình search đẹp hơn
                    search={{
                        labelWidth: 'auto',
                        searchText: 'Tìm kiếm',
                        resetText: 'Làm mới',
                        // filterType: 'light', // Nếu muốn search nằm ngang trên cùng thì bỏ comment dòng này
                    }}

                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        const action = await dispatch(fetchJob({ query }));
                        if (fetchJob.fulfilled.match(action)) {
                            return {
                                data: action.payload.data?.result,
                                success: true,
                                total: action.payload.data?.meta.total
                            }
                        }
                        return { data: [], success: false };
                    }}
                    scroll={{ x: true }}
                    pagination={{
                        showSizeChanger: true,
                        showTotal: (total, range) => { return (<div> {range[0]}-{range[1]} trên {total} job</div>) }
                    }}
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => {
                        return (
                            <Button
                                icon={<PlusOutlined />}
                                type="primary"
                                style={{ borderRadius: '6px', fontWeight: 500 }}
                                onClick={() => navigate('upsert')}
                            >
                                Đăng tin mới
                            </Button>
                        );
                    }}
                />
            </Access>
        </div >
    )
}

export default JobPage;