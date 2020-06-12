import React, { PureComponent } from 'react';
import { connect } from 'dva';
import LogPopWindow from '@/components/LogPopWindow';
import ConfigPopWindowDetail from './ConfigPopWindowDetail';

@connect(({ configPopWindow, loading }) => ({
  visible: configPopWindow.visible,
  dataSource: configPopWindow.dataSource,
  type: configPopWindow.type,
  loading: loading.effects['configPopWindow/getSettingInfo'],
}))
class ConfigPopWindow extends PureComponent {

  handleClose = ()=> {
    const { dispatch } = this.props;
    dispatch({
      type: 'configPopWindow/doClose',
    });
  };

  render() {
    const { visible, dataSource, type, loading } = this.props;
    return (
      <LogPopWindow
        title={`配置中心-${dataSource ? dataSource.label : '业务服务'}`}
        visible={visible}
        loading={!!loading}
        onClose={this.handleClose}
      >
        <ConfigPopWindowDetail type={type} dataSource={dataSource}/>
      </LogPopWindow>
    )
  }
}

export default ConfigPopWindow;