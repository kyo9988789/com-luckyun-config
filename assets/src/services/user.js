import request from '@/utils/request';
import api from './ApiConfig';

// 获取当前登录人信息
export async function getUserInfo() {
  return request(`${api.BASE_HOST}/user/sysUser/noAuthUser`);
}

// 刷新token
export async function autoRefreshToken() {
  return request(`${api.API_HOST}/auth/do/refresh/token/noAuth`);
}

// 获取菜单
export async function getMenu(){
  return request(`${api.BASE_HOST}/module/sysModule/noAuthUserAllModule`);
}