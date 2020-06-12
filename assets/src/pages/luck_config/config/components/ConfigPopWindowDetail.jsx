import React  from 'react';
import { connect } from 'dva';
import { formatMessage } from 'umi-plugin-react/locale';
import { Form, Input, Icon, Button, message, Tooltip, Modal } from 'antd';
import { BasicForm, BasicFormItem } from 'luck-react';
import omit from 'lodash/omit';
import PopWindowDetail from '@/components/LogPopWindow/PopWindowDetail';
import styles from '../configForm.less';

@connect(({ configPopWindow, config, loading }) => ({
  config: configPopWindow.config,
  formData: configPopWindow.formData,
  loading: loading.effects['configPopWindow/doConfig'] || loading.effects['configPopWindow/addOtherSettingInfo'],
}))
class Detail extends PopWindowDetail {

  constructor(props){
    super(props);
    this.fieldRef = React.createRef();
    this.labelRef = React.createRef();
  }

  componentDidMount() {
    const { dispatch, dataSource, type } = this.props;
    if (type === 'config') {
      dispatch({
        type: 'configPopWindow/getSettingInfo',
        payload: {
          filename: dataSource.name,
          stype: dataSource.type,
        }
      });
    }
  }

  handleAddField = ()=> {
    const newField = this.fieldRef.current.state.value;
    const newLabel = this.labelRef.current.state.value;
    if (newField && newLabel) {
      this.fieldRef.current.state.value = '';
      this.labelRef.current.state.value = '';
      const { dispatch } = this.props;
      dispatch({
        type: 'configPopWindow/setConfig',
        payload: {field: newField, label: newLabel}
      })
    } else if (!newLabel) {
      this.labelRef.current.focus();
      message.error('请输入配置域标识');
    } else if (!newField) {
      this.fieldRef.current.focus();
      message.error('请输入配置域');
    }
  };

  handleRemove = (json)=> {
    const { dispatch } = this.props;
    dispatch({
      type: 'configPopWindow/setConfig',
      payload: {field: json.field, isRemove: true}
    })
  };

  handleSubmit = (err, values, submitValues)=> {
    if (!err) {
      const { dispatch, dataSource, type } = this.props;
      Object.keys(values).forEach((key, index)=> {
        const temp = this.props.formRef.current.props.children[0].find(child=> child.props && child.props.name === key);
        if (temp) {
          values[key] = {value: values[key], label: temp.props.label.props.children, sort: index}
        }
      });
      const confirm = (data)=> {
        if (submitValues && Object.keys(submitValues).length > 0) {
          Modal.confirm({
            title: `重启${data.label}`,
            content: '配置修改成功，是否重启服务？',
            okText: '重启',
            cancelText: '取消',
            onOk: ()=> {
              dispatch({
                type: 'config/doRestart',
                payload: data
              });
            }
          })
        }
      };
      if (type === 'add') { // 添加业务服务的处理
        const filename = values['other|alias'].value;
        const name = values['other|name'].value;
        values['systeminfo|system-name'] = {value: name};
        dispatch({
          type: 'configPopWindow/addOtherSettingInfo',
          payload: {
            content: JSON.stringify({...omit(values, ['other|alias', 'other|name']), updateKeys: Object.keys(submitValues).join()}),
            sname: name,
            filename
          },
          callback: ()=>confirm({name: filename, label: name})
        });
      } else if (type === 'config') { // 一般配置的处理
        values['systeminfo|system-name'] = {value: dataSource.label};
        dispatch({
          type: 'configPopWindow/doConfig',
          payload: {
            params: {
              filename: dataSource.name,
              stype: dataSource.type,
            },
            body: {...values, updateKeys: Object.keys(submitValues).join()}
          },
          callback: ()=>confirm(dataSource),
        });
      }
    }
  };

  render() {
    const { type, config, dataSource, formData, loading } = this.props;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 5 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 12 },
        md: { span: 18 },
      },
      style:{
        width:'100%',
      },
    };
    const formItems = config.map(json => {
      if (json.defaultValue && !formData[json.field]) {
        formData[json.field] = json.defaultValue;
      }
      return (
        <BasicFormItem
          key={json.field}
          type={json.type || 'input'}
          name={json.field}
          label={
            <Tooltip title={json.field} placement="left">
              {json.label || json.field}
            </Tooltip>
          }
          layout="flex"
          config={{
            required: json.required,
            placeholder: formatMessage({ id: 'app.base.tip.input' }),
            message: formatMessage({ id: 'app.base.tip.input' }),
            addonAfter: !json.required && <Icon type="minus-circle-o" onClick={() => this.handleRemove(json)}/>
          }}
        />
      )
    });

    return this.buildForm(
      <BasicForm
        className={styles.configForm}
        handleSubmit={this.handleSubmit}
        dataSource={formData}
        loading={!!loading}
        readOnly={false}
      >
        {formItems}
        {
          (type === 'add' || (dataSource && dataSource.type !== 'basic')) && (
            <Form.Item {...formItemLayout} label="添加配置项" className={styles.addItem}>
              <div style={{display:'flex', width:'100%', alignItems:'center'}}>
                <Input placeholder="配置域标识" className={styles.fieldInput} style={{marginRight: 4}} ref={this.labelRef}/>
                <Input placeholder="配置域" className={styles.fieldInput} style={{marginRight: 4}} ref={this.fieldRef}/>
                <Button shape="circle" icon="plus" type="primary" onClick={this.handleAddField}/>
              </div>
            </Form.Item>
          )
        }
      </BasicForm>
    )
  }
}

export default Form.create({ name: 'config-form' })(Detail);