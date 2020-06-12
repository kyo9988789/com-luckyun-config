import React, {PureComponent} from 'react';
import PropTypes from 'prop-types';
import { Box } from 'luck-react';
import findIndex from 'lodash/findIndex';
import styles from './style/index.less';
import Sider from './Sider';
import Main from './Main';

class Layout extends PureComponent {


  static Sider = Sider;

  static Main = Main;

  static Paste = Box.Layout.Paste;

  static propTypes = {
    /**
     * 自定义样式类
     */
    className: PropTypes.string,
    height: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  };

  static defaultProps = {
    className: '',
  };

  render() {
    const { children, className, height, ...rest } = this.props;
    const hasSider = findIndex(children, child=> child.type && child.type.name === 'Sider') > -1;
    const newChildren = React.Children.map(children, child => {
      if (child.type && child.type.name === 'Main' && hasSider) {
        return React.cloneElement(
          child,
          {
            ...child.props,
            className: `${styles.luckMain} ${child.props.scroll ? styles.scroll : ''} ${child.props.className}`
          },
        )
      }
      return child
    });
    const tempHeight = hasSider ? '100%' : 'auto';
    return (
      <Box {...rest} className={`${styles.luckLayout} ${className}`} height={height || tempHeight} width="100%">
        {newChildren}
      </Box>
    )
  }
}

export default Layout