//引入Vue
import Vue from 'vue'
//引入App
import App from './App.vue'
//引入VueRouter
import VueRouter from 'vue-router'
//引入路由器
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import axios from "axios";
import lang from 'element-ui/lib/locale/lang/en'
import locale from 'element-ui/lib/locale'

Vue.use(ElementUI)
// 设置语言
locale.use(lang)
Vue.use(ElementUI, { locale })
//关闭Vue的生产提示
Vue.config.productionTip = false
//应用插件
Vue.use(VueRouter)
Vue.prototype.$axios = axios;  //写成原型属性，
Vue.prototype.$apiUrl = 'http://192.168.1.251:8080'
//创建vm
new Vue({
	el:'#app',
	render: h => h(App),
	router:router
})
