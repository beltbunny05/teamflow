const services = [
  { name: 'Identity Service', port: '8081', status: 'Scaffold' },
  { name: 'Team Service', port: '8082', status: 'Scaffold' },
  { name: 'Project Service', port: '8083', status: 'Scaffold' },
  { name: 'Planning Service', port: '8084', status: 'Scaffold' },
]

function App() {
  return (
    <main className="shell">
      <header className="hero">
        <p className="eyebrow">TEAMFLOW MVP</p>
        <h1>Planejamento de equipes, com clareza.</h1>
        <p className="intro">
          Fundação executável preparada para receber os fluxos de equipes,
          projetos, alocações e planejamento.
        </p>
      </header>

      <section className="status-card" aria-labelledby="services-title">
        <div className="section-heading">
          <div>
            <p className="eyebrow">AMBIENTE LOCAL</p>
            <h2 id="services-title">Serviços preparados</h2>
          </div>
          <span className="badge">Sem regras de negócio</span>
        </div>

        <div className="service-grid">
          {services.map((service) => (
            <article className="service" key={service.name}>
              <span className="service-dot" aria-hidden="true" />
              <div>
                <h3>{service.name}</h3>
                <p>Porta local {service.port}</p>
              </div>
              <span className="service-status">{service.status}</span>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}

export default App
