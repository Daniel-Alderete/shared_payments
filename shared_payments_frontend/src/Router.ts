import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    alias: "/groups",
    name: "groups",
    component: () => import("./components/GroupList.vue"),
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
