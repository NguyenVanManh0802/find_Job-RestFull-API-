import { Breadcrumb, Col, ConfigProvider, Divider, Form, Row, message, notification } from "antd";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { DebounceSelect } from "../user/debouce.select";
import { FooterToolbar, ProForm, ProFormDatePicker, ProFormDigit, ProFormSelect, ProFormSwitch, ProFormText } from "@ant-design/pro-components";
import styles from 'styles/admin.module.scss';
import { LOCATION_LIST } from "@/config/utils";
import { ICompanySelect } from "../user/modal.user";
import { useState, useEffect } from 'react';
import { callCreateJob, callFetchAllSkill, callFetchCompany, callFetchJobById, callUpdateJob } from "@/config/api";
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import { CheckSquareOutlined } from "@ant-design/icons";
import enUS from 'antd/lib/locale/en_US';
import dayjs from 'dayjs';
import { IJob, ISkill } from "@/types/backend";

interface ISkillSelect {
    label: string;
    value: string;
    key?: string;
}

const ViewUpsertJob = (props: any) => {
    const [companies, setCompanies] = useState<ICompanySelect[]>([]);
    const [skills, setSkills] = useState<ISkillSelect[]>([]);

    const navigate = useNavigate();
    const [value, setValue] = useState<string>("");

    let location = useLocation();
    let params = new URLSearchParams(location.search);
    const id = params?.get("id"); // job id
    const [dataUpdate, setDataUpdate] = useState<IJob | null>(null);
    const [form] = Form.useForm();

    useEffect(() => {
        const init = async () => {
            const temp = await fetchSkillList();
            setSkills(temp);

            if (id) {
                const res = await callFetchJobById(id);
                if (res && res.data) {
                    setDataUpdate(res.data);
                    setValue(res.data.description);
                    
                    if (res.data.company) {
                        setCompanies([
                            {
                                label: res.data.company.name as string,
                                value: `${res.data.company.id}@#$${res.data.company.logo}` as string,
                                key: `${res.data.company.id}`
                            }
                        ])
                    }

                    const skillInit = res.data.skills?.map((item: ISkill) => {
                        return {
                            label: item.name,
                            value: item.id,
                            key: item.id
                        }
                    })

                    form.setFieldsValue({
                        ...res.data,
                        company: res.data.company ? {
                            label: res.data.company.name as string,
                            value: `${res.data.company.id}@#$${res.data.company.logo}` as string,
                            key: `${res.data.company.id}`
                        } : undefined,
                        skills: skillInit,
                        startDate: res.data.startDate ? dayjs(res.data.startDate) : undefined,
                        endDate: res.data.endDate ? dayjs(res.data.endDate) : undefined,
                    })
                }
            }
        }
        init();
        return () => form.resetFields()
    }, [id])

    async function fetchCompanyList(name: string): Promise<ICompanySelect[]> {
        const res = await callFetchCompany(`page=1&size=100&name ~ '${name}'`);
        if (res && res.data) {
            const list = res.data.result;
            const temp = list.map(item => {
                return {
                    label: item.name as string,
                    value: `${item.id}@#$${item.logo}` as string
                }
            })
            return temp;
        } else return [];
    }

    async function fetchSkillList(): Promise<ISkillSelect[]> {
        const res = await callFetchAllSkill(`page=1&size=100`);
        let rawData = [];
        if ((res as any)?.result) {
             rawData = (res as any).result;
        } else if (res?.data?.result) {
             rawData = res.data.result;
        }

        if (rawData.length > 0) {
            const temp = rawData.map((item: any) => {
                return {
                    label: item.name as string,
                    value: item.id as string, 
                    key: item.id
                }
            })
            return temp;
        } 
        return [];
    }

    const onFinish = async (values: any) => {
        let companyId = "";
        let companyLogo = "";
        
        if (values.company) {
            const rawVal = values.company.value ?? values.company;
            if (typeof rawVal === 'string' && rawVal.includes('@#$')) {
                const split = rawVal.split('@#$');
                companyId = split[0];
                companyLogo = split[1];
            } else {
                companyId = rawVal;
            }
        }

        const arrSkills = values.skills?.map((skill: any) => {
            if (typeof skill === 'object' && skill.value) {
                return { id: +skill.value }; 
            }
            return { id: +skill }; 
        }) || [];

        const job = {
            name: values.name,
            skills: arrSkills,
            company: {
                id: +companyId, 
                name: values.company.label ?? values.company.name,
                logo: companyLogo
            },
            location: values.location,
            salary: values.salary,
            quantity: values.quantity,
            level: values.level,
            description: value,
            startDate: dayjs(values.startDate).toDate(),
            endDate: dayjs(values.endDate).toDate(),
            active: values.active
        };

        if (dataUpdate?.id) {
            // --- FIX: Ép kiểu (as any) để tránh lỗi TypeScript ---
            const res = await callUpdateJob(job as any, dataUpdate.id);
            if (res.data) {
                message.success("Cập nhật job thành công");
                navigate('/admin/job')
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: res.message
                });
            }
        } else {
            // --- FIX: Ép kiểu (as any) để tránh lỗi TypeScript ---
            const res = await callCreateJob(job as any);
            if (res.data) {
                message.success("Tạo mới job thành công");
                navigate('/admin/job')
            } else {
                notification.error({
                    message: 'Có lỗi xảy ra',
                    description: res.message
                });
            }
        }
    }

    return (
        <div className={styles["upsert-job-container"]}>
            <div className={styles["title"]}>
                <Breadcrumb
                    separator=">"
                    items={[
                        {
                            title: <Link to="/admin/job">Manage Job</Link>,
                        },
                        {
                            title: 'Upsert Job',
                        },
                    ]}
                />
            </div>
            <div >
                <ConfigProvider locale={enUS}>
                    <ProForm
                        form={form}
                        onFinish={onFinish}
                        submitter={
                            {
                                searchConfig: {
                                    resetText: "Hủy",
                                    submitText: <>{dataUpdate?.id ? "Cập nhật Job" : "Tạo mới Job"}</>
                                },
                                onReset: () => navigate('/admin/job'),
                                render: (_: any, dom: any) => <FooterToolbar>{dom}</FooterToolbar>,
                                submitButtonProps: {
                                    icon: <CheckSquareOutlined />
                                },
                            }
                        }
                    >
                        <Row gutter={[20, 20]}>
                            <Col span={24} md={12}>
                                <ProFormText
                                    label="Tên Job"
                                    name="name"
                                    rules={[{ required: true, message: 'Vui lòng không bỏ trống' }]}
                                    placeholder="Nhập tên job"
                                />
                            </Col>
                            <Col span={24} md={6}>
                                <ProFormSelect
                                    name="skills"
                                    label="Kỹ năng yêu cầu"
                                    options={skills}
                                    placeholder="Please select a skill"
                                    rules={[{ required: true, message: 'Vui lòng chọn kỹ năng!' }]}
                                    allowClear
                                    mode="multiple"
                                    fieldProps={{ suffixIcon: null }}
                                />
                            </Col>
                            <Col span={24} md={6}>
                                <ProFormSelect
                                    name="location"
                                    label="Địa điểm"
                                    options={LOCATION_LIST.filter(item => item.value !== 'ALL')}
                                    placeholder="Please select a location"
                                    rules={[{ required: true, message: 'Vui lòng chọn địa điểm!' }]}
                                />
                            </Col>
                            <Col span={24} md={6}>
                                <ProFormDigit
                                    label="Mức lương"
                                    name="salary"
                                    rules={[{ required: true, message: 'Vui lòng không bỏ trống' }]}
                                    placeholder="Nhập mức lương"
                                    fieldProps={{
                                        addonAfter: " đ",
                                        formatter: (value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ','),
                                        parser: (value) => +(value || '').replace(/\$\s?|(,*)/g, '')
                                    }}
                                />
                            </Col>
                            <Col span={24} md={6}>
                                <ProFormDigit
                                    label="Số lượng"
                                    name="quantity"
                                    rules={[{ required: true, message: 'Vui lòng không bỏ trống' }]}
                                    placeholder="Nhập số lượng"
                                />
                            </Col>
                            <Col span={24} md={6}>
                                <ProFormSelect
                                    name="level"
                                    label="Trình độ"
                                    valueEnum={{
                                        INTERN: 'INTERN',
                                        FRESHER: 'FRESHER',
                                        JUNIOR: 'JUNIOR',
                                        MIDDLE: 'MIDDLE',
                                        SENIOR: 'SENIOR',
                                    }}
                                    placeholder="Please select a level"
                                    rules={[{ required: true, message: 'Vui lòng chọn level!' }]}
                                />
                            </Col>

                            {(dataUpdate?.id || !id) &&
                                <Col span={24} md={6}>
                                    <ProForm.Item
                                        name="company"
                                        label="Thuộc Công Ty"
                                        rules={[{ required: true, message: 'Vui lòng chọn company!' }]}
                                    >
                                        <DebounceSelect
                                            allowClear
                                            showSearch
                                            defaultValue={companies}
                                            value={companies}
                                            placeholder="Chọn công ty"
                                            fetchOptions={fetchCompanyList}
                                            onChange={(newValue: any) => {
                                                if (newValue?.length === 0 || newValue?.length === 1) {
                                                    setCompanies(newValue as ICompanySelect[]);
                                                }
                                            }}
                                            style={{ width: '100%' }}
                                        />
                                    </ProForm.Item>
                                </Col>
                            }
                        </Row>
                        <Row gutter={[20, 20]}>
                            <Col span={24} md={6}>
                                <ProFormDatePicker
                                    label="Ngày bắt đầu"
                                    name="startDate"
                                    normalize={(value) => value && dayjs(value, 'DD/MM/YYYY')}
                                    fieldProps={{ format: 'DD/MM/YYYY' }}
                                    rules={[{ required: true, message: 'Vui lòng chọn ngày cấp' }]}
                                    placeholder="dd/mm/yyyy"
                                />
                            </Col>
                            <Col span={24} md={6}>
                                <ProFormDatePicker
                                    label="Ngày kết thúc"
                                    name="endDate"
                                    normalize={(value) => value && dayjs(value, 'DD/MM/YYYY')}
                                    fieldProps={{ format: 'DD/MM/YYYY' }}
                                    rules={[{ required: true, message: 'Vui lòng chọn ngày cấp' }]}
                                    placeholder="dd/mm/yyyy"
                                />
                            </Col>
                            <Col span={24} md={6}>
                                <ProFormSwitch
                                    label="Trạng thái"
                                    name="active"
                                    checkedChildren="ACTIVE"
                                    unCheckedChildren="INACTIVE"
                                    initialValue={true}
                                    fieldProps={{ defaultChecked: true }}
                                />
                            </Col>
                            <Col span={24}>
                                <ProForm.Item
                                    name="description"
                                    label="Miêu tả job"
                                    rules={[{ required: true, message: 'Vui lòng nhập miêu tả job!' }]}
                                >
                                    <ReactQuill
                                        theme="snow"
                                        value={value}
                                        onChange={setValue}
                                    />
                                </ProForm.Item>
                            </Col>
                        </Row>
                        <Divider />
                    </ProForm>
                </ConfigProvider>
            </div>
        </div>
    )
}

export default ViewUpsertJob;