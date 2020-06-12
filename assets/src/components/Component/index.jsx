import React, { PureComponent } from 'react';
import { connect } from 'dva';

function buildComponent(
  WrappedComponent,
  modelName,
  beforeInit = () => new Promise(resolve => resolve()),
  afterInit,
  alwaysReset = false,
) {
  if (!modelName) {
    return WrappedComponent;
  }

  @connect(state => {
    const pageKey = window.location.href;
    const pageStates = state[modelName].pageStates[pageKey];
    return {
      params: pageStates ? pageStates.params : {},
      mainInfo: pageStates ? pageStates.mainInfo : null,
      list: pageStates ? pageStates.list : [],
      detail: pageStates ? pageStates.detail : null,
      pageno: pageStates ? pageStates.pageno : 1,
      pagesize: pageStates ? pageStates.pagesize : 10,
      totalCount: pageStates ? pageStates.totalCount : null,
      operates: pageStates ? pageStates.operates : [],
      loading: state.loading.effects[`${modelName}/doQuery`],
    };
  })
  class Component extends PureComponent {
    constructor(props) {
      super(props);
      this.pageKey = window.location.href;
      props.dispatch({
        type: `${modelName}/initPageState`,
        payload: {pageKey: this.pageKey, reset: false},
      });
    }

    componentDidMount() {
      const { dispatch, totalCount } = this.props;
      if (alwaysReset || !totalCount) {
        // 第二次打开页面不重置参数
        try {
          beforeInit(dispatch).then(params => {
            dispatch({
              type: `${modelName}/init`,
              payload: params,
              callback: afterInit ? res => afterInit(res, dispatch) : null,
            });
          });
        } catch (e) {
          console.error('beforeInit必须为返回Promise对象的方法');
        }
      }
    }

    componentWillUnmount() {
      if (alwaysReset) {
        const { dispatch } = this.props;
        dispatch({
          type: `${modelName}/initPageState`,
          payload: {pageKey: this.pageKey, reset: true},
        });
      }
    }

    handleQuery = params => {
      const { dispatch } = this.props;
      dispatch({
        type: `${modelName}/doQuery`,
        payload: { params },
      });
    };

    render() {
      return <WrappedComponent {...this.props} onQuery={this.handleQuery} />;
    }
  }

  return Component;
}

export default buildComponent;
