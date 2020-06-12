import request from '@/utils/request';
import api from '@/services/ApiConfig';
import { stringify } from 'querystring';

export async function readState() {
  return request(`${api.CONFIG_HOST}/getSettingState`);
}

export async function getOtherSettingInfo() {
  return request(`${api.CONFIG_HOST}/getOtherSettingInfo`);
}

export async function getSettingInfo(params) {
  return request(`${api.CONFIG_HOST}/getSettingInfo?${stringify(params)}`);
}

export async function doConfig(params, body) {
  return request(`${api.CONFIG_HOST}/writeProperties?${stringify(params)}`, {
    method: 'POST',
    body,
  });
}

export async function addOtherSettingInfo(body) {
  return request(`${api.CONFIG_HOST}/addOtherSettingInfo`, {
    method: 'POST',
    body,
  });
}

export async function delOtherSettingInfo(filename) {
  return request(`${api.CONFIG_HOST}/delOtherSettingInfo?filename=${filename}`);
}

export async function doRestart(filename) {
  return request(`${api.INFO_HOST}/restartSingleApplication?filename=${filename}`);
}