import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Alert, Icon, Button } from 'antd';
import Layout from '@/components/Layout';
import GridList from '@/components/List/Grid';

const { Main } = Layout;

const sleep = time => new Promise(resolve => setTimeout(resolve, time));

@connect(({ info, loading }) => ({
  dataSource: info.dataSource,
  restartIndex: info.restartIndex,
  loading: loading.effects['info/readAll'],
}))
class Info extends PureComponent {
  columns = [{ title: '服务名称', field: 'sname' }];

  componentDidMount() {
    const { dispatch } = this.props;
    const readAll = () => {
      dispatch({ type: 'info/readAll' });
    };
    this.timer = setInterval(() => {
      readAll();
    }, 10 * 1000);
    readAll();
  }

  componentWillUnmount() {
    if (this.timer) clearInterval(this.timer);
  }

  handleRestart = services => {
    const { dispatch, dataSource } = this.props;
    if (services.length > 1) {
      const loop = i => {
        const tempI = i + 1;
        dispatch({
          type: 'info/setRestartIndex',
          payload: tempI < services.length ? tempI : 0,
        });
        services[i].instance.forEach(instance => {
          dispatch({
            type: 'info/restart',
            payload: { instanceId: instance.instanceId, instanceName: services[i].name },
          });
        });
        if (tempI < services.length) {
          sleep(10 * 1000).then(() => {
            loop(tempI);
          });
        }
      };
      loop(0);
    } else if (services.length === 1) {
      const service = services[0];
      const index = dataSource.findIndex(item => item.name === service.name) + 1;
      dispatch({
        type: 'info/setRestartIndex',
        payload: index,
      });
      service.instance.forEach(instance => {
        dispatch({
          type: 'info/restart',
          payload: { instanceId: instance.instanceId, instanceName: service.name },
        });
      });
      sleep(10 * 1000).then(() => {
        dispatch({
          type: 'info/setRestartIndex',
          payload: 0,
        });
      });
    }
  };

  renderActions = data => (
    <a style={{ color: '#1890ff' }} onClick={() => this.handleRestart([data])}>
      重启
    </a>
  );

  renderExpandedRow = {
    height: params => 48 * params.data.instance.length + 40,
    render: record =>
      record.instance.map(item => (
        <Alert
          key={item.instanceId}
          message={item.instanceId}
          type={item.status === 'UP' ? 'success' : 'error'}
          showIcon
          style={{ marginBottom: 8 }}
          icon={<Icon type={item.status === 'UP' ? 'arrow-up' : 'arrow-down'} />}
        />
      )),
  };

  render() {
    const { dataSource, restartIndex, loading } = this.props;
    return (
      <>
        <Layout height="100%">
          <Main paddingTop={8} paddingBottom={8}>
            <div style={{ paddingLeft: 8 }}>
              <Button
                type="primary"
                onClick={() => this.handleRestart(dataSource)}
                loading={restartIndex !== 0 && restartIndex !== dataSource.length}
              >
                {restartIndex === 0
                  ? '重启所有服务'
                  : `正在重启${dataSource[restartIndex - 1].sname}`}
              </Button>
            </div>
            <GridList
              rowKey="name"
              modelName="info"
              columns={this.columns}
              dataSource={dataSource}
              loading={!!loading || restartIndex !== 0}
              pagination={false}
              bordered
              renderIndex
              renderActions={this.renderActions}
              renderExpandedRow={this.renderExpandedRow}
            />
          </Main>
        </Layout>
      </>
    );
  }
}

export default Info;
