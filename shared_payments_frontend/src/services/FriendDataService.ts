import apiClient from '@/http-common'

class FriendDataService {
  getAll (groupId: any): Promise<any> {
    return apiClient.get(`/groups/${groupId}/friends`)
  }

  create (groupId: any, data: any): Promise<any> {
    return apiClient.post(`/groups/${groupId}/friends`, data)
  }

  get (groupId: any, id: any): Promise<any> {
    return apiClient.get(`/groups/${groupId}/friends${id}`)
  }

  update (groupId: any, id: any, data: any): Promise<any> {
    return apiClient.put(`/groups/${groupId}/friends${id}`, data)
  }

  delete (groupId: any, id: any): Promise<any> {
    return apiClient.delete(`/groups/${groupId}/friends${id}`)
  }
}

export default new FriendDataService()
