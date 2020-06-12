import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Box } from 'luck-react';
import { Collapse, Button, Spin, Tag, Upload, message, Modal } from 'antd';
import { saveAs } from 'file-saver';
import api from '@/services/ApiConfig';
import Layout from '@/components/Layout';
import Card from './components/Card';
import ConfigPopWindow from './components/ConfigPopWindow';
import styles from './index.less';

const { Panel } = Collapse;
const { Main, Paste } = Layout;

const basic = [
  {name: 'basicInfo', label: '基础配置', type: 'basic'},
  {name: 'redis', label: 'Redis配置', icon:'iconRedispeizhi-hui', type: 'basic'},
  {name: 'mq', label: 'MQ配置', icon: 'iconMQpeizhi-hui', type: 'basic'},
  {name: 'db', label: '基础数据库配置', type: 'basic'},
];

const service = [
  {name: 'base', label: '基础服务', icon: 'iconjichufuwu-hui', type: 'service'},
  {name: 'auth', label: '权限服务', type: 'service'},
  {name: 'zuul', label: '网关服务', icon: 'iconwangguanpeizhi-hui', type: 'service'},
  {name: 'report-lucksoft.yml', label: '导入导出服务', type: 'service'},
  {name: 'oss-pro', label: 'OSS服务', icon: 'iconOSSfuwu-hui', type: 'service'},
  {name: 'bpm-lucksoft.yml', label: 'BPM服务', icon: 'iconBPMfuwu-hui', type: 'service'},
  {name: 'erralert', label: '告警服务', icon: 'icongaojingfuwu-hui', type: 'service'},
  {name: 'siatask-lucksoft.yml', label: '定时器服务', icon: 'icondingshifuwu-hui', type: 'service'},
  {name: 'ewx-lucksoft.yml', label: '企业微信', type: 'service'},
];

@connect(({ config, loading }) => ({
  states: config.states,
  other: config.other,
  uploading: config.uploading,
  loading: loading.effects['config/readStateAndOther'] || loading.effects['config/doRestart'],
}))
class Config extends PureComponent {

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'config/readStateAndOther'
    });
  }

  handleClick = dataSource=> {
    const { dispatch } = this.props;
    dispatch({
      type: 'configPopWindow/doOpen',
      payload: {
        dataSource,
        type: 'config'
      }
    });
  };

  handleDelete = dataSource=> {
    const { dispatch } = this.props;
    dispatch({
      type: 'config/delOtherSettingInfo',
      payload: dataSource.name
    });
  };

  handleRestart = dataSource=> {
    Modal.confirm({
      title: `重启${dataSource.label}`,
      content: `是否重启${dataSource.type === 'basic' ? '所有服务' : dataSource.label}？`,
      okText: '重启',
      cancelText: '取消',
      onOk: ()=> {
        dispatch({
          type: 'config/doRestart',
          payload: dataSource
        });
      }
    })
  };

  handleAdd = ()=> {
    const { dispatch } = this.props;
    dispatch({
      type: 'configPopWindow/doOpen',
      payload: {type: 'add'}
    });
  };

  stopPropagation = e=> {
    e.stopPropagation();
  };

  handleExport = e=> {
    e.stopPropagation();
    saveAs(`${api.CONFIG_HOST}/getYaml`);
  };

  handleUploadChange = info=> {
    const { dispatch } = this.props;
    if (info.file && info.file.status === 'uploading') {
      dispatch({
        type: 'config/setUploading',
        payload: true,
      })
    }
    if (info.file && info.file.status === 'done') {
      dispatch({
        type: 'config/setUploading',
        payload: false,
      })
    }
    if (info.file && info.file.status === 'done' && info.file.response.code === 1) {
      message.success('导入成功');
    }
  };

  renderCard = (item, states)=> (
    <Card key={item.name} state={!!states[item.name]} dataSource={item} onClick={this.handleClick} onDelete={this.handleDelete} onRestart={this.handleRestart}/>
  );

  renderButtons = ()=> (
    <div style={{display: 'flex'}}>
      <span onClick={this.stopPropagation}>
        <Upload showUploadList={false} accept=".yaml,.yml" action={`${api.CONFIG_HOST}/importYaml`} onChange={this.handleUploadChange}>
          <Tag color="blue" style={{cursor: 'pointer'}}>导入配置</Tag>
        </Upload>
      </span>
      <Tag color="#2db7f5" style={{cursor: 'pointer'}} onClick={this.handleExport}>导出配置</Tag>
    </div>
  );

  render() {
    const { states, other, loading, uploading } = this.props;
    return (
      <>
        <Layout>
          <Main>
            <Paste>
              <Spin spinning={!!loading || uploading}>
                <Collapse className={styles.configCollapse} bordered={false} defaultActiveKey={['basic','service','other']}>
                  <Panel header="基本配置" key="basic" extra={this.renderButtons()}>
                    <Box wrap="wrap">
                      {basic.map(item=> this.renderCard(item, states))}
                    </Box>
                  </Panel>
                  <Panel header="服务配置" key="service">
                    <Box wrap="wrap">
                      {service.map(item=> this.renderCard(item, states))}
                    </Box>
                  </Panel>
                  <Panel header="业务配置" key="other">
                    <Button type="primary" style={{marginLeft: 8}} onClick={this.handleAdd}>添加配置</Button>
                    <Box wrap="wrap">
                      {other.map(item=> this.renderCard(item, states))}
                    </Box>
                  </Panel>
                </Collapse>
              </Spin>
            </Paste>
          </Main>
        </Layout>
        <ConfigPopWindow />
      </>
    );
  }
}

export default Config;