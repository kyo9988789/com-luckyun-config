import { readAll, restart } from "../services";

export default {
  namespace: 'info',
  state: {
    dataSource: null,
    restartIndex: 0,
  },
  effects: {
    *readAll(_, { call, put }) {
      const response = yield call(readAll);
      const { code, data } = response;
      if (code === 1) {
        yield put({
          type: 'setDataSource',
          payload: data,
        })
      }
    },
    *restart({ payload }, { call }) {
      yield call(restart, payload);
    }
  },
  reducers: {
    setDataSource(state, { payload }) {
      state.dataSource = payload;
    },
    setRestartIndex(state, { payload }) {
      state.restartIndex = payload;
    }
  }
};
