import { createApp } from "vue";
import "highlight.js/styles/github.css";
import App from "./App.vue";
// 挂载前端根组件，统一承载登录、文档和后台页面。
createApp(App).mount("#app");
