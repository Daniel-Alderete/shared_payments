import apiClient from '@/http-common'

class GroupDataService {
  getAll (): Promise<any> {
    return apiClient.get('/groups')
  }

  create (data: any): Promise<any> {
    return apiClient.post('/groups', data)
  }

  get (id: any): Promise<any> {
    return apiClient.get(`/groups/${id}`)
  }

  update (id: any, data: any): Promise<any> {
    return apiClient.put(`/groups${id}`, data)
  }

  delete (id: any): Promise<any> {
    return apiClient.delete(`/groups${id}`)
  }

  getInfo (id: any): Promise<any> {
    return apiClient.get(`/groups/${id}/info`)
  }
}

export default new GroupDataService()
