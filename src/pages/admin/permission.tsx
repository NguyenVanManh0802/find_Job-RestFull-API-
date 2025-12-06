import DataTable from "@/components/client/data-table";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { IPermission } from "@/types/backend";
import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, Tag, Tooltip, message, notification } from "antd";
import { useState, useRef } from 'react';
import dayjs from 'dayjs';
import { callDeletePermission } from "@/config/api";
import queryString from 'query-string';
import { fetchPermission } from "@/redux/slice/permissionSlide";
import ViewDetailPermission from "@/components/admin/permission/view.permission";
import ModalPermission from "@/components/admin/permission/modal.permission";
import { colorMethod } from "@/config/utils";
import Access from "@/components/share/access";
import { ALL_PERMISSIONS } from "@/config/permissions";
import styles from '@/styles/admin.module.scss'; // Tái sử dụng style

const PermissionPage = () => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const [dataInit, setDataInit] = useState<IPermission | null>(null);
    const [openViewDetail, setOpenViewDetail] = useState<boolean>(false);

    const tableRef = useRef<ActionType>();

    const isFetching = useAppSelector(state => state.permission.isFetching);
    const meta = useAppSelector(state => state.permission.meta);
    const dispatch = useAppDispatch();

    const handleDeletePermission = async (id: string | undefined) => {
        if (id) {
            const res = await callDeletePermission(id);
            if (res && res.statusCode === 200) {
                message.success('Xóa Permission thành công');
                reloadTable();
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: res.error
                });
            }
        }
    }

    const reloadTable = () => {
        tableRef?.current?.reload();
    }

    const columns: ProColumns<IPermission>[] = [
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
            title: 'Name',
            dataIndex: 'name',
            sorter: true,
            render: (dom, entity) => (
                <span 
                    className={styles['job-name']}
                    onClick={() => { setOpenViewDetail(true); setDataInit(entity); }}
                >
                    {entity.name}
                </span>
            )
        },
        {
            title: 'API',
            dataIndex: 'apiPath',
            sorter: true,
            render: (text) => <span style={{fontFamily: 'monospace', color: '#d63384'}}>{text}</span>
        },
        {
            title: 'Method',
            dataIndex: 'method',
            sorter: true,
            render(dom, entity, index, action, schema) {
                return (
                    <Tag color={colorMethod(entity?.method as string)} style={{ fontWeight: 'bold' }}>
                        {entity?.method || ''}
                    </Tag>
                )
            },
        },
        {
            title: 'Module',
            dataIndex: 'module',
            sorter: true,
            // --- CẤU HÌNH MÀU SẮC CHO MODULE ---
            render: (dom, entity) => {
                let color = 'default';
                switch(entity.module) {
                    case 'USERS': color = 'blue'; break;
                    case 'ROLES': color = 'cyan'; break;
                    case 'PERMISSIONS': color = 'purple'; break;
                    case 'JOBS': color = 'orange'; break;
                    case 'COMPANIES': color = 'magenta'; break;
                    case 'RESUMES': color = 'geekblue'; break;
                    case 'FILES': color = 'green'; break;
                    case 'SKILLS': color = 'lime'; break; // Màu xanh nõn chuối cho Skills
                    default: color = 'default';
                }
                return <Tag color={color}>{entity.module}</Tag>
            }
            // -----------------------------------
        },
        {
            title: 'CreatedAt',
            dataIndex: 'createdAt',
            width: 150,
            sorter: true,
            render: (text, record, index, action) => {
                return <span style={{ color: '#888' }}>{record.createdAt ? dayjs(record.createdAt).format('DD/MM/YYYY HH:mm') : ""}</span>
            },
            hideInSearch: true,
        },
        {
            title: 'UpdatedAt',
            dataIndex: 'updatedAt',
            width: 150,
            sorter: true,
            render: (text, record, index, action) => {
                return <span style={{ color: '#888' }}>{record.updatedAt ? dayjs(record.updatedAt).format('DD/MM/YYYY HH:mm') : ""}</span>
            },
            hideInSearch: true,
        },
        {
            title: 'Actions',
            hideInSearch: true,
            width: 100,
            align: 'center',
            render: (_value, entity, _index, _action) => (
                <Space size="middle">
                    <Access permission={ALL_PERMISSIONS.PERMISSIONS.UPDATE} hideChildren>
                        <Tooltip title="Chỉnh sửa">
                            <EditOutlined
                                className={styles['action-btn']}
                                style={{ fontSize: 18, color: '#ffa500', cursor: 'pointer' }}
                                onClick={() => {
                                    setOpenModal(true);
                                    setDataInit(entity);
                                }}
                            />
                        </Tooltip>
                    </Access>
                    <Access permission={ALL_PERMISSIONS.PERMISSIONS.DELETE} hideChildren>
                        <Popconfirm
                            placement="leftTop"
                            title={"Xác nhận xóa"}
                            description={"Bạn có chắc chắn muốn xóa permission này ?"}
                            onConfirm={() => handleDeletePermission(entity.id)}
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
        const q: any = {
            page: params.current,
            size: params.pageSize,
            filter: ""
        }

        // Logic filter (giữ nguyên của bạn)
        let parts = [];
        if (clone.name) parts.push(`name ~ '${clone.name}'`);
        if (clone.apiPath) parts.push(`apiPath ~ '${clone.apiPath}'`);
        if (clone.method) parts.push(`method ~ '${clone.method}'`);
        if (clone.module) parts.push(`module ~ '${clone.module}'`);

        q.filter = parts.join(' and ');
        if (!q.filter) delete q.filter;

        let temp = queryString.stringify(q);

        let sortBy = "";
        const fields = ["name", "apiPath", "method", "module", "createdAt", "updatedAt"];

        if (sort) {
            for (const field of fields) {
                if (sort[field]) {
                    sortBy = `sort=${field},${sort[field] === 'ascend' ? 'asc' : 'desc'}`;
                    break;
                }
            }
        }

        if (Object.keys(sortBy).length === 0) {
            temp = `${temp}&sort=updatedAt,desc`;
        } else {
            temp = `${temp}&${sortBy}`;
        }

        return temp;
    }

    return (
        <div className={styles['page-container']}>
            <Access permission={ALL_PERMISSIONS.PERMISSIONS.GET_PAGINATE}>
                <DataTable<IPermission>
                    actionRef={tableRef}
                    headerTitle={<span style={{ fontWeight: 'bold', fontSize: '18px', color: '#333' }}>Quản lý Quyền Hạn (Permissions)</span>}
                    rowKey="id"
                    loading={isFetching}
                    columns={columns}
                    
                    // Cấu hình search
                    search={{
                        labelWidth: 'auto',
                        searchText: 'Tìm kiếm',
                        resetText: 'Làm mới',
                    }}

                    // FIX: request trả về data
                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        const action = await dispatch(fetchPermission({ query }));
                        
                        if (fetchPermission.fulfilled.match(action)) {
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
                        showTotal: (total, range) => { return (<div> {range[0]}-{range[1]} trên {total} permissions</div>) }
                    }}
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => {
                        return (
                            <Access permission={ALL_PERMISSIONS.PERMISSIONS.CREATE} hideChildren>
                                <Button
                                    icon={<PlusOutlined />}
                                    type="primary"
                                    style={{ borderRadius: '6px', fontWeight: 500 }}
                                    onClick={() => { setOpenModal(true); setDataInit(null); }}
                                >
                                    Thêm mới
                                </Button>
                            </Access>
                        );
                    }}
                />
            </Access>
            <ModalPermission
                openModal={openModal}
                setOpenModal={setOpenModal}
                reloadTable={reloadTable}
                dataInit={dataInit}
                setDataInit={setDataInit}
            />

            <ViewDetailPermission
                onClose={setOpenViewDetail}
                open={openViewDetail}
                dataInit={dataInit}
                setDataInit={setDataInit}
            />
        </div>
    )
}

export default PermissionPage;