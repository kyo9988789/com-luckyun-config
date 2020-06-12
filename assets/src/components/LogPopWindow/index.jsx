import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { PopWindow } from 'luck-react';
import styles from './index.less';

class LogPopWindow extends PureComponent {
  static propTypes = {
    className: PropTypes.string,
    mode: PropTypes.oneOf(['drawer', 'modal']),
    size: PropTypes.oneOf(['small', 'default', 'large', 'full']),
    loading: PropTypes.bool,
    readOnly: PropTypes.bool,
    fullScreen: PropTypes.bool,
  };

  static defaultProps = {
    className: '',
    mode: 'modal',
    size: 'default',
    loading: false,
    readOnly: false,
    fullScreen: true,
  };
  render() {
    const { children, mode, size, loading, readOnly, fullScreen, footer, ...rest } = this.props;
    return (
      <PopWindow
        {...rest}
        mode={mode}
        size={size}
        loading={loading}
        readOnly={readOnly}
        fullScreen={fullScreen}
        destroyOnClose
        className={styles.logPopWindow}
        wrapClassName={styles.logPopWindowWrap}
        footer={null}
      >
        {children}
      </PopWindow>
    );
  }
}

export default LogPopWindow;
