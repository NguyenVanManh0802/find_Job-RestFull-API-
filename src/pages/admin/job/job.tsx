import DataTable from "@/components/client/data-table";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { IJob } from "@/types/backend";
import { DeleteOutlined, EditOutlined, PlusOutlined, CheckCircleOutlined, CloseCircleOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
// 1. Thêm import Alert
import { Button, Popconfirm, Space, Tag, Tooltip, message, notification, Alert } from "antd";
import { useRef, useState, useEffect } from 'react';
import dayjs from 'dayjs';
// 2. Thêm import callFetchJob
import { callDeleteJob, callUpdateJob, callFetchJob } from "@/config/api"; 
import queryString from 'query-string';
import { useNavigate } from "react-router-dom";
import { fetchJob } from "@/redux/slice/jobSlide";
import Access from "@/components/share/access";
import { ALL_PERMISSIONS } from "@/config/permissions";
import { sfIn } from "spring-filter-query-builder";
import styles from '@/styles/admin.module.scss';

const JobPage = () => {
    const tableRef = useRef<ActionType>();
    
    // 3. State lưu số lượng chờ duyệt
    const [pendingJobCount, setPendingJobCount] = useState<number>(0);

    const isFetching = useAppSelector(state => state.job.isFetching);
    const meta = useAppSelector(state => state.job.meta);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const user = useAppSelector(state => state.account.user);
    const isSuperAdmin = user?.role?.name === "SUPER_ADMIN";

    useEffect(() => {
        // a. Load dữ liệu bảng (Logic cũ)
        const query = buildQuery({ current: 1, pageSize: 10 }, null, null);
        dispatch(fetchJob({ query }));

        // b. --- LOGIC MỚI: Đếm số Job đang chờ duyệt ---
        // Chỉ Admin mới cần xem thông báo này
        if (isSuperAdmin) {
            const fetchPendingCount = async () => {
                // Filter: active = false
                // PageSize = 1 (Tối ưu: chỉ cần lấy field meta.total, không cần lấy data list)
                const queryPending = `current=1&pageSize=1&filter=active:false`;
                
                const res = await callFetchJob(queryPending);
                if (res && res.data && res.data.meta) {
                    setPendingJobCount(res.data.meta.total);
                }
            }
            fetchPendingCount();
        }
        // -----------------------------------------------
        
    }, [isSuperAdmin]); // Thêm dependency


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

    const handleApproveJob = async (job: IJob) => {
        if (!job.id) return;
        const updatedJob = { ...job, active: true };
        const res = await callUpdateJob(updatedJob, job.id);
        
        if (res && res.data) {
            message.success('Duyệt bài đăng thành công!');
            reloadTable();
            
            // Cập nhật lại số lượng chờ duyệt sau khi duyệt xong
            if(pendingJobCount > 0) setPendingJobCount(prev => prev - 1);
        } else {
            notification.error({
                message: 'Có lỗi xảy ra',
                description: res.message
            });
        }
    }

    const reloadTable = () => {
        tableRef?.current?.reload();
    }

    const getColorLevel = (level: string) => {
        switch (level) {
            case 'INTERN': return '#87d068';
            case 'FRESHER': return '#2db7f5';
            case 'JUNIOR': return '#108ee9';
            case 'MIDDLE': return '#722ed1';
            case 'SENIOR': return '#eb2f96';
            case 'LEAD': return '#f50';
            case 'MANAGER': return '#fa8c16';
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
                                className={styles['level-tag']}
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
                    <>
                        {entity.active ? 
                            <Tag color="success" icon={<CheckCircleOutlined />}>Đã duyệt</Tag> : 
                            <Tag color="error" icon={<CloseCircleOutlined />}>Chờ duyệt</Tag>
                        }
                    </>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Ngày tạo',
            dataIndex: 'createdAt',
            width: 150,
            sorter: true,
            render: (text, record, index, action) => {
                return <span style={{ color: '#666' }}>{record.createdAt ? dayjs(record.createdAt).format('DD/MM/YYYY') : ""}</span>
            },
            hideInSearch: true,
        },
        {
            title: 'Thao tác',
            hideInSearch: true,
            width: 150,
            align: 'center',
            render: (_value, entity, _index, _action) => (
                <Space size="small">
                    {entity.active === false && isSuperAdmin && (
                        <Access permission={ALL_PERMISSIONS.JOBS.UPDATE} hideChildren>
                            <Popconfirm
                                title="Duyệt bài đăng này?"
                                description="Bài đăng sẽ được hiển thị công khai cho ứng viên."
                                onConfirm={() => handleApproveJob(entity)}
                                okText="Duyệt ngay"
                                cancelText="Hủy"
                            >
                                <Tooltip title="Duyệt bài đăng">
                                    <Button 
                                        type="primary" 
                                        size="small" 
                                        icon={<CheckCircleOutlined />}
                                        style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
                                    />
                                </Tooltip>
                            </Popconfirm>
                        </Access>
                    )}
                    
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
                            onConfirm={() => handleDeleteJob(entity.id as string)}
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
        <div className={styles['page-container']}>
            
            {/* 4. HIỂN THỊ ALERT THÔNG BÁO */}
            {isSuperAdmin && pendingJobCount > 0 && (
                <div style={{ marginBottom: 20 }}>
                    <Alert
                        message="Cần duyệt bài đăng"
                        description={
                            <span>
                                Hiện đang có <b>{pendingJobCount} tin tuyển dụng</b> mới đang chờ duyệt. 
                                Vui lòng kiểm tra và kích hoạt để hiển thị lên trang chủ.
                            </span>
                        }
                        type="warning"
                        showIcon
                        closable
                    />
                </div>
            )}
            {/* ------------------------------------- */}

            <Access permission={ALL_PERMISSIONS.JOBS.GET_PAGINATE}>
                <DataTable<IJob>
                    actionRef={tableRef}
                    headerTitle={<span style={{ fontWeight: 'bold', fontSize: '18px', color: '#333' }}>Quản lý Công Việc</span>}
                    rowKey="id"
                    columns={columns}
                    search={{
                        labelWidth: 'auto',
                        searchText: 'Tìm kiếm',
                        resetText: 'Làm mới',
                    }}
                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        const action = await dispatch(fetchJob({ query }));
                        if (fetchJob.fulfilled.match(action)) {
                            const res: any = action.payload;
                            const listData = res.data?.result ?? res.result ?? [];
                            const total = res.data?.meta?.total ?? res.meta?.total ?? 0;

                            return {
                                data: listData,
                                success: true,
                                total: total
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