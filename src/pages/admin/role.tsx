import DataTable from "@/components/client/data-table";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { IRole } from "@/types/backend";
import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, Tag, Tooltip, message, notification } from "antd";
import { useState, useRef } from 'react';
import dayjs from 'dayjs';
import { callDeleteRole } from "@/config/api";
import queryString from 'query-string';
import { fetchRole, fetchRoleById } from "@/redux/slice/roleSlide";
import ModalRole from "@/components/admin/role/modal.role";
import { ALL_PERMISSIONS } from "@/config/permissions";
import Access from "@/components/share/access";
import { sfLike } from "spring-filter-query-builder";
import styles from '@/styles/admin.module.scss'; // Tái sử dụng Style

const RolePage = () => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const tableRef = useRef<ActionType>();

    const isFetching = useAppSelector(state => state.role.isFetching);
    const meta = useAppSelector(state => state.role.meta);
    const dispatch = useAppDispatch();

    const handleDeleteRole = async (id: string | undefined) => {
        if (id) {
            const res = await callDeleteRole(id);
            if (res && +res.statusCode === 200) {
                message.success('Xóa Role thành công');
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

    const columns: ProColumns<IRole>[] = [
        {
            title: 'Id',
            dataIndex: 'id',
            width: 50,
            render: (text, record, index, action) => {
                return (
                    <span>{record.id}</span>
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
                    onClick={() => {
                        dispatch(fetchRoleById((entity.id) as string));
                        setOpenModal(true);
                    }}
                >
                    {entity.name}
                </span>
            )
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
            title: 'Ngày tạo',
            dataIndex: 'createdAt',
            width: 200,
            sorter: true,
            render: (text, record, index, action) => {
                return <span style={{ color: '#666' }}>{record.createdAt ? dayjs(record.createdAt).format('DD/MM/YYYY HH:mm') : ""}</span>
            },
            hideInSearch: true,
        },
        {
            title: 'Ngày cập nhật',
            dataIndex: 'updatedAt',
            width: 200,
            sorter: true,
            render: (text, record, index, action) => {
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
                <Space size="middle">
                    <Access permission={ALL_PERMISSIONS.ROLES.UPDATE} hideChildren>
                        <Tooltip title="Chỉnh sửa">
                            <EditOutlined
                                className={styles['action-btn']}
                                style={{ fontSize: 18, color: '#ffa500', cursor: 'pointer' }}
                                onClick={() => {
                                    dispatch(fetchRoleById((entity.id) as string));
                                    setOpenModal(true);
                                }}
                            />
                        </Tooltip>
                    </Access>

                    <Access permission={ALL_PERMISSIONS.ROLES.DELETE} hideChildren>
                        <Popconfirm
                            placement="leftTop"
                            title={"Xác nhận xóa"}
                            description={"Bạn có chắc chắn muốn xóa role này ?"}
                            onConfirm={() => handleDeleteRole(entity.id)}
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
        const q: any = {
            page: params.current,
            size: params.pageSize,
            filter: ""
        }
        const clone = { ...params };
        if (clone.name) q.filter = `${sfLike("name", clone.name)}`;
        if (!q.filter) delete q.filter;
        let temp = queryString.stringify(q);

        let sortBy = "";
        if (sort && sort.name) sortBy = sort.name === 'ascend' ? "sort=name,asc" : "sort=name,desc";
        if (sort && sort.createdAt) sortBy = sort.createdAt === 'ascend' ? "sort=createdAt,asc" : "sort=createdAt,desc";
        if (sort && sort.updatedAt) sortBy = sort.updatedAt === 'ascend' ? "sort=updatedAt,asc" : "sort=updatedAt,desc";

        if (!sortBy) temp = `${temp}&sort=updatedAt,desc`;
        else temp = `${temp}&${sortBy}`;

        return temp;
    }

    return (
        <div className={styles['page-container']}>
            <Access permission={ALL_PERMISSIONS.ROLES.GET_PAGINATE}>
                <DataTable<IRole>
                    actionRef={tableRef}
                    headerTitle={<span style={{ fontWeight: 'bold', fontSize: '18px', color: '#333' }}>Quản lý Vai Trò (Roles)</span>}
                    rowKey="id"
                    loading={isFetching}
                    columns={columns}
                    
                    // Cấu hình search
                    search={{
                        labelWidth: 'auto',
                        searchText: 'Tìm kiếm',
                        resetText: 'Làm mới',
                    }}

                    // FIX Request
                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        const action = await dispatch(fetchRole({ query }));
                        
                        if (fetchRole.fulfilled.match(action)) {
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
                        showTotal: (total, range) => { return (<div> {range[0]}-{range[1]} trên {total} roles</div>) }
                    }}
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => {
                        return (
                            <Access permission={ALL_PERMISSIONS.ROLES.CREATE} hideChildren>
                                <Button
                                    icon={<PlusOutlined />}
                                    type="primary"
                                    style={{ borderRadius: '6px', fontWeight: 500 }}
                                    onClick={() => setOpenModal(true)}
                                >
                                    Thêm mới
                                </Button>
                            </Access>
                        );
                    }}
                />
            </Access>
            <ModalRole
                openModal={openModal}
                setOpenModal={setOpenModal}
                reloadTable={reloadTable}
            />
        </div>
    )
}

export default RolePage;