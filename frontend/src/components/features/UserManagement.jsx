import React, { useState, useEffect } from 'react';
import UserService from '../../services/UserService';
import RoleService from '../../services/RoleService';
import '../../styles/main.css';

function UserManagement() {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [formData, setFormData] = useState({ name: '', email: '', password: '', vigencia: true, role: null });
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Estados para búsqueda y paginación
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [usersPerPage, setUsersPerPage] = useState(10);
  
  // Estado para el modal
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    loadUsers();
    loadRoles();
  }, []);

  const loadRoles = async () => {
    try {
      const data = await RoleService.getAllRoles();
      setRoles(data);
    } catch (err) {
      // Si es error 401, no mostrar mensaje porque ya se redirigirá al login
      if (err.response?.status !== 401) {
        console.error('Error al cargar roles:', err);
      }
    }
  };

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await UserService.getAllUsers();
      setUsers(data);
    } catch (err) {
      // Si es error 401, no mostrar mensaje porque ya se redirigirá al login
      if (err.response?.status !== 401) {
        setError('Error al cargar usuarios');
      }
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError('');
      if (editingId) {
        await UserService.updateUser(editingId, formData);
        setEditingId(null);
      } else {
        await UserService.createUser(formData);
      }
      setFormData({ name: '', email: '', password: '', vigencia: true, role: null });
      setIsModalOpen(false);
      loadUsers();
    } catch (err) {
      // Mostrar mensaje específico si es error de email duplicado
      if (err.response?.status === 400 && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Error al guardar usuario');
      }
      console.error(err);
    }
  };

  const handleEdit = (user) => {
    setFormData({ name: user.name, email: user.email, password: '', vigencia: user.vigencia, role: user.role });
    setEditingId(user.id);
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de que deseas eliminar este usuario? Esta acción no se puede deshacer.')) {
      try {
        setError('');
        await UserService.deleteUser(id);
        loadUsers();
      } catch (err) {
        setError('Error al eliminar usuario');
        console.error(err);
      }
    }
  };

  const handleCancel = () => {
    setFormData({ name: '', email: '', password: '', vigencia: true, role: null });
    setEditingId(null);
    setIsModalOpen(false);
  };

  const openModal = () => {
    setFormData({ name: '', email: '', password: '', vigencia: true, role: null });
    setEditingId(null);
    setIsModalOpen(true);
  };

  return (
    <>
      {error && <div className="error-message">{error}</div>}

      <div className="users-card">
        <div className="users-header">
          <div className="header-title-row">
            <h2>Lista de Usuarios</h2>
            <button className="btn btn-primary" onClick={openModal}>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              Agregar Usuario
            </button>
          </div>
          
          <div className="users-controls">
            <div className="search-box">
              <input
                type="text"
                placeholder="Buscar por nombre o email..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setCurrentPage(1); // Reiniciar a la primera página al buscar
                }}
                className="search-input"
              />
              <svg className="search-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
            </div>
            
            <div className="per-page-selector">
              <label>Mostrar:</label>
              <select 
                value={usersPerPage} 
                onChange={(e) => {
                  setUsersPerPage(Number(e.target.value));
                  setCurrentPage(1);
                }}
                className="select-small"
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={25}>25</option>
                <option value={50}>50</option>
              </select>
            </div>
          </div>
        </div>

        {loading ? (
          <p className="loading">Cargando...</p>
        ) : users.length === 0 ? (
          <p className="no-users">No hay usuarios registrados</p>
        ) : (
          <>
            {(() => {
              // Filtrar usuarios según término de búsqueda
              const filteredUsers = users.filter(user => 
                user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                user.email.toLowerCase().includes(searchTerm.toLowerCase())
              );

              // Calcular paginación
              const totalUsers = filteredUsers.length;
              const totalPages = Math.ceil(totalUsers / usersPerPage);
              const indexOfLastUser = currentPage * usersPerPage;
              const indexOfFirstUser = indexOfLastUser - usersPerPage;
              const currentUsers = filteredUsers.slice(indexOfFirstUser, indexOfLastUser);

              if (filteredUsers.length === 0) {
                return <p className="no-users">No se encontraron usuarios que coincidan con la búsqueda</p>;
              }

              return (
                <>
                  <div className="users-info">
                    Mostrando {indexOfFirstUser + 1} - {Math.min(indexOfLastUser, totalUsers)} de {totalUsers} usuario{totalUsers !== 1 ? 's' : ''}
                  </div>

                  <div className="table-container">
                    <table className="users-table">
                      <thead>
                        <tr>
                          <th>Nombre</th>
                          <th>Email</th>
                          <th>Rol</th>
                          <th>Estado</th>
                          <th className="actions-column">Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {currentUsers.map((user) => (
                          <tr key={user.id}>
                            <td data-label="Nombre">{user.name}</td>
                            <td data-label="Email">{user.email}</td>
                            <td data-label="Rol">
                              <span className="badge">{user.role?.name || 'Sin rol'}</span>
                            </td>
                            <td data-label="Estado">
                              <span className={`badge ${user.vigencia ? 'badge-success' : 'badge-inactive'}`}>
                                {user.vigencia ? 'Activo' : 'Inactivo'}
                              </span>
                            </td>
                            <td data-label="Acciones" className="actions-column">
                              <div className="action-buttons">
                                <button 
                                  className="btn-action btn-edit" 
                                  onClick={() => handleEdit(user)} 
                                  title="Editar usuario"
                                  aria-label="Editar usuario"
                                >
                                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                  </svg>
                                </button>
                                <button 
                                  className="btn-action btn-delete" 
                                  onClick={() => handleDelete(user.id)} 
                                  title="Eliminar usuario"
                                  aria-label="Eliminar usuario"
                                >
                                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                    <polyline points="3 6 5 6 21 6"></polyline>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                    <line x1="10" y1="11" x2="10" y2="17"></line>
                                    <line x1="14" y1="11" x2="14" y2="17"></line>
                                  </svg>
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {totalPages > 1 && (
                    <div className="pagination">
                      <button
                        onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                        disabled={currentPage === 1}
                        className="btn btn-secondary btn-small"
                      >
                        Anterior
                      </button>
                      
                      <div className="pagination-info">
                        {(() => {
                          // Mostrar máximo 5 números de página
                          const pageNumbers = [];
                          let startPage = Math.max(1, currentPage - 2);
                          let endPage = Math.min(totalPages, currentPage + 2);

                          // Ajustar si estamos al inicio o al final
                          if (currentPage <= 3) {
                            endPage = Math.min(5, totalPages);
                          }
                          if (currentPage >= totalPages - 2) {
                            startPage = Math.max(1, totalPages - 4);
                          }

                          for (let i = startPage; i <= endPage; i++) {
                            pageNumbers.push(
                              <button
                                key={i}
                                onClick={() => setCurrentPage(i)}
                                className={`btn ${currentPage === i ? 'btn-primary' : 'btn-secondary'} btn-small`}
                              >
                                {i}
                              </button>
                            );
                          }

                          return pageNumbers;
                        })()}
                      </div>

                      <button
                        onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                        disabled={currentPage === totalPages}
                        className="btn btn-secondary btn-small"
                      >
                        Siguiente
                      </button>
                    </div>
                  )}
                </>
              );
            })()}
          </>
        )}
      </div>

      {isModalOpen && (
        <div className="modal-overlay" onClick={handleCancel}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingId ? 'Editar Usuario' : 'Agregar Usuario'}</h2>
              <button className="modal-close" onClick={handleCancel}>
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
            <div className="modal-body">
              <form onSubmit={handleSubmit} key={editingId || 'new'}>

          <div className="form-group">
            <label>Nombre</label>
            <input
              type="text"
              placeholder="Nombre completo"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
              maxLength={100}
            />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="usuario@empresa.com"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              required
              autoComplete="email"
              disabled={!!editingId}
              style={editingId ? { backgroundColor: '#f0f0f0', cursor: 'not-allowed' } : {}}
              maxLength={100}
            />
          </div>
          <div className="form-group">
            <label>Contraseña</label>
            <input
              type="password"
              placeholder={editingId ? "Dejar vacío para no cambiar" : "Contraseña temporal"}
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              required={!editingId}
              autoComplete="new-password"
              minLength={6}
              maxLength={50}
            />
          </div>
          <div className="form-group">
            <label>Rol</label>
            <select
              value={formData.role?.id || ''}
              onChange={(e) => {
                const selectedRole = roles.find(r => r.id === parseInt(e.target.value));
                setFormData({ ...formData, role: selectedRole || null });
              }}
              required
            >
              <option value="">Seleccionar rol...</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Estado</label>
            <div className="toggle-switch-container">
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={formData.vigencia}
                  onChange={(e) => setFormData({ ...formData, vigencia: e.target.checked })}
                />
                <span className="toggle-slider"></span>
              </label>
              <span className={`toggle-label ${formData.vigencia ? 'active' : 'inactive'}`}>
                {formData.vigencia ? 'Usuario Activo' : 'Usuario Inactivo'}
              </span>
            </div>
          </div>
          <div className="button-group">
            <button type="submit" className="btn btn-primary">
              {editingId ? 'Actualizar' : 'Agregar'}
            </button>
            {editingId && (
              <button type="button" className="btn btn-secondary" onClick={handleCancel}>
                Cancelar
              </button>
            )}
          </div>
        </form>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default UserManagement;
