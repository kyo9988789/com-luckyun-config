import { parse } from 'querystring';
import pathRegexp from 'path-to-regexp';
/* eslint no-useless-escape:0 import/prefer-default-export:0 */

const reg = /(((^https?:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+(?::\d+)?|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w-_]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)$/;
export const isUrl = path => reg.test(path);
export const isAntDesignPro = () => {
  if (ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION === 'site') {
    return true;
  }

  return window.location.hostname === 'preview.pro.ant.design';
}; // 给官方演示站点用，用于关闭真实开发环境不需要使用的特性

export const isAntDesignProOrDev = () => {
  const { NODE_ENV } = process.env;

  if (NODE_ENV === 'development') {
    return true;
  }

  return isAntDesignPro();
};
export const getPageQuery = () => parse(window.location.href.split('?')[1]);

export const formatQueryParams = (params)=> {
  const pageParams = getPageQuery();
  let ret = {};
  Object.keys(params).forEach(key => {
    try {
      params[key].forEach(value => {
        ret[value.key] = value.value instanceof Array ? join(value.value, ',') : value.value;
      })
    } catch (e) {
      const tempValue = params[key];
      ret[tempValue.key] = tempValue.value instanceof Array ? join(tempValue.value, ',') : tempValue.value;
    }
  });
  if (pageParams && Object.keys(pageParams).length > 0) {
    ret = Object.assign(pageParams, ret);
  }
  return ret;
};

/**
 * props.route.routes
 * @param router [{}]
 * @param pathname string
 */

export const getAuthorityFromRouter = (router = [], pathname) => {
  const authority = router.find(({ path }) => path && pathRegexp(path).exec(pathname));
  if (authority) return authority;
  return undefined;
};

/**
 * 格式化成树形数据源
 * @param allList 获取的平级数据源
 * @param treeData 目标树形数据源
 */
export function treeDataFormat(allList, treeData) {
  treeData.forEach((v) => {
    const tempList = [];
    allList.forEach((value) => {
      if (value.iparentid == null) {
        value.iparentid = 0;
      }
      if (Number(value.iparentid) === Number(v.indocno)) {
        const obj = { ...value, key: `${value.indocno}` };
        if (v.children === null || !v.children) {
          v.children = [];
        }
        v.children.push(obj);
      } else {
        tempList.push(value);
      }
    });
    if (v.children && v.children !== null) {
      treeDataFormat(tempList, v.children);
    }
  });
}


/**
 * 格式化成树形数据源
 * @param data 平级数据源
 * @returns {{indocno: number, children: Array}[]}
 */
export function dataToTree(data, rootid) {
  const treeData = [{ indocno: rootid || 0, children: [] }];
  if (data) {
    treeDataFormat(data, treeData);
  }
  return treeData;
}