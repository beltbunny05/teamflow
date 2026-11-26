import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/PageHeader'
import { ErrorMessage, EmptyState, LoadingState } from '../../components/Feedback'
import { FormActions, FormHeading } from '../../components/FormActions'
import { PLANNING_API_URL } from '../../app/config'
import { request } from '../../services/httpClient'

const EMPTY = { projectId: '', resourceType: 'TEAM', resourceId: '', startDate: '', endDate: '', fixed: false }

export function PlanningPage() {
  const [allocations, setAllocations] = useState([])
  const [transports, setTransports] = useState([])
  const [proposals, setProposals] = useState([])
  const [form, setForm] = useState(EMPTY)
  const [transportDurations, setTransportDurations] = useState({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const load = async () => {
    setLoading(true); setError('')
    try {
      const [a, t, p] = await Promise.all([
        request(PLANNING_API_URL, '/planning/allocations'),
        request(PLANNING_API_URL, '/planning/transports'),
        request(PLANNING_API_URL, '/planning/proposals'),
      ])
      setAllocations(a); setTransports(t); setProposals(p)
    } catch (e) { setError(e.message) } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const saveAllocation = async (event) => {
    event.preventDefault(); setSaving(true); setError(''); setNotice('')
    try {
      await request(PLANNING_API_URL, '/planning/allocations', { method: 'POST', body: JSON.stringify({ ...form, projectId: form.projectId, resourceId: form.resourceId }) })
      setForm(EMPTY); setNotice('Alocação criada e validada.'); await load()
    } catch (e) { setError(e.message) } finally { setSaving(false) }
  }

  const changeFixed = async (allocation) => {
    try { await request(PLANNING_API_URL, `/planning/allocations/${allocation.id}/fixed?value=${!allocation.fixed}&expectedUpdatedAt=${encodeURIComponent(allocation.updatedAt)}`, { method: 'POST' }); await load() } catch (e) { setError(e.message) }
  }
  const cancelAllocation = async (allocation) => {
    if (!window.confirm('Cancelar esta alocação?')) return
    try { await request(PLANNING_API_URL, `/planning/allocations/${allocation.id}?expectedUpdatedAt=${encodeURIComponent(allocation.updatedAt)}`, { method: 'DELETE' }); await load() } catch (e) { setError(e.message) }
  }
  const generate = async () => {
    setError(''); setNotice('Gerando proposta…')
    try { const result = await request(PLANNING_API_URL, '/planning/proposals', { method: 'POST' }); setNotice(`Proposta ${result.version} criada para revisão.`); await load() } catch (e) { setError(e.message); setNotice('') }
  }
  const approve = async (proposal, itemIds) => {
    try { await request(PLANNING_API_URL, `/planning/proposals/${proposal.id}/approve`, { method: 'POST', body: JSON.stringify(itemIds ? { itemIds } : {}) }); setNotice(`Proposta ${proposal.version} aprovada.`); await load() } catch (e) { setError(e.message) }
  }
  const updateProposal = async (proposal, items, explanation) => {
    try { await request(PLANNING_API_URL, `/planning/proposals/${proposal.id}`, { method: 'PUT', body: JSON.stringify({ items, explanation }) }); setNotice(`Revisão da proposta ${proposal.version} salva.`); await load() } catch (e) { setError(e.message) }
  }
  const reject = async (proposal) => { try { await request(PLANNING_API_URL, `/planning/proposals/${proposal.id}/reject`, { method: 'POST' }); await load() } catch (e) { setError(e.message) } }
  const defineTransport = async (transport) => {
    const durationDays = Number(transportDurations[transport.id])
    if (!durationDays || durationDays < 1) { setError('Informe uma duração de transporte em dias de calendário.'); return }
    try { await request(PLANNING_API_URL, `/planning/transports/${transport.id}`, { method: 'PUT', body: JSON.stringify({ resourceId: transport.resourceId, resourceType: transport.resourceType, origin: transport.origin, destination: transport.destination, startDate: transport.startDate, durationDays, expectedUpdatedAt: transport.updatedAt }) }); await load() } catch (e) { setError(e.message) }
  }

  return <section>
    <PageHeader title="Planejamento" description="Crie compromissos manuais, revise propostas explicáveis e só confirme alterações após aprovação." actionLabel="Gerar proposta automática" onAction={generate} />
    {error && <ErrorMessage>{error}</ErrorMessage>}
    {notice && <p className="success-message">{notice}</p>}
    <div className="content-section">
      <form className="form-layout" onSubmit={saveAllocation}>
        <FormHeading eyebrow="ALOCAÇÃO MANUAL" title="Adicionar compromisso" description="Use IDs de projeto e recurso cadastrados. A validação consulta os serviços de origem." />
        <div className="form-grid"><label>Projeto<input required value={form.projectId} onChange={e => setForm({ ...form, projectId: e.target.value })} placeholder="UUID" /></label><label>Tipo de recurso<select value={form.resourceType} onChange={e => setForm({ ...form, resourceType: e.target.value })}><option>TEAM</option><option>EMPLOYEE</option></select></label><label>Recurso<input required value={form.resourceId} onChange={e => setForm({ ...form, resourceId: e.target.value })} placeholder="UUID" /></label><label>Início<input required type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })} /></label><label>Fim<input required type="date" value={form.endDate} onChange={e => setForm({ ...form, endDate: e.target.value })} /></label></div>
        <label><input type="checkbox" checked={form.fixed} onChange={e => setForm({ ...form, fixed: e.target.checked })} /> Fixar compromisso</label>
        <FormActions saving={saving} onCancel={() => setForm(EMPTY)} />
      </form>
      {loading ? <LoadingState label="Carregando planejamento…" /> : <>
        <h2>Alocações confirmadas</h2>
        {allocations.length === 0 ? <EmptyState title="Nenhuma alocação confirmada" description="Crie uma alocação manual ou aprove uma proposta." /> : <div className="table-wrap"><table><thead><tr><th>Projeto</th><th>Recurso</th><th>Período</th><th>Fixado</th><th>Situação</th><th>Ações</th></tr></thead><tbody>{allocations.map(a => <tr key={a.id}><td>{a.projectId}</td><td>{a.resourceType} · {a.resourceId}</td><td>{a.startDate} → {a.endDate}</td><td>{a.fixed ? 'Sim' : 'Não'}</td><td>{a.status}</td><td><button className="text-button" type="button" onClick={() => changeFixed(a)}>{a.fixed ? 'Liberar' : 'Fixar'}</button> <button className="text-button danger" type="button" onClick={() => cancelAllocation(a)}>Cancelar</button></td></tr>)}</tbody></table></div>}
        <h2>Transportes</h2>
        {transports.length === 0 ? <p className="muted">Nenhum deslocamento registrado.</p> : <div className="table-wrap"><table><thead><tr><th>Recurso</th><th>Origem → destino</th><th>Início</th><th>Duração</th><th>Situação</th><th></th></tr></thead><tbody>{transports.map(t => <tr key={t.id}><td>{t.resourceType} · {t.resourceId}</td><td>{t.origin} → {t.destination}</td><td>{t.startDate}</td><td>{t.durationDays ?? <input className="inline-input" type="number" min="1" value={transportDurations[t.id] || ''} onChange={e => setTransportDurations({ ...transportDurations, [t.id]: e.target.value })} placeholder="dias" />}</td><td>{t.status}</td><td>{t.status === 'PENDING' && <button className="text-button" type="button" onClick={() => defineTransport(t)}>Confirmar duração</button>}</td></tr>)}</tbody></table></div>}
        <h2>Propostas</h2>
        {proposals.length === 0 ? <p className="muted">Gere uma proposta para avaliar alocações, pendências e projetos não atendidos.</p> : proposals.map(p => <ProposalCard key={p.id} proposal={p} onApprove={approve} onReject={reject} onUpdate={updateProposal} />)}
      </>}
    </div>
  </section>
}

function ProposalCard({ proposal, onApprove, onReject, onUpdate }) {
  const [editedItems, setEditedItems] = useState(proposal.payload?.items || [])
  const [selectedIds, setSelectedIds] = useState(() => new Set())
  const items = proposal.payload?.items || []
  const unattended = proposal.payload?.unattendedProjects || []
  useEffect(() => { setEditedItems(items); setSelectedIds(new Set(items.filter(item => actionable(item) && item.status !== 'PENDING_TRANSPORT' && item.status !== 'BLOCKING').map(item => item.itemId))) }, [proposal.id, proposal.updatedAt])
  const actionableItems = editedItems.filter(actionable)
  const selectable = actionableItems.filter(item => item.status !== 'PENDING_TRANSPORT' && item.status !== 'BLOCKING')
  const changeItem = (itemId, changes) => setEditedItems(current => current.map(item => item.itemId === itemId ? { ...item, ...changes } : item))
  const toggleSelection = (itemId) => setSelectedIds(current => { const next = new Set(current); next.has(itemId) ? next.delete(itemId) : next.add(itemId); return next })
  const saveReview = () => onUpdate(proposal, editedItems, proposal.explanation)
  return <article className="proposal-card"><div className="proposal-heading"><div><span className="eyebrow">VERSÃO {proposal.version}</span><h3>{proposal.status}</h3></div>{proposal.status === 'DRAFT' && <div><button className="button secondary" type="button" onClick={() => onApprove(proposal)}>Aprovar tudo</button> <button className="text-button danger" type="button" onClick={() => onReject(proposal)}>Descartar</button></div>}</div><p>{proposal.explanation}</p><p className="muted">Itens: {items.length} · Projetos não atendidos: {unattended.length} · Revisão-base: {proposal.baseRevision}</p>{editedItems.length > 0 && <ul className="proposal-list">{editedItems.map(item => <li key={item.itemId}><label className="proposal-item-line">{proposal.status === 'DRAFT' && actionable(item) && <input type="checkbox" checked={selectedIds.has(item.itemId)} disabled={item.status === 'PENDING_TRANSPORT' || item.status === 'BLOCKING'} onChange={() => toggleSelection(item.itemId)} />}<span><strong>{item.operation}</strong> · {item.projectIdentification} · {item.resourceIdentification} <span className="status-pill">{item.status}</span>{(item.operation === 'CREATE' || item.operation === 'UPDATE') && proposal.status === 'DRAFT' && <span className="inline-fields"><input type="date" value={item.startDate || ''} onChange={e => changeItem(item.itemId, { startDate: e.target.value })} /><input type="date" value={item.endDate || ''} onChange={e => changeItem(item.itemId, { endDate: e.target.value })} /></span>}{item.operation === 'TRANSPORT' && proposal.status === 'DRAFT' && <span className="inline-fields"><span>{item.origin || 'origem pendente'} → {item.destination || 'destino pendente'}</span><input className="inline-input" type="number" min="1" placeholder="dias" value={item.durationDays || ''} onChange={e => changeItem(item.itemId, { durationDays: e.target.value ? Number(e.target.value) : null })} /></span>}<small>{item.startDate} → {item.endDate} · {item.explanation}</small></span></label></li>)}</ul>}{unattended.length > 0 && <p className="warning-message">Projetos sem atendimento: {unattended.map(item => item.projectIdentification).join(', ')}. Alternativas foram incluídas na proposta.</p>}{proposal.status === 'DRAFT' && <div className="proposal-actions"><button className="text-button" type="button" onClick={saveReview}>Salvar revisão</button>{selectable.length > 0 && <button className="text-button" type="button" onClick={() => onApprove(proposal, [...selectedIds])}>Aprovar seleção</button>}</div>}{proposal.payload?.approvalComplete === false && <p className="warning-message">Aprovação parcial: o planejamento permanece incompleto e requer revisão.</p>}</article>
}

function actionable(item) { return ['CREATE', 'TRANSPORT', 'CANCEL', 'UPDATE'].includes(item.operation) }
