import { notification } from 'antd';
import { formatMessage } from 'umi-plugin-react/locale';
import { formatQueryParams } from '@/utils/utils';

export function createModel(options) {
  const { modelType, namespace, state, subscriptions, effects, reducers } = options;

  if (!namespace) throw new Error('未提供namespace');

  return {
    namespace,
    state,
    subscriptions,
    effects: {
      *doOperate({ payload }, { call, put, select }) {
        const {
          api,
          data,
          onSuccess,
          onError,
          operateType,
          showMessage = true,
          onQueryCallback,
        } = payload;

        if (!api) throw new Error('未实现抽象方法api');

        const response = yield call(api, data);
        if (response.code === 1 && modelType === 'main') {
          const pageKey = window.location.href;
          const stateTemp = yield select(state => state[namespace]);
          const pageState = stateTemp.pageStates[pageKey];
          let { pageno } = pageState;
          if (response.code === 1) {
            switch (operateType) {
              case 'insert':
                pageno = 1;
                break;
              case 'delete':
                if (pageState.list.length === 1) pageno -= 1;
                break;
              case 'update':
              case 'fast':
              default:
                break;
            }
            if (operateType !== 'fast') {
              yield put({
                type: 'doQuery',
                payload: { pageno },
                callback: onQueryCallback,
              });
            }
            if (onSuccess) yield call(onSuccess, response);
            if (showMessage) {
              notification.success({
                message: formatMessage({ id: 'app.base.tip.title' }),
                description: formatMessage({ id: 'app.base.tip.operate.success' }),
              });
            }
          }
        } else if (response.code === 1 && modelType === 'sub') {
          if (onSuccess) yield call(onSuccess, response);
          if (showMessage) {
            notification.success({
              message: formatMessage({ id: 'app.base.tip.title' }),
              description: formatMessage({ id: 'app.base.tip.operate.success' }),
            });
          }
        } else if (response.code !== 1) {
          if (onError) yield call(onError, response);
          if (modelType === 'main' && operateType === 'fast') {
            yield put({
              type: 'doQuery',
              payload: { pageno },
              callback: onQueryCallback,
            });
          }
        }
      },
      ...effects,
    },
    reducers,
  };
}

export function createMainModel(options) {
  const { namespace, state, subscriptions, effects, reducers, readAll, pageSize = 10 } = options;

  if (!readAll) throw new Error('未实现抽象方法readAll');

  const refreshParams = (oldParams, payload) => {
    const { params, pageno, pagesize } = payload;
    const newParams = { ...oldParams };
    if (params) {
      Object.keys(params).forEach(key => {
        const { operate } = params[key];
        switch (operate) {
          case 'remove':
            delete newParams.params[key];
            break;
          case 'removeAll':
            {
              const tempParams = {};
              if (options.params && Object.keys(options.params).length > 0) {
                Object.keys(options.params).forEach(k => {
                  tempParams[k] = newParams.params[k];
                });
              }
              newParams.params = tempParams;
            }
            break;
          case 'update':
          default:
            delete params[key].operate;
            newParams.params[key] = params[key];
            break;
        }
      });
    }
    if (pageno) {
      newParams.pageno = pageno;
    }
    if (pagesize) {
      newParams.pagesize = pagesize;
    }
    return newParams;
  };

  return createModel({
    modelType: 'main',
    namespace,
    state: {
      pageStates: {},
      ...state,
    },
    subscriptions,
    effects: {
      *init({ payload, callback }, { put }) {
        if (payload && Object.keys(payload).length > 0) {
          if (options.params) {
            Object.keys(payload).forEach(key => {
              options.params[key] = { ...payload[key] };
            });
          } else {
            options.params = { ...payload };
          }
          yield put({
            type: 'doQuery',
            payload: { params: options.params, pageno: 1 },
            callback,
          });
        } else {
          yield put({
            type: 'doQuery',
            payload: { pageno: 1 },
            callback,
          });
        }
      },
      *doQuery({ payload, callback }, { put, call, select }) {
        const pageKey = window.location.href;
        const modelState = yield select(state => state[namespace]);
        if (!modelState) return;
        const pageState = modelState.pageStates[pageKey];
        let queryParams = {
          params: pageState.params,
          pageno: pageState.pageno,
          pagesize: pageState.pagesize,
        };
        if (payload) {
          if (!payload.pageno) {
            payload.pageno = 1;
          }
          queryParams = refreshParams(queryParams, payload);
          yield put({
            type: 'refreshPageState',
            payload: { pageKey, queryParams },
          });
        }
        const apiParams = formatQueryParams(queryParams.params);
        const response = yield call(readAll, {
          ...apiParams,
          pageno: queryParams.pageno,
          pagesize: queryParams.pagesize,
        });
        if (response.code === 1) {
          yield put({
            type: 'setReadAll',
            payload: { pageKey, ...response, pagination: response.pagination || {} },
          });
        }
        if (callback) {
          yield call(callback, response);
        }
      },
      ...effects,
    },
    reducers: {
      initPageState(state, { payload }) {
        const { pageKey, reset } = payload;
        if (reset || !state.pageStates[pageKey]) {
          state.pageStates[pageKey] = {
            params: { ...options.params } || {},
            mainInfo: null,
            list: [],
            detail: null,
            pageno: 1,
            pagesize: pageSize,
            totalCount: null,
            operates: [],
          };
        }
      },
      refreshPageState(state, { payload }) {
        const { pageKey, queryParams } = payload;
        if (queryParams) {
          const { params, pageno, pagesize } = queryParams;
          if (params) {
            state.pageStates[pageKey].params = { ...params };
          }
          if (pageno) {
            state.pageStates[pageKey].pageno = pageno;
          }
          if (pagesize) {
            state.pageStates[pageKey].pagesize = pagesize;
          }
        }
      },
      setReadAll(state, { payload }) {
        const {
          pageKey,
          mainInfo,
          list,
          detail,
          operates,
          pagination: { totalCount },
        } = payload;
        state.pageStates[pageKey].mainInfo = mainInfo;
        state.pageStates[pageKey].list = list;
        state.pageStates[pageKey].detail = detail;
        state.pageStates[pageKey].totalCount = totalCount;
        state.pageStates[pageKey].operates = operates;
      },
      ...reducers,
    },
  });
}

export function createSubModel(options) {
  const { namespace, state, subscriptions, effects, reducers, readOne } = options;

  if (!readOne) throw new Error('未实现抽象方法readAll');

  return createModel({
    modelType: 'sub',
    namespace,
    state: {
      indocno: null,
      dataSource: {},
      readOnly: false,
      operates: [],
      ...state,
    },
    subscriptions,
    effects: {
      *readOne({ payload, callback }, { call, put }) {
        const { params, format } = payload || {};
        const response = yield call(readOne, params);
        const { code, list, detail, operates } = response;
        if (code === 1) {
          const dataSource = detail[list[0]];
          yield put({
            type: 'setDataSource',
            payload: {
              dataSource: format ? format(dataSource) : dataSource,
              operates,
            },
          });
        }
        if (callback) {
          yield call(callback, response);
        }
      },
      ...effects,
    },
    reducers: {
      setDataSource(state, { payload }) {
        const { dataSource, operates } = payload;
        state.dataSource = dataSource;
        state.operates = operates;
      },
      ...reducers,
    },
  });
}
