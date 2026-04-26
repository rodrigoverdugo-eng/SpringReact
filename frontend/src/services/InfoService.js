import axios from 'axios';

const API_URL = '/api/info';

class InfoService {
  async getVersion() {
    const response = await axios.get(`${API_URL}/version`);
    return response.data.version;
  }
}

export default new InfoService();
