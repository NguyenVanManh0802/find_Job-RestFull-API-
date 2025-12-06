import DataTable from "@/components/client/data-table";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { IResume } from "@/types/backend";
import { ActionType, ProColumns, ProFormSelect } from '@ant-design/pro-components';
import { Space, Tag, Tooltip, message, notification } from "antd";
import { useState, useRef } from 'react';
import dayjs from 'dayjs';
import { callDeleteResume } from "@/config/api";
import queryString from 'query-string';
import { fetchResume } from "@/redux/slice/resumeSlide";
import ViewDetailResume from "@/components/admin/resume/view.resume";
import { ALL_PERMISSIONS } from "@/config/permissions";
import Access from "@/components/share/access";
import { sfIn } from "spring-filter-query-builder";
import { EditOutlined } from "@ant-design/icons";
import styles from '@/styles/admin.module.scss'; // Tái sử dụng style hiện đại

const ResumePage = () => {
    const tableRef = useRef<ActionType>();

    const isFetching = useAppSelector(state => state.resume.isFetching);
    const meta = useAppSelector(state => state.resume.meta);
    const dispatch = useAppDispatch();

    const [dataInit, setDataInit] = useState<IResume | null>(null);
    const [openViewDetail, setOpenViewDetail] = useState<boolean>(false);

    const handleDeleteResume = async (id: string | undefined) => {
        if (id) {
            const res = await callDeleteResume(id);
            if (res && res.data) {
                message.success('Xóa Resume thành công');
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

    const columns: ProColumns<IResume>[] = [
        {
            title: 'Id',
            dataIndex: 'id',
            width: 50,
            render: (text, record, index, action) => {
                return (
                    <a href="#" onClick={() => {
                        setOpenViewDetail(true);
                        setDataInit(record);
                    }}>
                        {record.id}
                    </a>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Trạng Thái',
            dataIndex: 'status',
            sorter: true,
            // Render Tag màu cho trạng thái
            render: (_, entity) => {
                let color = 'default';
                if (entity.status === 'PENDING') color = 'orange';
                else if (entity.status === 'REVIEWING') color = 'blue';
                else if (entity.status === 'APPROVED') color = 'green';
                else if (entity.status === 'REJECTED') color = 'red';
                
                return (
                    <Tag color={color} style={{fontWeight: 500}}>
                        {entity.status}
                    </Tag>
                );
            },
            renderFormItem: (item, props, form) => (
                <ProFormSelect
                    showSearch
                    mode="multiple"
                    allowClear
                    valueEnum={{
                        PENDING: 'PENDING',
                        REVIEWING: 'REVIEWING',
                        APPROVED: 'APPROVED',
                        REJECTED: 'REJECTED',
                    }}
                    placeholder="Chọn trạng thái"
                />
            ),
        },

        {
            title: 'Job',
            dataIndex: ["job", "name"],
            hideInSearch: true,
            render: (dom, entity) => <span className={styles['job-name']}>{entity.job?.name}</span>
        },
        {
            title: 'Company',
            dataIndex: "companyName",
            hideInSearch: true,
            render: (dom, entity) => <span style={{fontWeight: 500, color: '#4a5568'}}>{entity.companyName}</span>
        },

        {
            title: 'Ngày tạo',
            dataIndex: 'createdAt',
            width: 150,
            sorter: true,
            render: (text, record) => {
                return <span style={{ color: '#666' }}>{record.createdAt ? dayjs(record.createdAt).format('DD/MM/YYYY HH:mm') : ""}</span>
            },
            hideInSearch: true,
        },
        {
            title: 'Ngày cập nhật',
            dataIndex: 'updatedAt',
            width: 150,
            sorter: true,
            render: (text, record) => {
                return <span style={{ color: '#666' }}>{record.updatedAt ? dayjs(record.updatedAt).format('DD/MM/YYYY HH:mm') : ""}</span>
            },
            hideInSearch: true,
        },
        {
            title: 'Thao tác',
            hideInSearch: true,
            width: 100,
            align: 'center',
            render: (_value, entity, _index, _action) => (
                <Space>
                   <Access permission={ALL_PERMISSIONS.RESUMES.UPDATE} hideChildren>
                        <Tooltip title="Chỉnh sửa trạng thái">
                            <EditOutlined
                                className={styles['action-btn']}
                                style={{
                                    fontSize: 20,
                                    color: '#ffa500',
                                    cursor: 'pointer'
                                }}
                                onClick={() => {
                                    setOpenViewDetail(true);
                                    setDataInit(entity);
                                }}
                            />
                        </Tooltip>
                   </Access>
                </Space>
            ),
        },
    ];

    const buildQuery = (params: any, sort: any, filter: any) => {
        const clone = { ...params };

        if (clone?.status?.length) {
            clone.filter = sfIn("status", clone.status).toString();
            delete clone.status;
        }

        clone.page = clone.current;
        clone.size = clone.pageSize;

        delete clone.current;
        delete clone.pageSize;

        let temp = queryString.stringify(clone);

        let sortBy = "";
        if (sort && sort.status) sortBy = sort.status === 'ascend' ? "sort=status,asc" : "sort=status,desc";
        if (sort && sort.createdAt) sortBy = sort.createdAt === 'ascend' ? "sort=createdAt,asc" : "sort=createdAt,desc";
        if (sort && sort.updatedAt) sortBy = sort.updatedAt === 'ascend' ? "sort=updatedAt,asc" : "sort=updatedAt,desc";

        if (!sortBy) temp = `${temp}&sort=updatedAt,desc`;
        else temp = `${temp}&${sortBy}`;

        return temp;
    }

    return (
        <div className={styles['page-container']}> {/* Container Style */}
            <Access permission={ALL_PERMISSIONS.RESUMES.GET_PAGINATE}>
                <DataTable<IResume>
                    actionRef={tableRef}
                    headerTitle={<span style={{ fontWeight: 'bold', fontSize: '18px', color: '#333' }}>Quản lý Hồ Sơ (Resumes)</span>}
                    rowKey="id"
                    loading={isFetching}
                    columns={columns}
                    
                    // Cấu hình search
                    search={{
                        labelWidth: 'auto',
                        searchText: 'Tìm kiếm',
                        resetText: 'Làm mới',
                    }}

                    // FIX: request trả về data trực tiếp
                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        const action = await dispatch(fetchResume({ query }));
                        
                        if (fetchResume.fulfilled.match(action)) {
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
                        showTotal: (total, range) => { return (<div> {range[0]}-{range[1]} trên {total} resumes</div>) }
                    }}
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => {
                        return (<></>);
                    }}
                />
            </Access>
            <ViewDetailResume
                open={openViewDetail}
                onClose={setOpenViewDetail}
                dataInit={dataInit}
                setDataInit={setDataInit}
                reloadTable={reloadTable}
            />
        </div>
    )
}

export default ResumePage;