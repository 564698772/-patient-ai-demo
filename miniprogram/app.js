App({
  globalData: {
    location: null,      // { latitude, longitude }
    apiBase: 'https://hwjhwjhwj.asia'
  },

  onLaunch() {
    this.fetchLocation()
  },

  fetchLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        }
      },
      fail: () => {
        console.warn('位置获取失败')
      }
    })
  },

  // 封装 POST 请求
  request(path, data) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: this.globalData.apiBase + '/api' + path,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data,
        success: (res) => {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(res.data)
          } else {
            reject(new Error(res.data?.message || '请求失败'))
          }
        },
        fail: (err) => reject(new Error(err.errMsg || '网络错误'))
      })
    })
  },

  call120() {
    wx.makePhoneCall({
      phoneNumber: '120',
      fail: () => {
        wx.showToast({ title: '请手动拨打120', icon: 'none' })
      }
    })
  }
})
