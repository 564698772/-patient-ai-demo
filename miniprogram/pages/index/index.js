const app = getApp()

Page({
  data: {
    symptoms: '',
    loading: false,
    loadStep: 0,
    result: null,
    hospitals: [],
    error: null,
    quickTags: ['发烧', '头痛', '腹痛', '咳嗽', '胸闷', '腰痛', '皮肤过敏', '眩晕']
  },

  onLoad() {
    // 进入页面时请求位置
    this._requestLocation()
  },

  _requestLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        app.globalData.location = {
          latitude: res.latitude,
          longitude: res.longitude
        }
      },
      fail: () => {
        // 位置获取失败不阻断流程
        wx.showToast({
          title: '位置获取失败，医院推荐不可用',
          icon: 'none',
          duration: 2500
        })
      }
    })
  },

  onSymptomsInput(e) {
    this.setData({ symptoms: e.detail.value })
  },

  appendTag(e) {
    const tag = e.currentTarget.dataset.tag
    const cur = this.data.symptoms
    const sep = cur && !cur.endsWith('、') && !cur.endsWith('，') ? '、' : ''
    this.setData({ symptoms: cur ? cur + sep + tag : tag })
  },

  async handleSubmit() {
    const symptoms = this.data.symptoms.trim()
    if (!symptoms || this.data.loading) return

    this.setData({ loading: true, loadStep: 0, result: null, hospitals: [], error: null })

    // 步骤进度动画
    const stepTimer = setInterval(() => {
      if (this.data.loadStep < 2) {
        this.setData({ loadStep: this.data.loadStep + 1 })
      }
    }, 1600)

    try {
      // 1. AI 分析
      const result = await app.request('/analyze', { symptoms })
      this.setData({ loadStep: 2 })

      // 2. 搜索附近医院
      let hospitals = []
      const loc = app.globalData.location
      if (loc) {
        try {
          hospitals = await app.request('/hospitals', {
            latitude: loc.latitude,
            longitude: loc.longitude,
            department: result.department || null
          })
        } catch (e) {
          console.warn('医院查询失败', e)
        }
      }

      clearInterval(stepTimer)
      this.setData({ loading: false, result, hospitals })

      // 急症时震动提醒
      if (result.isEmergency) {
        wx.vibrateLong()
      }
    } catch (err) {
      clearInterval(stepTimer)
      this.setData({
        loading: false,
        error: err.message || '网络异常，请稍后重试'
      })
    }
  },

  call120() {
    wx.makePhoneCall({
      phoneNumber: '120',
      fail: () => {
        wx.showToast({ title: '请手动拨打 120', icon: 'none' })
      }
    })
  },

  navigateToHospital(e) {
    const hospital = e.currentTarget.dataset.hospital
    const loc = app.globalData.location

    if (!loc) {
      wx.showToast({ title: '无法获取当前位置', icon: 'none' })
      return
    }

    wx.openLocation({
      latitude: loc.latitude,
      longitude: loc.longitude,
      name: hospital.name,
      address: hospital.address || '',
      scale: 15
    })
  },

  reset() {
    this.setData({
      symptoms: '',
      result: null,
      hospitals: [],
      error: null,
      loadStep: 0
    })
    wx.pageScrollTo({ scrollTop: 0, duration: 300 })
  }
})
