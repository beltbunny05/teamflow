import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/PageHeader'
import { ErrorMessage, LoadingState } from '../../components/Feedback'
import { PLANNING_API_URL } from '../../app/config'
import { request } from '../../services/httpClient'

export function PlanningPage() { const [allocations, setAllocations] = useState([]); const [transports, setTransports] = useState([]); const [error, setError] = useState(''); const [loading, setLoading] = useState(true)
  const load = async () => { setLoading(true); try { const [a, t] = await Promise.all([request(PLANNING_API_URL, '/planning/allocations'), request(PLANNING_API_URL, '/planning/transports')]); setAllocations(a); setTransports(t) } catch (e) { setError(e.message) } finally { setLoading(false) } }
  useEffect(() => { load() }, [])
  const proposal = async () => { try { const result = await request(PLANNING_API_URL, '/planning/proposals', { method: 'POST' }); window.alert(`Proposta ${result.version} criada para revisão.`) } catch (e) { setError(e.message) } }
  return <section><PageHeader title="Planejamento" description="Consulte compromissos, transportes e gere propostas que só alteram o planejamento após aprovação." actionLabel="Gerar proposta" onAction={proposal} />{error && <ErrorMessage>{error}</ErrorMessage>}{loading ? <LoadingState label="Carregando planejamento…" /> : <div className="content-section"><h2>Alocações confirmadas</h2><div className="table-wrap"><table><thead><tr><th>Projeto</th><th>Recurso</th><th>Período</th><th>Fixado</th><th>Situação</th></tr></thead><tbody>{allocations.length ? allocations.map(a => <tr key={a.id}><td>{a.projectId}</td><td>{a.resourceType} · {a.resourceId}</td><td>{a.startDate} → {a.endDate}</td><td>{a.fixed ? 'Sim' : 'Não'}</td><td>{a.status}</td></tr>) : <tr><td colSpan="5">Nenhuma alocação confirmada.</td></tr>}</tbody></table></div><h2>Transportes</h2><div className="table-wrap"><table><thead><tr><th>Recurso</th><th>Origem → destino</th><th>Início</th><th>Duração</th><th>Situação</th></tr></thead><tbody>{transports.length ? transports.map(t => <tr key={t.id}><td>{t.resourceType} · {t.resourceId}</td><td>{t.origin} → {t.destination}</td><td>{t.startDate}</td><td>{t.durationDays ?? 'Pendente'}</td><td>{t.status}</td></tr>) : <tr><td colSpan="5">Nenhum transporte identificado.</td></tr>}</tbody></table></div></div>}</section>
}
