import { getUpdateLog } from "../services";

export default {
  namespace: 'log',
  state: {
    dataSource: null,
  },
  effects: {
    *getUpdateLog(_, { call, put }) {
      const response = yield call(getUpdateLog);
      const { code, data } = response;
      if (code === 1) {
        yield put({
          type: 'setDataSource',
          payload: data,
        })
      }
    },
  },
  reducers: {
    setDataSource(state, { payload }) {
      state.dataSource = payload;
    },
  }
};
