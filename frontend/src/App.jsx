import { useState } from 'react'
import { TeamPage } from './features/teams/TeamPage'
import { ProjectPage } from './features/projects/ProjectPage'
import { EmployeePage } from './features/employees/EmployeePage'
import { PlanningPage } from './features/planning/PlanningPage'

function App() {
  const [section, setSection] = useState('teams')
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const navigate = (nextSection) => {
    setSection(nextSection)
    setSidebarOpen(false)
  }

  return (
    <main className="app-frame">
      <button className="mobile-menu-button" type="button" aria-expanded={sidebarOpen} aria-controls="main-sidebar" onClick={() => setSidebarOpen((open) => !open)}>
        <span aria-hidden="true">☰</span> Menu
      </button>
      <Sidebar section={section} onNavigate={navigate} isOpen={sidebarOpen} />
      {sidebarOpen && <button className="sidebar-scrim" type="button" aria-label="Fechar menu" onClick={() => setSidebarOpen(false)} />}
      <div className="app-main">
        <header className="mobile-header"><span className="brand-name">TeamFlow</span><span className="environment-label">Contexto local</span></header>
        <div className="page-content">
          {section === 'teams' ? <TeamPage /> : section === 'projects' ? <ProjectPage /> : section === 'employees' ? <EmployeePage /> : <PlanningPage />}
        </div>
      </div>
    </main>
  )
}

function Sidebar({ section, onNavigate, isOpen }) {
  return (
    <aside id="main-sidebar" className={isOpen ? 'sidebar is-open' : 'sidebar'}>
      <div className="sidebar-brand"><span className="brand-mark" aria-hidden="true">TF</span><span className="brand-name">TeamFlow</span></div>
      <div className="sidebar-section">
        <p className="sidebar-label">Cadastros</p>
        <button className={section === 'teams' ? 'sidebar-link active' : 'sidebar-link'} type="button" onClick={() => onNavigate('teams')}>Equipes</button>
        <button className={section === 'projects' ? 'sidebar-link active' : 'sidebar-link'} type="button" onClick={() => onNavigate('projects')}>Projetos</button>
        <button className={section === 'employees' ? 'sidebar-link active' : 'sidebar-link'} type="button" onClick={() => onNavigate('employees')}>Funcionários</button>
        <button className={section === 'planning' ? 'sidebar-link active' : 'sidebar-link'} type="button" onClick={() => onNavigate('planning')}>Planejamento</button>
      </div>
      <div className="sidebar-context"><span>Organização</span><strong>Contexto local</strong></div>
    </aside>
  )
}

export default App
