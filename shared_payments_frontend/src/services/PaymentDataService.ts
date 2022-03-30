import apiClient from '@/http-common'

class PaymentDataService {
  getAll (groupId: any, friendId: any): Promise<any> {
    return apiClient.get(`/groups/${groupId}/friends/${friendId}/payments`)
  }

  create (groupId: any, friendId: any, data: any): Promise<any> {
    return apiClient.post(`/groups/${groupId}/friends/${friendId}/payments`, data)
  }

  get (groupId: any, friendId: any, id: any): Promise<any> {
    return apiClient.get(`/groups/${groupId}/friends/${friendId}/payments/${id}`)
  }

  update (groupId: any, friendId: any, id: any, data: any): Promise<any> {
    return apiClient.put(`/groups/${groupId}/friends/${friendId}/payments/${id}`, data)
  }

  delete (groupId: any, friendId: any, id: any): Promise<any> {
    return apiClient.delete(`/groups/${groupId}/friends/${friendId}/payments/${id}`)
  }
}

export default new PaymentDataService()
