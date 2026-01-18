import axios from 'axios';

const API_URL = '/api/roles';

class RoleService {
  getAllRoles() {
    return axios.get(API_URL).then(response => response.data);
  }
}

export default new RoleService();
