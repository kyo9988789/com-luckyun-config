import React, { PureComponent } from 'react';
import { connect } from 'dva';
import NProgress from 'nprogress';
import LuckLayout from './LuckLayout';
import 'nprogress/nprogress.css';

let currHref = '';

class SecurityLayout extends PureComponent {

  render() {
    const { children, loading } = this.props;

    const { href } = window.location; // 浏览器地址栏中地址
    if (currHref !== href) {
      // currHref 和 href 不一致时说明进行了页面跳转
      NProgress.start(); // 页面开始加载时调用 start 方法
      if (!loading) {
        NProgress.done(); // 页面请求完毕时调用 done 方法
        currHref = href; // 将新页面的 href 值赋值给 currHref
      }
    }

    return (
      <LuckLayout {...this.props}>
        {children}
      </LuckLayout>
    );
  }
}

export default connect(({ user, loading }) => ({
  loading: loading.global,
}))(SecurityLayout);
