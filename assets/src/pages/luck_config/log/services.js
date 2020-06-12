import request from '@/utils/request';
import api from '@/services/ApiConfig';

export async function getUpdateLog() {
  return request(`${api.CONFIG_HOST}/getUpdateLog`);
}