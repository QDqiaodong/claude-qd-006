import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 10000 })

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const msg = err?.response?.data?.message || err.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

export const kitchenApi = {
  list: (params) => http.get('/kitchens', { params }),
  create: (data) => http.post('/kitchens', data),
  update: (id, data) => http.put(`/kitchens/${id}`, data)
}

export const equipmentApi = {
  list: (params) => http.get('/equipments', { params }),
  create: (data) => http.post('/equipments', data),
  update: (id, data) => http.put(`/equipments/${id}`, data)
}

export const dishApi = {
  list: (params) => http.get('/dishes', { params }),
  create: (data) => http.post('/dishes', data),
  update: (id, data) => http.put(`/dishes/${id}`, data)
}

export const batchApi = {
  list: (params) => http.get('/batches', { params }),
  open: (data) => http.post('/batches', data),
  advance: (id, action, actualPortions) =>
    http.post(`/batches/${id}/advance`, null, { params: { action, actualPortions } })
}

export const slotApi = {
  list: (params) => http.get('/slots', { params }),
  open: (data) => http.post('/slots', data),
  advance: (id, action) => http.post(`/slots/${id}/advance`, null, { params: { action } })
}

export const deliveryApi = {
  list: (params) => http.get('/deliveries', { params }),
  open: (data) => http.post('/deliveries', data),
  advance: (id, action, driver, deliverDate) =>
    http.post(`/deliveries/${id}/advance`, null, { params: { action, driver, deliverDate } })
}

export default http
