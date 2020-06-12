import React, { PureComponent } from 'react';
import { connect } from 'dva';
import Link from 'umi/link';
import { Layout, Result, Button } from 'antd';
import { Box } from 'luck-react';
import Authorized from '@/utils/Authorized';
import { getAuthorityFromRouter } from '@/utils/utils';
import styles from './LuckLayout.less';
import logo from '../assets/logo.svg';
import LuckMenuNav from '@/components/GlobalHeader/LuckMenuNav';
import LuckRightContent from '@/components/GlobalHeader/LuckRightContent';

const { Header, Content } = Layout;
const noMatch = (
  <Result
    status="403"
    title="403"
    subTitle="Sorry, you are not authorized to access this page."
    extra={
      <Button type="primary">
        <Link to="/login">Go Login</Link>
      </Button>
    }
  />
);

@connect(({ settings }) => ({
  settings,
}))

class LuckLayout extends PureComponent{

  render() {
    const {children, settings: {headerBackground, title, contentHeight}, location = {pathname: '/',}, route} = this.props;
    const authorized = getAuthorityFromRouter(route.routes, location.pathname) || {
      authority: undefined,
    };

    return (
      <Layout className={styles.luckLayout}>
        <Header className={styles.luckLayoutHeader} style={{background: headerBackground}}>
          <Link to="/" className={styles.logoLink}>
            <img src={logo} alt="logo" />
            <h1>{title}</h1>
          </Link>
          <Box height="100%" grow={1}></Box>
        </Header>
        <LuckMenuNav location={location} />
        <Content className={contentHeight === 'Fixed' ? styles.luckLayoutContentFixed : styles.luckLayoutContentFluid}>
          <Authorized authority={authorized.authority} noMatch={noMatch}>
            {children}
          </Authorized>
        </Content>
      </Layout>
    )
  }
}

export default LuckLayout;