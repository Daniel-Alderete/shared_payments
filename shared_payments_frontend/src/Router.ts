import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    alias: "/groups",
    name: "groups",
    component: () => import("./components/GroupList.vue"),
  },
  {
    path: "/groups/:id",
    name: "group-details",
    component: () => import("./components/GroupDetail.vue"),
  },
  /* {
    path: "/addfriend",
    name: "add-friend",
    component: () => import("./components/AddFriend.vue"),
  },
  {
    path: "/addpayment",
    name: "add-payment",
    component: () => import("./components/AddPayment.vue"),
  } */
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
