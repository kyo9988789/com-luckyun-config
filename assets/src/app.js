export function patchRoutes(routes) {
  const packageJson = require('../package');
  const tempRoutes = [...routes];
  const root = tempRoutes.find(route=> route.path === '/');
  // 移除路由中的业务名
  const businessName = `/${packageJson.name}`;
  root.routes = root.routes.map(route=> route.path && route.path.indexOf(businessName) > -1 ? {...route, path: route.path.replace(businessName, '')} : route);
  window.routes = {};
  tempRoutes[0].routes.forEach(route => {
    if (route.path) {
      window.routes[route.path] = route;
    }
  });
  return tempRoutes;
}
