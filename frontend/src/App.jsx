import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_TEAM_SERVICE_URL || 'http://localhost:8082'
const ORGANIZATION_ID = import.meta.env.VITE_ORGANIZATION_ID || '00000000-0000-0000-0000-000000000001'
const EMPTY_FORM = { identification: '', totalMembers: '', status: '', competencies: '', qualifications: '', availability: '', composition: [{ role: '', quantity: '' }] }

async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', 'X-Organization-Id': ORGANIZATION_ID, ...(options.headers || {}) },
  })
  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    const fieldMessage = body.fields ? Object.values(body.fields).join(' ') : ''
    throw new Error(fieldMessage || body.message || 'Não foi possível concluir a operação.')
  }
  return body
}

function toForm(team) {
  return { identification: team.identification || '', totalMembers: team.totalMembers ?? '', status: team.status || '', competencies: (team.competencies || []).join(', '), qualifications: (team.qualifications || []).join(', '), availability: team.availability ? JSON.stringify(team.availability, null, 2) : '', composition: team.composition?.length ? team.composition.map((item) => ({ role: item.role, quantity: item.quantity })) : [{ role: '', quantity: '' }] }
}

function toPayload(form) {
  let availability
  if (form.availability.trim()) availability = JSON.parse(form.availability)
  return { identification: form.identification, totalMembers: Number(form.totalMembers), status: form.status || null, competencies: form.competencies.split(',').map((value) => value.trim()).filter(Boolean), qualifications: form.qualifications.split(',').map((value) => value.trim()).filter(Boolean), availability: availability || null, composition: form.composition.filter((item) => item.role.trim()).map((item) => ({ role: item.role.trim(), quantity: Number(item.quantity) })) }
}

function App() {
  const [teams, setTeams] = useState([])
  const [selectedTeam, setSelectedTeam] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')

  const loadTeams = async () => {
    setLoading(true); setError('')
    try { const result = await request('/teams'); setTeams(result.content || []) } catch (loadError) { setError(loadError.message) } finally { setLoading(false) }
  }

  useEffect(() => { loadTeams() }, [])
  const startCreate = () => { setEditingId(null); setSelectedTeam(null); setForm(EMPTY_FORM); setFormError('') }
  const startEdit = (team) => { setEditingId(team.id); setSelectedTeam(team); setForm(toForm(team)); setFormError('') }
  const updateField = (field, value) => setForm((current) => ({ ...current, [field]: value }))
  const updateComposition = (index, field, value) => setForm((current) => ({ ...current, composition: current.composition.map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item) }))

  const saveTeam = async (event) => {
    event.preventDefault(); setSaving(true); setFormError('')
    try {
      const team = await request(editingId ? `/teams/${editingId}` : '/teams', { method: editingId ? 'PUT' : 'POST', body: JSON.stringify(toPayload(form)) })
      setSelectedTeam(team); setEditingId(team.id); await loadTeams()
    } catch (saveError) { setFormError(saveError.message === 'Unexpected end of JSON input' ? 'Disponibilidade deve conter um JSON válido.' : saveError.message) } finally { setSaving(false) }
  }

  return <main className="shell">
    <header className="hero compact-hero"><p className="eyebrow">TEAMFLOW MVP · TEAM SERVICE</p><div className="hero-row"><div><h1>Equipes que cabem no plano.</h1><p className="intro">Cadastre e consulte equipes agregadas para apoiar o planejamento operacional.</p></div><button className="primary-button" type="button" onClick={startCreate}>Nova equipe</button></div></header>
    {error && <div className="alert error-alert" role="alert">{error}</div>}
    <section className="workspace"><div className="panel list-panel"><div className="section-heading"><div><p className="eyebrow">CADASTRO</p><h2>Equipes</h2></div><span className="count">{teams.length}</span></div>{loading ? <p className="muted">Carregando equipes…</p> : teams.length === 0 ? <div className="empty-state"><h3>Nenhuma equipe cadastrada</h3><p>Comece adicionando a primeira equipe do contexto local.</p><button className="secondary-button" type="button" onClick={startCreate}>Cadastrar equipe</button></div> : <div className="team-list">{teams.map((team) => <button className={`team-item ${selectedTeam?.id === team.id ? 'selected' : ''}`} type="button" key={team.id} onClick={() => { setSelectedTeam(team); setEditingId(null) }}><span className="team-avatar">{team.identification.slice(0, 1).toUpperCase()}</span><span className="team-item-copy"><strong>{team.identification}</strong><small>{team.totalMembers} integrantes · {team.status || 'Situação não informada'}</small></span><span className="chevron">→</span></button>)}</div>}</div>
      <div className="panel detail-panel">{selectedTeam && !editingId ? <TeamDetails team={selectedTeam} onEdit={() => startEdit(selectedTeam)} /> : <form onSubmit={saveTeam}><div className="section-heading"><div><p className="eyebrow">{editingId ? 'EDIÇÃO' : 'NOVO REGISTRO'}</p><h2>{editingId ? 'Editar equipe' : 'Cadastrar equipe'}</h2></div></div>{formError && <div className="alert error-alert" role="alert">{formError}</div>}<label>Nome ou identificação<input required maxLength="150" value={form.identification} onChange={(event) => updateField('identification', event.target.value)} /></label><label>Total de integrantes<input required min="0" type="number" value={form.totalMembers} onChange={(event) => updateField('totalMembers', event.target.value)} /></label><label>Situação <span className="optional">opcional</span><input maxLength="50" value={form.status} onChange={(event) => updateField('status', event.target.value)} placeholder="Ex.: ATIVA" /></label><fieldset><legend>Composição por função <span className="optional">opcional</span></legend>{form.composition.map((item, index) => <div className="composition-row" key={index}><input aria-label="Função" placeholder="Função" value={item.role} onChange={(event) => updateComposition(index, 'role', event.target.value)} /><input aria-label="Quantidade" min="0" type="number" placeholder="Qtd." value={item.quantity} onChange={(event) => updateComposition(index, 'quantity', event.target.value)} /></div>)}<button className="text-button" type="button" onClick={() => setForm((current) => ({ ...current, composition: [...current.composition, { role: '', quantity: '' }] }))}>+ Adicionar função</button></fieldset><label>Competências <span className="optional">opcional · separadas por vírgula</span><input value={form.competencies} onChange={(event) => updateField('competencies', event.target.value)} /></label><label>Qualificações <span className="optional">opcional · separadas por vírgula</span><input value={form.qualifications} onChange={(event) => updateField('qualifications', event.target.value)} /></label><label>Disponibilidade <span className="optional">opcional · JSON preservado sem interpretação</span><textarea rows="4" value={form.availability} onChange={(event) => updateField('availability', event.target.value)} placeholder={'{\n  "observacao": "..."\n}'} /></label><div className="form-actions"><button className="secondary-button" type="button" onClick={() => { setSelectedTeam(null); setEditingId(null) }}>Cancelar</button><button className="primary-button" disabled={saving} type="submit">{saving ? 'Salvando…' : 'Salvar equipe'}</button></div></form>}</div></section>
  </main>
}

function TeamDetails({ team, onEdit }) { return <div><div className="section-heading"><div><p className="eyebrow">DETALHES DA EQUIPE</p><h2>{team.identification}</h2></div><button className="secondary-button" type="button" onClick={onEdit}>Editar</button></div><div className="facts"><div><span>Total de integrantes</span><strong>{team.totalMembers}</strong></div><div><span>Situação</span><strong>{team.status || 'Não informada'}</strong></div></div><DetailList title="Funções" values={(team.composition || []).map((item) => `${item.role} · ${item.quantity}`)} empty="Composição não detalhada." /><DetailList title="Competências" values={team.competencies} empty="Nenhuma competência informada." /><DetailList title="Qualificações" values={team.qualifications} empty="Nenhuma qualificação informada." /><div className="detail-block"><h3>Disponibilidade</h3><pre>{team.availability ? JSON.stringify(team.availability, null, 2) : 'Não informada.'}</pre></div></div> }
function DetailList({ title, values = [], empty }) { return <div className="detail-block"><h3>{title}</h3>{values.length ? <div className="chips">{values.map((value) => <span className="chip" key={value}>{value}</span>)}</div> : <p className="muted">{empty}</p>}</div> }

export default App
