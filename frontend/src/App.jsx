import { useState } from 'react'
import { TeamPage } from './features/teams/TeamPage'
import { ProjectPage } from './features/projects/ProjectPage'

function App() {
  const [section, setSection] = useState('teams')

  return (
    <main className="app-shell">
      <header className="app-header">
        <div className="brand-lockup">
          <span className="brand-mark" aria-hidden="true">TF</span>
          <span className="brand-name">TeamFlow</span>
        </div>
        <span className="environment-label">Contexto local</span>
      </header>

      <nav className="main-nav" aria-label="Navegação principal">
        <div className="nav-group">
          <span className="nav-label">Cadastros</span>
          <button className={section === 'teams' ? 'nav-link active' : 'nav-link'} type="button" onClick={() => setSection('teams')}>Equipes</button>
          <button className={section === 'projects' ? 'nav-link active' : 'nav-link'} type="button" onClick={() => setSection('projects')}>Projetos</button>
        </div>
      </nav>

      <div className="page-content">
        {section === 'teams' ? <TeamPage /> : <ProjectPage />}
      </div>
    </main>
  )
}

export default App
