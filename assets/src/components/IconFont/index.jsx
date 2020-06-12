import React, { PureComponent } from 'react';
import { Icon } from 'antd';

class IconFont extends PureComponent{

  render() {

    const { extraCommonProps, type, ...rest } = this.props;

    // SVG Symbol
    const Index = Icon.createFromIconfontCN({
      scriptUrl: '/luck/icons/iconfont.js',
      extraCommonProps
    });

    return <Index type={type} {...rest} />
  }
}

export default IconFont;