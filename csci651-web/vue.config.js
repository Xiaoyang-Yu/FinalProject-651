const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  publicPath:'./',
  // devServer: {
  //   proxy:"http://172.16.2.96:9000"
  // }
  devServer:{
    proxy:{
      '/yxy': {
        target:'http://192.168.1.251:8090',
        pathRewrite:{'^/yxy':''},
        changeOrigin:true
      }
    }
  }
})
