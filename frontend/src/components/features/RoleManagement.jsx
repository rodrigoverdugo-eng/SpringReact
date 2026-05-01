import React, { useState, useEffect } from 'react';
import RoleService from '../../services/RoleService';
import '../../styles/main.css';

function RoleManagement() {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadRoles();
  }, []);

  const loadRoles = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await RoleService.getAllRoles();
      setRoles(data);
    } catch (err) {
      setError('Error al cargar roles');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {error && <div className="error-message">{error}</div>}

      <div className="users-card">
        <div className="header-title-row">
          <h2>Lista de Roles</h2>
        </div>
        {loading ? (
          <p className="loading">Cargando...</p>
        ) : roles.length === 0 ? (
          <p className="no-users">No hay roles registrados</p>
        ) : (
          <div className="users-list">
            {roles.map((role) => (
              <div key={role.id} className="user-item">
                <div className="user-info">
                  <h3>{role.name}</h3>
                  <p>{role.descripcion || 'Sin descripción'}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
}

export default RoleManagement;
