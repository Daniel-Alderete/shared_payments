import axios from 'axios'

export default axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 30000,
  headers: {
    'Content-type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE'
  },
  auth: {
    username: 'vuejs-client',
    password: 'youshallnotpassword'
  }
})
