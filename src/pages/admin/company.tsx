import DataTable from "@/components/client/data-table";
import { useAppDispatch, useAppSelector } from "@/redux/hooks";
import { ICompany } from "@/types/backend";
import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { Button, Popconfirm, Space, Tooltip, message, notification } from "antd";
import { useState, useRef } from 'react';
import dayjs from 'dayjs';
import { callDeleteCompany } from "@/config/api";
import queryString from 'query-string';
import { fetchCompany } from "@/redux/slice/companySlide";
import ModalCompany from "@/components/admin/company/modal.company";
import Access from "@/components/share/access";
import { ALL_PERMISSIONS } from "@/config/permissions";
import { sfLike } from "spring-filter-query-builder";
import styles from '@/styles/admin.module.scss'; // Tái sử dụng CSS của Job

const CompanyPage = () => {
    const [openModal, setOpenModal] = useState<boolean>(false);
    const [dataInit, setDataInit] = useState<ICompany | null>(null);
    const tableRef = useRef<ActionType>();

    const isFetching = useAppSelector(state => state.company.isFetching);
    const meta = useAppSelector(state => state.company.meta);
    const dispatch = useAppDispatch();

    const handleDeleteCompany = async (id: string | undefined) => {
        if (id) {
            const res = await callDeleteCompany(id);
            if (res && +res.statusCode === 200) {
                message.success('Xóa Company thành công');
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

    const columns: ProColumns<ICompany>[] = [
        {
            title: 'STT',
            key: 'index',
            width: 50,
            align: "center",
            render: (text, record, index) => {
                return (
                    <span style={{ color: '#888' }}>
                        {(index + 1) + (meta.page - 1) * (meta.pageSize)}
                    </span>
                )
            },
            hideInSearch: true,
        },
        {
            title: 'Tên Công Ty',
            dataIndex: 'name',
            sorter: true,
            render: (dom, entity) => (
                <span 
                    className={styles['job-name']} // Style tên đậm, hover xanh
                    onClick={() => { setOpenModal(true); setDataInit(entity); }}
                >
                    {entity.name}
                </span>
            )
        },
        {
            title: 'Địa chỉ',
            dataIndex: 'address',
            sorter: true,
            render: (text) => <span style={{ color: '#555' }}>{text}</span>
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
                <Space size="middle">
                    <Access permission={ALL_PERMISSIONS.COMPANIES.UPDATE} hideChildren>
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

                    <Access permission={ALL_PERMISSIONS.COMPANIES.DELETE} hideChildren>
                        <Popconfirm
                            placement="leftTop"
                            title={"Xác nhận xóa"}
                            description={"Bạn có chắc chắn muốn xóa company này ?"}
                            onConfirm={() => handleDeleteCompany(entity.id)}
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

        if (clone.name) q.filter = `${sfLike("name", clone.name)}`;
        if (clone.address) {
            q.filter = clone.name ? 
                q.filter + " and " + `${sfLike("address", clone.address)}` 
                : `${sfLike("address", clone.address)}`;
        }

        if (!q.filter) delete q.filter;

        let temp = queryString.stringify(q);

        let sortBy = "";
        if (sort && sort.name) sortBy = sort.name === 'ascend' ? "sort=name,asc" : "sort=name,desc";
        if (sort && sort.address) sortBy = sort.address === 'ascend' ? "sort=address,asc" : "sort=address,desc";
        if (sort && sort.createdAt) sortBy = sort.createdAt === 'ascend' ? "sort=createdAt,asc" : "sort=createdAt,desc";
        if (sort && sort.updatedAt) sortBy = sort.updatedAt === 'ascend' ? "sort=updatedAt,asc" : "sort=updatedAt,desc";

        if (!sortBy) temp = `${temp}&sort=updatedAt,desc`;
        else temp = `${temp}&${sortBy}`;

        return temp;
    }

    return (
        <div className={styles['page-container']}> {/* Thêm container style */}
            <Access permission={ALL_PERMISSIONS.COMPANIES.GET_PAGINATE}>
                <DataTable<ICompany>
                    actionRef={tableRef}
                    headerTitle={<span style={{ fontWeight: 'bold', fontSize: '18px', color: '#333' }}>Quản lý Công Ty</span>}
                    rowKey="id"
                    loading={isFetching}
                    columns={columns}
                    
                    // Cấu hình search
                    search={{
                        labelWidth: 'auto',
                        searchText: 'Tìm kiếm',
                        resetText: 'Làm mới',
                    }}

                    // SỬA LẠI REQUEST ĐỂ DỮ LIỆU LOAD ĐÚNG VÀ TẮT LOADING
                    request={async (params, sort, filter): Promise<any> => {
                        const query = buildQuery(params, sort, filter);
                        const action = await dispatch(fetchCompany({ query }));
                        
                        if (fetchCompany.fulfilled.match(action)) {
                            const res: any = action.payload;
                            const listData = res.data?.result ?? res.result ?? []; // Logic bắt dính dữ liệu
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
                        showTotal: (total, range) => { return (<div> {range[0]}-{range[1]} trên {total} công ty</div>) }
                    }}
                    rowSelection={false}
                    toolBarRender={(_action, _rows): any => {
                        return (
                            <Access permission={ALL_PERMISSIONS.COMPANIES.CREATE} hideChildren>
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
            <ModalCompany
                openModal={openModal}
                setOpenModal={setOpenModal}
                reloadTable={reloadTable}
                dataInit={dataInit}
                setDataInit={setDataInit}
            />
        </div>
    )
}

export default CompanyPage;