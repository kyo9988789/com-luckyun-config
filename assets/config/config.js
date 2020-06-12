import defaultSettings from './defaultSettings'; // https://umijs.org/config/

import slash from 'slash2';
import themePluginConfig from './themePluginConfig';
// import routes from './routes.config';

const { pwa } = defaultSettings; // preview.pro.ant.design only do not use in your production ;
// preview.pro.ant.design 专用环境变量，请不要在你的项目中使用它。

const { ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION, RUNTIME, APP_TYPE } = process.env;
const isAntDesignProPreview = ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION === 'site';
const plugins = [
  [
    'umi-plugin-react',
    {
      antd: true,
      dva: {
        hmr: true,
        immer: true,
      },
      locale: {
        enable: true,
        default: 'zh-CN',
        baseNavigator: true, // default true, when it is true, will use `navigator.language` overwrite default
      },
      // dynamicImport: {
      //   loadingComponent: './components/PageLoading/index',
      //   webpackChunkName: true,
      //   level: 3,
      // },
      pwa: pwa
        ? {
            workboxPluginMode: 'InjectManifest',
            workboxOptions: {
              importWorkboxFrom: 'local',
            },
          }
        : false, // default close dll, because issue https://github.com/ant-design/ant-design-pro/issues/4665
      // dll features https://webpack.js.org/plugins/dll-plugin/
      // dll: {
      //   include: ['dva', 'dva/router', 'dva/saga', 'dva/fetch'],
      //   exclude: ['@babel/runtime', 'netlify-lambda'],
      // },
      routes: {
        // pages文件夹下的models、components、services、locales文件夹及services.js文件均不解析成routes(ex: exclude './pages/a.js' (route) { return route.component === './pages/a.js' })
        exclude: [/models\//, /components\//, /services\//, /services\.js/, /locales\//],
        // update(routes) {
        //   const packageJson = require('../package');
        //   const tempRoutes = [...routes];
        //   const root = tempRoutes.find(route=> route.path === '/');
        //   // 移除路由中的业务名
        //   const businessName = `/${packageJson.name}`;
        //   root.routes = root.routes.map(route=> route.path && route.path.indexOf(businessName) > -1 ? {...route, path: route.path.replace(businessName, '')} : route);
        //   return tempRoutes;
        // }
      },
    },
  ],
  [
    'umi-plugin-pro-block',
    {
      moveMock: false,
      moveService: false,
      modifyRequest: true,
      autoAddMenu: true,
    },
  ],
];

if (isAntDesignProPreview) {
  // 针对 preview.pro.ant.design 的 GA 统计代码
  plugins.push([
    'umi-plugin-ga',
    {
      code: 'UA-72788897-6',
    },
  ]);
  plugins.push(['umi-plugin-antd-theme', themePluginConfig]);
}

export default {
  plugins,
  hash: true,
  targets: {
    ie: 11,
  },
  // umi routes: https://umijs.org/zh/guide/router.html
  // routes,
  // Theme for antd: https://ant.design/docs/react/customize-theme-cn
  theme: {
    // ...darkTheme,
  },
  define: {
    APP_TYPE: APP_TYPE || '',
    RUNTIME: RUNTIME || 'dev',
    ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION:
      ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION || '', // preview.pro.ant.design only do not use in your production ; preview.pro.ant.design 专用环境变量，请不要在你的项目中使用它。
  },
  ignoreMomentLocale: true,
  lessLoaderOptions: {
    javascriptEnabled: true,
  },
  disableRedirectHoist: true,
  cssLoaderOptions: {
    modules: true,
    getLocalIdent: (context, _, localName) => {
      if (
        context.resourcePath.includes('node_modules') ||
        context.resourcePath.includes('ant.design.pro.less') ||
        context.resourcePath.includes('global.less')
      ) {
        return localName;
      }

      const match = context.resourcePath.match(/src(.*)/);

      if (match && match[1]) {
        const antdProPath = match[1].replace('.less', '');
        const arr = slash(antdProPath)
          .split('/')
          .map(a => a.replace(/([A-Z])/g, '-$1'))
          .map(a => a.toLowerCase());
        return `antd-pro${arr.join('-')}-${localName}`.replace(/--/g, '-');
      }

      return localName;
    },
  },
  manifest: {
    basePath: '/',
  }, // chainWebpack: webpackPlugin,
  proxy: {
    '/api/': {
      // target: 'http://10.10.10.52:21597',
      // target: 'http://162.1.1.60:8888',
      target: 'http://172.16.0.104:8888',
      // target: 'http://162.1.1.60:9998',
      changeOrigin: true,
      pathRewrite: { '^/api': '' },
    },
  },
};
