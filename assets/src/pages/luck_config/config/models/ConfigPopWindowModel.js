import { doConfig, getSettingInfo, addOtherSettingInfo } from '../services';

export default {
  namespace: 'configPopWindow',
  state: {
    visible: false,
    type: null,
    dataSource: null,
    formData: {},
    config: [],
  },
  effects: {
    *doOpen({ payload }, { put }) {
      const { dataSource, type } = payload;
      yield put({
        type: 'setVisible',
        payload: {
          dataSource,
          type,
          visible: true,
        },
      });
    },
    *doClose(_, { put }) {
      yield put({
        type: 'setVisible',
        payload: {
          visible: false,
        },
      })
    },
    *getSettingInfo({ payload }, { call, put }) {
      const response = yield call(getSettingInfo, payload);
      const { code, data } = response;
      if (code === 1 && data) {
        const temp = {};
        Object.keys(data).forEach(key => {
          if (!/\$\{.*?(?=\})/gi.test(data[key].value)) { // 不以'${'开头且不以'}'结尾
            const tempKey = key.replace(/\./g,"|"); // 替换所有的'.'为'|'
            temp[tempKey] = data[key];
          }
        });
        yield put({
          type: 'setFormData',
          payload: temp
        });
      }
    },
    *doConfig({ payload, callback }, { call, put }) {
      const { params, body } = payload;
      const response = yield call(doConfig, params, body);
      const { code } = response;
      if (code === 1) {
        yield put({
          type: 'doClose'
        });
        yield put({
          type: 'config/readStateAndOther'
        });
        if (callback) {
          yield call(callback);
        }
      }
    },
    *addOtherSettingInfo({ payload, callback }, { call, put }) {
      const response = yield call(addOtherSettingInfo, payload);
      if (response.code === 1) {
        yield put({
          type: 'doClose'
        });
        yield put({
          type: 'config/readStateAndOther'
        });
        if (callback) {
          yield call(callback);
        }
      }
    }
  },
  reducers: {
    setVisible(state, { payload }) {
      const { visible, type, dataSource } = payload;
      state.visible = visible;
      if (visible) {
        let configJson = null;
        try {
          if (type === 'add') {
            configJson = require('../configFiles/other.config.json');
          } else if (type === 'config') {
            configJson = dataSource ? require(`../configFiles/${dataSource.name}.config.json`) : [];
          }
        } catch (e) {
          configJson = [];
        }
        state.type = type;
        state.dataSource = dataSource;
        state.config = configJson && configJson.length > 0 ? [...configJson] : [];
      } else {
        state.type = null;
        state.dataSource = null;
        state.config = [];
        state.formData = {};
      }
    },
    setConfig(state, { payload }) {
      if (payload.isRemove) {
        state.config = state.config.filter(item=> item.field !== payload.field);
      } else {
        state.config = state.config.concat(payload);
      }
    },
    setFormData(state, { payload }) {
      const temp = {};
      if (payload && Object.keys(payload).length > 0) {
        const config = [...state.config];
        const newConfig = [];
        Object.keys(payload).forEach(key => {
          temp[key] = payload[key].value;
          if (key !== 'systeminfo|system-name' && key !== 'updateKeys' && config.findIndex(item=> item.field === key) === -1) {
            newConfig.push({field: key, label: payload[key].label, sort: payload[key].sort || 100})
          }
        });
        state.formData = temp;
        state.config = config.concat(newConfig.sort((a, b)=> a.sort - b.sort));
      }
    }
  },
};
