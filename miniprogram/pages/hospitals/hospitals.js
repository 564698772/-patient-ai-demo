const app = getApp()

Page({
  data: {
    hospitals: [],
    loading: false,
    hasLocation: false
  },

  onShow() {
    const loc = app.globalData.location
    if (loc) {
      this.setData({ hasLocation: true })
      this.loadHospitals()
    } else {
      this.setData({ hasLocation: false })
    }
  },

  requestLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        app.globalData.location = { latitude: res.latitude, longitude: res.longitude }
        this.setData({ hasLocation: true })
        this.loadHospitals()
      },
      fail: () => {
        wx.showToast({ title: '位置授权失败', icon: 'none' })
      }
    })
  },

  async loadHospitals() {
    const loc = app.globalData.location
    if (!loc) return

    this.setData({ loading: true, hospitals: [] })
    try {
      const hospitals = await app.request('/hospitals', {
        latitude: loc.latitude,
        longitude: loc.longitude,
        department: null
      })
      this.setData({ hospitals: hospitals || [] })
    } catch (e) {
      wx.showToast({ title: '搜索失败，请重试', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  call120() {
    wx.makePhoneCall({
      phoneNumber: '120',
      fail: () => wx.showToast({ title: '请手动拨打 120', icon: 'none' })
    })
  },

  openNavigation(e) {
    const hospital = e.currentTarget.dataset.hospital
    const loc = app.globalData.location
    if (!loc) return

    wx.openLocation({
      latitude: loc.latitude,
      longitude: loc.longitude,
      name: hospital.name,
      address: hospital.address || '',
      scale: 15
    })
  }
})
