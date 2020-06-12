import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { Box } from 'luck-react';

class Main extends PureComponent {
  static displayName = 'Box';

  static propTypes = {
    /**
     * 自定义样式类
     */
    className: PropTypes.string,
    /**
     * 是否允许滚动
     */
    scroll: PropTypes.bool,
    /**
     * 为满足设计图样式，手动修改顶部间距
     */
    paddingTop: PropTypes.oneOf([0, 4, 8, 16]),
    /**
     * 为满足设计图样式，手动修改底部间距
     */
    paddingBottom: PropTypes.oneOf([0, 4, 8, 16]),
  };

  static defaultProps = {
    className: '',
    scroll: false,
    paddingTop: 0,
    paddingBottom: 0,
  };

  render() {
    const { children, className, paddingTop, paddingBottom , style, scroll, ...rest } = this.props;
    return(
      <Box
        {...rest}
        height="100%"
        direction="column"
        grow={1}
        className={className}
        // className={`${styles.luckMain} ${scroll && styles.scroll} ${className}`}
        style={{...style, paddingTop, paddingBottom}}
      >
        {children}
      </Box>
    )
  }
}

export default Main;