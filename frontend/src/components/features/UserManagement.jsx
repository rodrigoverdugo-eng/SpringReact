import React, { useState, useEffect } from 'react';
import UserService from '../../services/UserService';
import RoleService from '../../services/RoleService';
import { CheckCircle, AlertCircle, X, Plus, Search, Edit, Clock, Trash2, Check, Copy, Eye, EyeOff, RefreshCw } from 'lucide-react';
import '../../styles/main.css';

function UserManagement() {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [formData, setFormData] = useState({ name: '', email: '', password: '', vigencia: true, requiresPasswordChange: true, role: null });
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [toasts, setToasts] = useState([]);
  
  // Estados para búsqueda y paginación
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [usersPerPage, setUsersPerPage] = useState(10);
  
  // Estado para el modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalError, setModalError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [passwordCopied, setPasswordCopied] = useState(false);

  // Estado para modal de actividad
  const [activityModal, setActivityModal] = useState({ open: false, userName: '', history: [], loading: false });

  // Último acceso por userId
  const [lastLogins, setLastLogins] = useState({});

  const generatePassword = () => {
    const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lower = 'abcdefghijklmnopqrstuvwxyz';
    const digits = '0123456789';
    const special = '!@#$%&*';
    const all = upper + lower + digits + special;
    const array = new Uint32Array(12);
    crypto.getRandomValues(array);
    let pwd = [
      upper[array[0] % upper.length],
      lower[array[1] % lower.length],
      digits[array[2] % digits.length],
      special[array[3] % special.length],
    ];
    for (let i = 4; i < 12; i++) {
      pwd.push(all[array[i] % all.length]);
    }
    // Mezclar
    const shuffled = new Uint32Array(pwd.length);
    crypto.getRandomValues(shuffled);
    pwd.sort((_, __) => shuffled[pwd.indexOf(_)] - shuffled[pwd.indexOf(__)]);
    const password = pwd.join('');
    setFormData(prev => ({ ...prev, password }));
    setShowPassword(true);
  };

  const copyPassword = () => {
    if (!formData.password) return;
    navigator.clipboard.writeText(formData.password).then(() => {
      setPasswordCopied(true);
      setTimeout(() => setPasswordCopied(false), 2000);
    });
  };

  // Estado para el diálogo de confirmación
  const [confirmDialog, setConfirmDialog] = useState({ open: false, userId: null, userName: '' });

  const showToast = (type, title, text) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, type, title, text, hiding: false }]);
    setTimeout(() => {
      setToasts(prev => prev.map(t => t.id === id ? { ...t, hiding: true } : t));
      setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 300);
    }, 4000);
  };

  const dismissToast = (id) => {
    setToasts(prev => prev.map(t => t.id === id ? { ...t, hiding: true } : t));
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 300);
  };

  useEffect(() => {
    loadUsers();
    loadRoles();
    loadLastLogins();
  }, []);

  const loadLastLogins = async () => {
    try {
      const data = await UserService.getAllLastLogins();
      const map = {};
      data.forEach(item => { map[item.userId] = item.lastLoginAt; });
      setLastLogins(map);
    } catch (err) {
      console.error('Error al cargar últimos accesos:', err);
    }
  };

  const loadRoles = async () => {
    try {
      const data = await RoleService.getAllRoles();
      setRoles(data);
    } catch (err) {
      if (err.response?.status !== 401) {
        console.error('Error al cargar roles:', err);
      }
    }
  };

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await UserService.getAllUsers();
      setUsers(data);
      // Refrescar últimos accesos también
      loadLastLogins();
    } catch (err) {
      if (err.response?.status !== 401) {
        showToast('error', 'Error', 'No se pudieron cargar los usuarios');
      }
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validación manual en español
    if (!formData.name.trim()) {
      setModalError('El nombre es obligatorio.');
      return;
    }
    if (!editingId && !formData.email.trim()) {
      setModalError('El email es obligatorio.');
      return;
    }
    if (!editingId && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      setModalError('Ingresa un email válido.');
      return;
    }
    if (!editingId && !formData.password) {
      setModalError('La contraseña es obligatoria.');
      return;
    }
    if (!editingId && formData.password.length < 6) {
      setModalError('La contraseña debe tener al menos 6 caracteres.');
      return;
    }
    if (editingId && formData.password && formData.password.length < 6) {
      setModalError('La contraseña debe tener al menos 6 caracteres.');
      return;
    }
    if (!formData.role) {
      setModalError('Debes seleccionar un rol.');
      return;
    }

    try {
      if (editingId) {
        await UserService.updateUser(editingId, formData);
        setEditingId(null);
        setFormData({ name: '', email: '', password: '', vigencia: true, requiresPasswordChange: true, role: null });
        setIsModalOpen(false);
        loadUsers();
        showToast('success', 'Usuario actualizado', `${formData.name} fue actualizado correctamente`);
      } else {
        await UserService.createUser(formData);
        setFormData({ name: '', email: '', password: '', vigencia: true, requiresPasswordChange: true, role: null });
        setIsModalOpen(false);
        loadUsers();
        showToast('success', 'Usuario creado', `${formData.name} fue agregado correctamente`);
      }
    } catch (err) {
      if (err.response?.status === 400 && err.response?.data?.message) {
        setModalError(err.response.data.message);
      } else {
        setModalError('Error al guardar usuario');
      }
      console.error(err);
    }
  };



  const handleDelete = (id) => {
    const user = users.find(u => u.id === id);
    setConfirmDialog({ open: true, userId: id, userName: user?.name || 'este usuario' });
  };

  const confirmDelete = async () => {
    const { userId, userName } = confirmDialog;
    setConfirmDialog({ open: false, userId: null, userName: '' });
    try {
      await UserService.deleteUser(userId);
      loadUsers();
      showToast('success', 'Usuario eliminado', `${userName} fue eliminado correctamente`);
    } catch (err) {
      showToast('error', 'Error al eliminar', 'No se pudo eliminar el usuario');
      console.error(err);
    }
  };

  const cancelDelete = () => {
    setConfirmDialog({ open: false, userId: null, userName: '' });
  };

  const openActivityModal = async (user) => {
    setActivityModal({ open: true, userName: user.name, history: [], loading: true });
    try {
      const data = await UserService.getLoginHistory(user.id);
      setActivityModal({ open: true, userName: user.name, history: data, loading: false });
    } catch (err) {
      setActivityModal({ open: true, userName: user.name, history: [], loading: false });
      console.error('Error al cargar historial:', err);
    }
  };

  const closeActivityModal = () => {
    setActivityModal({ open: false, userName: '', history: [], loading: false });
  };

  const handleCancel = () => {
    setFormData({ name: '', email: '', password: '', vigencia: true, requiresPasswordChange: true, role: null });
    setEditingId(null);
    setModalError('');
    setShowPassword(false);
    setPasswordCopied(false);
    setIsModalOpen(false);
  };

  const openModal = () => {
    setFormData({ name: '', email: '', password: '', vigencia: true, requiresPasswordChange: true, role: null });
    setEditingId(null);
    setModalError('');
    setShowPassword(false);
    setPasswordCopied(false);
    setIsModalOpen(true);
  };

  const handleEditOpen = (user) => {
    setFormData({ name: user.name, email: user.email, password: '', vigencia: user.vigencia, requiresPasswordChange: user.requiresPasswordChange ?? true, role: user.role });
    setEditingId(user.id);
    setModalError('');
    setShowPassword(false);
    setPasswordCopied(false);
    setIsModalOpen(true);
  };

  return (
    <>
      {/* Toast container */}
      <div className="toast-container">
        {toasts.map(toast => (
          <div key={toast.id} className={`toast toast-${toast.type}${toast.hiding ? ' toast-hiding' : ''}`}>
            <span className={`toast-icon toast-icon-${toast.type}`}>
              {toast.type === 'success' ? (
                <CheckCircle size={20} strokeWidth={2.5} />
              ) : (
                <AlertCircle size={20} strokeWidth={2.5} />
              )}
            </span>
            <div className="toast-body">
              <div className={`toast-title toast-title-${toast.type}`}>{toast.title}</div>
              <div className="toast-text">{toast.text}</div>
            </div>
            <button className="toast-close" onClick={() => dismissToast(toast.id)} aria-label="Cerrar">
              <X size={16} />
            </button>
          </div>
        ))}
      </div>

      <div className="users-card">
        <div className="users-header">
          <div className="header-title-row">
            <h2>Lista de Usuarios</h2>
            <button className="btn btn-primary" onClick={openModal}>
              <Plus size={16} />
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
              <Search className="search-icon" size={18} />
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
                          <th>Contraseña</th>
                          <th>Último acceso</th>
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
                            <td data-label="Contraseña">
                              <span className={`badge ${user.requiresPasswordChange ? 'badge-warning' : 'badge-success'}`}>
                                {user.requiresPasswordChange ? 'Cambio requerido' : 'Al día'}
                              </span>
                            </td>
                            <td data-label="Último acceso">
                              {lastLogins[user.id]
                                ? new Date(lastLogins[user.id]).toLocaleString('es-ES')
                                : <span className="text-muted">Sin accesos</span>}
                            </td>
                            <td data-label="Acciones" className="actions-column">
                              <div className="action-buttons">
                                <button 
                                  className="btn-action btn-edit" 
                                  onClick={() => handleEditOpen(user)} 
                                  title="Editar usuario"
                                  aria-label="Editar usuario"
                                >
                                  <Edit size={16} />
                                </button>
                                <button 
                                  className="btn-action btn-activity" 
                                  onClick={() => openActivityModal(user)} 
                                  title="Ver actividad"
                                  aria-label="Ver actividad"
                                >
                                  <Clock size={16} />
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

      {confirmDialog.open && (
        <div className="modal-overlay confirm-overlay" onClick={cancelDelete}>
          <div className="confirm-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="confirm-icon-wrapper">
              <Trash2 size={32} />
            </div>
            <h3 className="confirm-title">Eliminar usuario</h3>
            <p className="confirm-message">
              ¿Estás seguro de que deseas eliminar a <strong>{confirmDialog.userName}</strong>?
              <br />
              <span className="confirm-warning">Esta acción no se puede deshacer.</span>
            </p>
            <div className="confirm-actions">
              <button className="btn btn-secondary" onClick={cancelDelete}>Cancelar</button>
              <button className="btn btn-danger" onClick={confirmDelete}>Eliminar</button>
            </div>
          </div>
        </div>
      )}

      {isModalOpen && (
        <div className="modal-overlay" onClick={handleCancel}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingId ? 'Editar Usuario' : 'Agregar Usuario'}</h2>
              <button className="modal-close" onClick={handleCancel}>
                <X size={24} />
              </button>
            </div>
            <div className="modal-body">
              {modalError && <div className="error-message">{modalError}</div>}
              <form onSubmit={handleSubmit} key={editingId || 'new'}>

          <div className="form-group">
            <label>Nombre</label>
            <input
              type="text"
              placeholder="Nombre completo"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
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
              autoComplete="email"
              disabled={!!editingId}
              style={editingId ? { backgroundColor: 'var(--color-border-light)', cursor: 'not-allowed' } : {}}
              maxLength={100}
            />
          </div>
          <div className="form-group">
            <label>Contraseña</label>
            <div className="input-password-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder={editingId ? "Dejar vacío para no cambiar" : "Contraseña temporal"}
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                autoComplete="new-password"
                maxLength={50}
              />
              <div className="password-actions">
                <button
                  type="button"
                  className="toggle-password-btn"
                  onClick={copyPassword}
                  aria-label="Copiar contraseña"
                  title={passwordCopied ? 'Copiado' : 'Copiar contraseña'}
                  disabled={!formData.password}
                >
                  {passwordCopied ? <Check size={18} /> : <Copy size={18} />}
                </button>
                <button
                  type="button"
                  className="toggle-password-btn"
                  onClick={() => setShowPassword(v => !v)}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  title={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>
            <button type="button" className="btn-generate-password" onClick={generatePassword}>
              <RefreshCw size={14} />
              Generar contraseña segura
            </button>
          </div>
          <div className="form-group">
            <label>Rol</label>
            <select
              value={formData.role?.id || ''}
              onChange={(e) => {
                const selectedRole = roles.find(r => r.id === parseInt(e.target.value));
                setFormData({ ...formData, role: selectedRole || null });
              }}
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
          <div className="form-group">
              <label>Cambio de contraseña obligatorio</label>
              <div className="toggle-switch-container">
                <label className="toggle-switch">
                  <input
                    type="checkbox"
                    checked={formData.requiresPasswordChange}
                    onChange={(e) => setFormData({ ...formData, requiresPasswordChange: e.target.checked })}
                  />
                  <span className="toggle-slider"></span>
                </label>
                <span className={`toggle-label ${formData.requiresPasswordChange ? 'inactive' : 'active'}`}>
                  {formData.requiresPasswordChange ? 'Debe cambiar contraseña' : 'Sin cambio requerido'}
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

      {activityModal.open && (
        <div className="modal-overlay" onClick={closeActivityModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '560px' }}>
            <div className="modal-header">
              <h2>Actividad — {activityModal.userName}</h2>
              <button className="modal-close" onClick={closeActivityModal}>
                <X size={24} />
              </button>
            </div>
            <div className="modal-body">
              {activityModal.loading ? (
                <p className="loading">Cargando historial...</p>
              ) : activityModal.history.length === 0 ? (
                <p className="text-muted">No hay registros de acceso para este usuario.</p>
              ) : (
                <div className="table-container">
                  <table className="users-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Fecha y hora</th>
                        <th>IP</th>
                      </tr>
                    </thead>
                    <tbody>
                      {activityModal.history.map((entry, index) => (
                        <tr key={entry.id}>
                          <td>{index + 1}</td>
                          <td>{new Date(entry.loginAt).toLocaleString('es-ES')}</td>
                          <td>{entry.ipAddress || '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default UserManagement;
