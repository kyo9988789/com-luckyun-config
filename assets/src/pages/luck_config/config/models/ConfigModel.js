import { message } from 'antd';
import { readState, getOtherSettingInfo, delOtherSettingInfo, doRestart } from '../services';

export default {
  namespace: 'config',
  state: {
    states: {},
    other: [],
    uploading: false,
  },
  effects: {
    *readStateAndOther(_, { call, put, all }) {
      const { response1, response2 } = yield all({
        response1: call(readState),
        response2: call(getOtherSettingInfo)
      });
      yield put({
        type: 'setStatesAndOther',
        payload: {response1, response2},
      });
    },
    *delOtherSettingInfo({ payload }, { call, put }) {
      const response = yield call(delOtherSettingInfo, payload);
      if (response.code === 1) {
        yield put({
          type: 'readStateAndOther',
        });
      }
    },
    *doRestart({ payload }, { call }) {
      const response = yield call(doRestart, payload.name);
      if (response.code === 1) {
        message.success(`${payload.label}正在重启中…`)
      }
    }
  },
  reducers: {
    setStatesAndOther(state, { payload }) {
      const { response1, response2 } = payload;
      if (response1.code === 1) {
        state.states = response1.data;
      }
      if (response2.code === 1) {
        state.other = response2.data.map(item=> ({
          name: item.filename,
          label: item.sname,
          icon: 'iconmoumoupeizhi-hui',
          type: 'other',
        }));
      }
    },
    setUploading(state, { payload }) {
      state.uploading = payload;
    }
  }
};
