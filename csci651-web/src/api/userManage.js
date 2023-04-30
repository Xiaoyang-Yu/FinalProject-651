import request from '@/utils/request'

export default {
  getListDefault(searchModel) {
    return request({
      url: '/parts/importTxt',
      method: 'get',
      params: {
        id: searchModel.id,
      }
    })
  },
  addUser(user) {
    return request({
      url: '/user',
      method: 'post',
      data: user
    })
  },
  updateUser(user) {
    return request({
      url: '/user',
      method: 'put',
      data: user
    })
  },
  // 在这个方法判断，决定点击表单确定按钮，是新增还是修改
  saveUser(user) {
    if (user.id == null && user.id === undefined) {
      return this.addUser(user)
    }
    return this.updateUser(user)
  },
  getUserById(id) {
    return request({
      // url: '/user',
      url: `/user/${id}`,
      method: 'get'
    })
  },
  deleteUserById(id) {
    return request({
      // url: '/user',
      url: `/user/${id}`,
      method: 'delete'
    })
  }
}
