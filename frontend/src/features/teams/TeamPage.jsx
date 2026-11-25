import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/PageHeader'
import { EmptyState, ErrorMessage, LoadingState } from '../../components/Feedback'
import { FormActions, FormHeading } from '../../components/FormActions'
import { TEAM_API_URL } from '../../app/config'
import { request } from '../../services/httpClient'

const EMPTY_TEAM = {
  identification: '', totalMembers: '', status: '', competencies: '', qualifications: '', availability: '',
  composition: [{ role: '', quantity: '' }],
}

export function TeamPage() {
  const [teams, setTeams] = useState([])
  const [selectedTeam, setSelectedTeam] = useState(null)
  const [form, setForm] = useState(EMPTY_TEAM)
  const [editingId, setEditingId] = useState(null)
  const [view, setView] = useState('list')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')

  const loadTeams = async () => {
    setLoading(true)
    setError('')
    try {
      const result = await request(TEAM_API_URL, '/teams')
      setTeams(result.content || [])
    } catch (loadError) {
      setError(loadError.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadTeams() }, [])

  const startCreate = () => {
    setSelectedTeam(null)
    setEditingId('new')
    setView('new')
    setForm({ ...EMPTY_TEAM, composition: [{ role: '', quantity: '' }] })
    setFormError('')
  }

  const startEdit = (team) => {
    setEditingId(team.id)
    setSelectedTeam(team)
    setView('edit')
    setForm(teamToForm(team))
    setFormError('')
  }

  const closeEditor = () => {
    const returningToDetail = editingId !== 'new' && selectedTeam
    setEditingId(null)
    setFormError('')
    setView(returningToDetail ? 'detail' : 'list')
  }

  const saveTeam = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      const team = await request(TEAM_API_URL, editingId === 'new' ? '/teams' : `/teams/${editingId}`, {
        method: editingId === 'new' ? 'POST' : 'PUT',
        body: JSON.stringify(teamPayload(form)),
      })
      setSelectedTeam(team)
      setEditingId(null)
      setView('detail')
      await loadTeams()
    } catch (saveError) {
      setFormError(saveError.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section>
      <PageHeader title="Equipes" description="Organize equipes, capacidades e disponibilidade cadastrada." actionLabel="Nova equipe" onAction={startCreate} />
      {error && <ErrorMessage>{error}</ErrorMessage>}
      {view === 'list' && <div className="content-section">
          <div className="section-meta"><span>{teams.length} {teams.length === 1 ? 'equipe' : 'equipes'}</span></div>
          {loading ? <LoadingState label="Carregando equipes…" /> : teams.length === 0 ? (
            <EmptyState title="Nenhuma equipe cadastrada" description="Cadastre a primeira equipe para começar." actionLabel="Nova equipe" onAction={startCreate} />
          ) : <TeamTable teams={teams} selectedId={selectedTeam?.id} onSelect={(team) => { setSelectedTeam(team); setEditingId(null); setView('detail') }} />}
        </div>}
      {view === 'detail' && selectedTeam && <div className="content-section detail-page"><BackButton onClick={() => setView('list')} /><TeamDetails team={selectedTeam} onEdit={() => startEdit(selectedTeam)} /></div>}
      {(view === 'new' || view === 'edit') && <div className="content-section form-page"><BackButton onClick={closeEditor} /><TeamForm form={form} formError={formError} editingId={editingId} saving={saving} onChange={setForm} onSubmit={saveTeam} onCancel={closeEditor} /></div>}
    </section>
  )
}

function TeamTable({ teams, selectedId, onSelect }) {
  return <div className="table-wrap"><table><caption className="sr-only">Equipes cadastradas</caption><thead><tr><th scope="col">Equipe</th><th scope="col">Integrantes</th><th scope="col">Situação</th><th scope="col">Capacidades</th></tr></thead><tbody>{teams.map((team) => <tr key={team.id} className={selectedId === team.id ? 'is-selected' : ''} tabIndex="0" onClick={() => onSelect(team)} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') onSelect(team) }}><th scope="row">{team.identification}</th><td>{team.totalMembers}</td><td><StatusLabel value={team.status || 'Não informada'} /></td><td>{team.competencies?.length || team.qualifications?.length ? `${(team.competencies?.length || 0) + (team.qualifications?.length || 0)} itens` : '—'}</td></tr>)}</tbody></table></div>
}

function TeamForm({ form, formError, editingId, saving, onChange, onSubmit, onCancel }) {
  const update = (field, value) => onChange((current) => ({ ...current, [field]: value }))
  const updateComposition = (index, field, value) => onChange((current) => ({ ...current, composition: current.composition.map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item) }))
  const addComposition = () => onChange((current) => ({ ...current, composition: [...current.composition, { role: '', quantity: '' }] }))

  return <form className="form-layout" onSubmit={onSubmit}><FormHeading eyebrow={editingId === 'new' ? 'NOVA EQUIPE' : 'EDIÇÃO'} title={editingId === 'new' ? 'Cadastrar equipe' : 'Editar equipe'} description="Informe somente os dados disponíveis para esta equipe." />{formError && <ErrorMessage>{formError}</ErrorMessage>}<div className="form-group"><h3>Identificação</h3><label>Nome ou identificação<input required maxLength="150" value={form.identification} onChange={(event) => update('identification', event.target.value)} /></label><label>Total de integrantes<input required min="0" type="number" value={form.totalMembers} onChange={(event) => update('totalMembers', event.target.value)} /></label><label>Situação <span className="field-note">opcional</span><input maxLength="50" value={form.status} onChange={(event) => update('status', event.target.value)} placeholder="Ex.: ATIVA" /></label></div><div className="form-group"><h3>Capacidades</h3><fieldset><legend>Composição por função <span className="field-note">opcional</span></legend>{form.composition.map((item, index) => <div className="composition-row" key={index}><input aria-label="Função" placeholder="Função" value={item.role} onChange={(event) => updateComposition(index, 'role', event.target.value)} /><input aria-label="Quantidade" min="0" type="number" placeholder="Qtd." value={item.quantity} onChange={(event) => updateComposition(index, 'quantity', event.target.value)} /></div>)}<button className="text-button" type="button" onClick={addComposition}>+ Adicionar função</button></fieldset><label>Competências <span className="field-note">opcional · separadas por vírgula</span><input value={form.competencies} onChange={(event) => update('competencies', event.target.value)} /></label><label>Qualificações <span className="field-note">opcional · separadas por vírgula</span><input value={form.qualifications} onChange={(event) => update('qualifications', event.target.value)} /></label></div><div className="form-group"><h3>Disponibilidade</h3><label><span className="field-note">JSON preservado sem interpretação pelo frontend</span><textarea rows="4" value={form.availability} onChange={(event) => update('availability', event.target.value)} placeholder={'{\n  "observacao": "..."\n}'} /></label></div><FormActions saving={saving} onCancel={onCancel} /></form>
}

function TeamDetails({ team, onEdit }) {
  return <div className="detail-content"><div className="detail-header"><FormHeading eyebrow="DETALHES DA EQUIPE" title={team.identification} /><button className="secondary-button" type="button" onClick={onEdit}>Editar equipe</button></div><dl className="facts-list"><div><dt>Integrantes</dt><dd>{team.totalMembers}</dd></div><div><dt>Situação</dt><dd><StatusLabel value={team.status || 'Não informada'} /></dd></div></dl><DetailList title="Composição" values={(team.composition || []).map((item) => `${item.role} · ${item.quantity}`)} empty="Não informada." /><DetailList title="Competências" values={team.competencies} empty="Nenhuma competência informada." /><DetailList title="Qualificações" values={team.qualifications} empty="Nenhuma qualificação informada." /></div>
}

function DetailList({ title, values = [], empty }) { return <div className="detail-block"><h3>{title}</h3>{values.length ? <ul className="value-list">{values.map((value) => <li key={value}>{value}</li>)}</ul> : <p className="muted">{empty}</p>}</div> }
function StatusLabel({ value }) { return <span className="status-label">{value}</span> }
function BackButton({ onClick }) { return <button className="back-button" type="button" onClick={onClick}>← Voltar para equipes</button> }

function teamToForm(team) { return { identification: team.identification || '', totalMembers: team.totalMembers ?? '', status: team.status || '', competencies: (team.competencies || []).join(', '), qualifications: (team.qualifications || []).join(', '), availability: team.availability ? JSON.stringify(team.availability, null, 2) : '', composition: team.composition?.length ? team.composition.map((item) => ({ role: item.role, quantity: item.quantity })) : [{ role: '', quantity: '' }] } }
function listValues(value) { return value.split(',').map((item) => item.trim()).filter(Boolean) }
function teamPayload(form) { let availability; if (form.availability.trim()) availability = JSON.parse(form.availability); return { identification: form.identification, totalMembers: Number(form.totalMembers), status: form.status || null, competencies: listValues(form.competencies), qualifications: listValues(form.qualifications), availability: availability || null, composition: form.composition.filter((item) => item.role.trim()).map((item) => ({ role: item.role.trim(), quantity: Number(item.quantity) })) } }
