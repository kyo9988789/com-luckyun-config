class ConfigDev {
  // 开始界面
  homePage = { title: '开始页', path: '/' };

  // 是否自动登录
  isAutoLogin = 'LUCK_AUTO_LOGIN';

  // 用户令牌本地存储key
  authSaveKey = 'LUCK_AUTH_MANAGER';

  // 用户名密码输入错误次数key
  authErrorCountKey = "LUCK_AUTH_ERROR_COUNT";

  // token的过期时间(秒)
  tokenExpired = 3 * 24 * 60 * 60;

  // 自动刷新token的阈值
  autoRefreshTokenTime = parseInt(this.tokenExpired * 2 / 3, 10);

  // token的过期时间Key
  tokenRefreshTime = 'LUCK_REFRESH_TIME';

  // token自动刷新定时器扫描间隔(秒)
  tokenRefreshIntervalTime = 10;

  // 记录最后登录用户信息
  lastLoginUser = 'LUCK_LAST_LOGIN';

  // 实时通信服务器地址
  realtimeHost = '10.10.10.52';

  // 实时通信服务器端口
  realtimePort = 3014;

  // 当前用户
  currentUser = 'LUCK_CURRENT_USER';

  // 用户下拉列表历史
  userDropDownHistroy = 'LUCK_USER_DROPDOWN_HISTORY';

  // 是否启用多公司
  isMoreCompany = false;

  // 图片预览地址
  IMG_HOST = `/api/base/show/img/oss/read?sprojectno=base&prefix=${window.location.origin}/api&filepath=`;

  // 文件上传地址
  UPLOAD_HOST = '/api/oss/oss/upload/file?sprojectno=msp';

  // tabNav存储缓存的key
  tabNavKey = 'LOCAL_USER_TABS';

  // 存储配置的key
  settingStoreKey = 'LUCK_DEFAULT_SETTING';
}
window.appConfig = new ConfigDev();
console.info('RUNTIME：', 'load test config');
