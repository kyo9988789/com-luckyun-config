import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Ellipsis } from 'luck-react';
import moment from 'moment';
import compact from 'lodash/compact';
import Layout from '@/components/Layout';
import GridList from '@/components/List/Grid';

const { Main } = Layout;

@connect(({ log, loading }) => ({
  dataSource: log.dataSource,
  loading: loading.effects['log/getUpdateLog'],
}))
class Log extends PureComponent {

  columns = [
    { title: '服务名称', field: 'sysInfoName', filter: 'text' },
    { title: '修改时间', field: 'timestemp', render: text=> moment(text).format('YYYY-MM-DD HH:mm:ss'), filter: 'date' },
    { title: '修改字段', field: 'stype', render: (text, record)=> <Ellipsis tooltip lines={1}>{compact(record.settingInfos.map(info=> info.label)).join()}</Ellipsis> },
  ];

  subColumns = [
    { title: '标题', field: 'label' },
    { title: '键', field: 'updateKey', render: text=> <Ellipsis tooltip lines={1}>{text}</Ellipsis> },
    { title: '新值', field: 'newValue', render: text=> text ? <Ellipsis tooltip lines={1}>{text}</Ellipsis> : '-' },
    { title: '旧值', field: 'oldValue', render: text=> text ? <Ellipsis tooltip lines={1}>{text}</Ellipsis> : '-' },
    {
      title: '操作类型', field: 'timeStamp', render: (text, record)=> {
        if (record.oldValue && record.newValue) {
          return '修改';
        } else if (!record.oldValue && record.newValue) {
          return '添加';
        } else if (record.oldValue && !record.newValue) {
          return '删除';
        }
      }
    },
  ];

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({ type: 'log/getUpdateLog' });
  }

  handleRestore = data=> {

  };

  renderActions = data=> (
    <a style={{ color: '#1890ff' }} onClick={() => this.handleRestore(data)}>
      恢复
    </a>
  );

  renderExpandedRow = {
    height: params => 48 * params.data.settingInfos.length + 120,
    render: record => (
      <GridList
        rowKey="updateKey"
        columns={this.subColumns}
        dataSource={record.settingInfos}
        pagination={false}
        bordered
        showStatusBar={false}
      />
    )
  };

  render() {
    const { dataSource, loading } = this.props;
    return (
      <Layout height="100%">
        <Main paddingTop={8} paddingBottom={8}>
          <GridList
            rowKey="timestemp"
            modelName="log"
            columns={this.columns}
            dataSource={dataSource}
            loading={!!loading}
            pagination={false}
            bordered
            renderIndex
            // renderActions={this.renderActions}
            renderExpandedRow={this.renderExpandedRow}
          />
        </Main>
      </Layout>
    )
  }
}

export default Log;