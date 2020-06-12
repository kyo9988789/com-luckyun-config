import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { Box } from 'luck-react';
import styles from './style/index.less';

const LuckSider = Box.Layout.Sider;

class Sider extends PureComponent{
  static displayName = 'Box';

  static propTypes = {
    /**
     * 自定义样式类
     */
    className: PropTypes.string,
  };

  static defaultProps = {
    className: '',
  };

  render() {
    const { children, className, ...rest } = this.props;
    return (
      <LuckSider {...rest} height="calc(100vh - 180px)" className={`${styles.luckSider} ${className}`}>
        {children}
      </LuckSider>
    )
  }
}

export default Sider;