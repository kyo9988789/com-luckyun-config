import request from '@/utils/request';
import api from '@/services/ApiConfig';
import { stringify } from 'querystring';

export async function readAll() {
  return request(`${api.INFO_HOST}/getApplications`);
}

export async function restart(params) {
  return request(`${api.INFO_HOST}/restartApplicationByUrl?${stringify(params)}`);
}