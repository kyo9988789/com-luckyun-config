import React, { PureComponent } from 'react';
import { connect } from 'dva';
import PropTypes from 'prop-types';
import { formatMessage } from 'umi-plugin-react/locale';
import { Spin, Icon, Modal } from 'antd';
import NoticeIconView from './NoticeIconView';
import Timer from './Timer';
import styles from './index.less';

@connect(({ user }) => ({
  currentUser: user.currentUser,
}))

class LuckRightContent extends PureComponent {

  static propTypes = {
    className: PropTypes.string,
  };

  static defaultProps = {
    className: '',
  };

  handleLogout = ()=> {
    Modal.confirm({
      title: formatMessage({id: 'component.globalHeader.confirm.logout'}),
      okText: formatMessage({id: 'app.base.operate.ok'}),
      cancelText: formatMessage({id: 'app.base.operate.cancel'}),
      onOk: ()=> {
        const { dispatch } = this.props;
        dispatch({
          type: 'user/logout',
        });
      }
    });
  };

  render() {
    const { className, currentUser } = this.props;
    return (
      <div className={`${styles.luckRight} ${className}`}>
        <div className={styles.f1}>
          {currentUser && currentUser.sname ? (
            <div style={{marginRight: 8}}>{currentUser.sname}</div>
          ) : (
            <Spin size="small" style={{marginLeft: 8, marginRight: 8}}/>
          )}
          <NoticeIconView mini={false} />
          <div style={{marginLeft: 16, cursor: 'pointer'}} onClick={this.handleLogout}>
            <Icon type="poweroff" style={{marginRight: 4}}/>
            {formatMessage({id: 'menu.account.logout'})}
          </div>
        </div>
        <div className={styles.f2}><Timer/></div>
      </div>
    )
  }
}

export default LuckRightContent;