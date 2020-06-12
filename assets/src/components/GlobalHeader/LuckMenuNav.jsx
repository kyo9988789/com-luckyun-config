import React, { PureComponent } from 'react';
import { connect } from 'dva';
import Link from 'umi/link';
import { routerRedux } from 'dva/router';
import { Menu, Breadcrumb } from 'antd';
import IconFont from '@/components/IconFont';
import styles from './index.less';

const { Item, SubMenu } = Menu;

@connect(({ user }) => ({
  menus: user.menus,
  menuMap: user.menuMap,
  breadcrumbNameMap: user.breadcrumbNameMap,
}))

class LuckMenuNav extends PureComponent {

  handleClick = (e)=> {
    const { dispatch, menuMap } = this.props;
    const menu = menuMap[e.key];
    dispatch(routerRedux.push(menu.spath));
  };

  renderMenuItem = (menus)=> (
    menus.map(menu => (
      menu.children && menu.children.length > 0 ? (
        <SubMenu
          key={menu.indocno}
          title={
            <span>
              {menu.sicon && <IconFont type={menu.sicon} />}
              <span>{menu.sname}</span>
            </span>
          }
        >
          {this.renderMenuItem(menu.children)}
        </SubMenu>
      ) : (
        <Item key={menu.indocno}>
          {menu.sicon && <IconFont type={menu.sicon} />}
          <span>{menu.sname}</span>
        </Item>
      )
    ))
  );

  render() {
    const { menus, breadcrumbNameMap, location = {pathname: '/',} } = this.props;
    const pathSnippets = location.pathname.split('/').filter(i => i);
    const extraBreadcrumbItems = pathSnippets.map((_, index) => {
      const pathArr = pathSnippets.slice(0, index + 1);
      const url = `/${pathArr.join('/')}`;
      const breadcrumbItem = breadcrumbNameMap[url.toLowerCase()];
      const link = breadcrumbItem ? breadcrumbItem : pathArr.slice(-1);
      return (
        <Breadcrumb.Item key={url} className={index === pathSnippets.length - 1 ? styles.lastBreadcrumb : ''}>
          {(window.routes[url] && url !== location.pathname) ? <Link to={url}>{link}</Link> : link}
        </Breadcrumb.Item>
      );
    });
    const breadcrumbItems = [
      <Breadcrumb.Item key="home">
        配置平台
      </Breadcrumb.Item>,
    ].concat(extraBreadcrumbItems);
    const pages = {
      '/': '0',
      '/config': '0',
      '/info': '1',
      '/log': '2'
    };
    return (
      <div className={styles.luckMenuNav}>
        <Menu onClick={this.handleClick} mode="horizontal" selectedKeys={[pages[location.pathname]]}className={styles.boxPadding}>
          {this.renderMenuItem(menus)}
        </Menu>
        <Breadcrumb className={`${styles.breadcrumbBox} ${styles.boxPadding}`}>{breadcrumbItems}</Breadcrumb>
      </div>
    )
  }
}

export default LuckMenuNav;