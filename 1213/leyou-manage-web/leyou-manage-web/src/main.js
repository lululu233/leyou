// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import Vuetify from 'vuetify'
import config from './config'
import MyComponent from './components/MyComponent'
import './http';
import 'vuetify/dist/vuetify.min.css'
import qs from 'qs'
import 'element-ui/lib/theme-chalk/index.css';
import './assets/material.css';
import List from './List.vue';
import ElementUI from 'element-ui'
import echarts from 'echarts'

Vue.prototype.$echarts = echarts
Vue.use(ElementUI)
Vue.use(Vuetify, { theme: config.theme})
Vue.use(MyComponent)
Vue.prototype.$qs = qs;
Vue.prototype.verify = function () {
  return this.$http.get("/auth/verify")
};

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App,List },
  template: '<App/>'
})
