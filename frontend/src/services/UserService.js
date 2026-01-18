import axios from 'axios';

const API_URL = '/api/users';

class UserService {
  getAllUsers() {
    return axios.get(API_URL).then(response => response.data);
  }

  getUserById(id) {
    return axios.get(`${API_URL}/${id}`).then(response => response.data);
  }

  createUser(user) {
    return axios.post(API_URL, user).then(response => response.data);
  }

  updateUser(id, user) {
    return axios.put(`${API_URL}/${id}`, user).then(response => response.data);
  }

  deleteUser(id) {
    return axios.delete(`${API_URL}/${id}`);
  }
}

export default new UserService();
