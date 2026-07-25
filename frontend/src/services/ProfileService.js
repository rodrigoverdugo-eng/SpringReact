import axios from 'axios';

const API_URL = '/api/profile';

const ProfileService = {
  getProfile: async () => {
    const response = await axios.get(API_URL);
    return response.data;
  },

  updateTheme: async (theme) => {
    const response = await axios.put(`${API_URL}/theme`, { theme });
    return response.data;
  },
};

export default ProfileService;
