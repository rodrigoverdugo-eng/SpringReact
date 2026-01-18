import React from 'react'
import ReactDOM from 'react-dom/client'
import './styles/main.css'
import App from './App.jsx'
import AuthService from './services/AuthService'

// Configurar interceptor de Axios al iniciar la aplicación
AuthService.setupAxiosInterceptor();

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
