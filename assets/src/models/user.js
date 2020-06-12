import { getUserInfo, autoRefreshToken, getMenu } from '@/services/user';
import { routerRedux } from "dva/router";
import { getPageQuery, dataToTree } from '@/utils/utils';

// 存储token
const saveToken = (token, expire, autoLogin)=> {
  const nowDate = new Date();
  nowDate.setSeconds(nowDate.getSeconds() + expire);
  window.g_localStore.set(window.appConfig.tokenRefreshTime, nowDate);
  window.cookie.set(window.appConfig.authSaveKey, token, autoLogin && {expires: 999999});
  window.g_localStore.set(window.appConfig.isAutoLogin, autoLogin);
};

export default {
  namespace: 'user',
  state: {
    currentUser: window.g_localStore.get(window.appConfig.currentUser) || null,
    menus: [
      {indocno: 0, sname: "配置中心", spath: "/config"},
      {indocno: 1, sname: "服务信息", spath: "/info"},
      {indocno: 2, sname: "操作日志", spath: "/log"},
    ],
    menuMap: {
      0: {indocno: 0, sname: "配置中心", spath: "/config"},
      1: {indocno: 1, sname: "服务信息", spath: "/info"},
      2: {indocno: 2, sname: "操作日志", spath: "/log"},
    },
    breadcrumbNameMap: {'/config': '配置中心', '/info': '服务信息', '/log': '操作日志'},
  },
  effects: {
    *getCurrentUser({ payload, callback }, { put, call }) {
      const { token, expire, autoLogin } = payload;
      saveToken(token, expire, autoLogin);
      window.g_localStore.set(window.appConfig.authErrorCountKey, 0);
      const response = yield call(getUserInfo);
      if (response.code === 1) {
        const userData = { ...payload, ...response.detail[response.list[0]] };
        window.g_localStore.set(window.appConfig.currentUser, userData);
        yield put({
          type: 'setCurrentUser',
          payload: userData,
        });
        if (callback) {
          yield call(callback);
        }
      }
    },

    *doRefreshToken(_, { call, put }) {
      const response = yield call(autoRefreshToken);
      const { code, data, mainInfo } = response;
      if (code === 1) {
        const expire = mainInfo && mainInfo.expire ? mainInfo.expire : window.appConfig.autoRefreshTokenTime;
        const token = data;
        saveToken(token, expire);
        yield put({
          type: 'refreshToken',
          payload: token,
        })
      }
    },

    *getMenus(_, { call, put }) {
      const response = yield call(getMenu);
      if (!response) return;
      const { code, detail, list } = response;
      if (code === 1) {
        const breadcrumbNameMap = {};
        const menuList = [];
        const menuMap = {};
        list.forEach((indocno) => {
          const menu = detail[indocno];
          const path = menu.spath.replace(/(\/*$)/g,"");
          breadcrumbNameMap[path.toLowerCase()] = menu;
          if (menu.imenu === 1) {
            menuMap[indocno] = menu;
            menuList.push({
              ...menu,
              target: (menu.spath.indexOf('http://') > -1 || menu.spath.indexOf('https://') > -1) ? '_blank' : '',
            })
          }
        });
        yield put({
          type: 'setMenus',
          payload: {
            menus: dataToTree(menuList, 0)[0].children,
            menuMap,
            breadcrumbNameMap,
          },
        });
      }
    },

    *logout(_, { put }) {
      if (window.location.pathname !== '/login') {
        yield put(routerRedux.replace('/login'));
      }
      Object.keys(window.g_app._store.getState()).forEach(key => {
        delete window.g_app._store.getState()[key];
      });
      yield put({
        type: 'setCurrentUser',
        payload: null,
      });
      setTimeout(()=>{
        window.g_localStore.remove(window.appConfig.currentUser);
        window.g_localStore.remove(window.appConfig.tokenRefreshTime);

        window.cookie.remove(window.appConfig.authSaveKey);
      },1);
    },
  },
  reducers: {
    setCurrentUser(state, { payload }) {
      return { ...state, currentUser: payload };
    },
    refreshToken(state, { payload }) {
      const currentUser = {...state.currentUser};
      currentUser.token = payload;
      return { ...state, currentUser }
    },
    setMenus(state, { payload }) {
      const { menus, menuMap, breadcrumbNameMap } = payload;
      state.menus = menus;
      state.menuMap = menuMap;
      state.breadcrumbNameMap = breadcrumbNameMap;
    }
  },
};
