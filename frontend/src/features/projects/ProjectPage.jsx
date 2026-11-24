import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/PageHeader'
import { EmptyState, ErrorMessage, LoadingState } from '../../components/Feedback'
import { FormActions, FormHeading } from '../../components/FormActions'
import { PROJECT_API_URL } from '../../app/config'
import { request } from '../../services/httpClient'

const EMPTY_SERVICE = { identification: '', description: '', serviceDays: '', estimatedDurationDays: '', competencies: '', qualifications: '' }
const EMPTY_PROJECT = { identification: '', client: '', description: '', plannedStartDate: '', plannedEndDate: '', estimatedDurationDays: '', competencies: '', qualifications: '', localities: [{ identification: '' }], services: [{ ...EMPTY_SERVICE }] }

export function ProjectPage() {
  const [projects, setProjects] = useState([])
  const [selectedProject, setSelectedProject] = useState(null)
  const [form, setForm] = useState(EMPTY_PROJECT)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [formError, setFormError] = useState('')

  const loadProjects = async () => {
    setLoading(true)
    setError('')
    try {
      const result = await request(PROJECT_API_URL, '/projects')
      setProjects(result.content || [])
    } catch (loadError) {
      setError(loadError.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadProjects() }, [])

  const startCreate = () => {
    setSelectedProject(null)
    setEditingId('new')
    setForm({ ...EMPTY_PROJECT, localities: [{ identification: '' }], services: [{ ...EMPTY_SERVICE }] })
    setFormError('')
  }

  const startEdit = (project) => {
    setEditingId(project.id)
    setSelectedProject(project)
    setForm(projectToForm(project))
    setFormError('')
  }

  const closeEditor = () => {
    setEditingId(null)
    setFormError('')
  }

  const saveProject = async (event) => {
    event.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      const project = await request(PROJECT_API_URL, editingId === 'new' ? '/projects' : `/projects/${editingId}`, {
        method: editingId === 'new' ? 'POST' : 'PUT',
        body: JSON.stringify(projectPayload(form)),
      })
      setSelectedProject(project)
      setEditingId(null)
      await loadProjects()
    } catch (saveError) {
      setFormError(saveError.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section>
      <PageHeader title="Projetos" description="Organize projetos simultâneos, períodos e localidades." actionLabel="Novo projeto" onAction={startCreate} />
      {error && <ErrorMessage>{error}</ErrorMessage>}
      <div className="workspace-layout">
        <div className="table-section">
          <div className="section-meta"><span>{projects.length} {projects.length === 1 ? 'projeto' : 'projetos'}</span></div>
          {loading ? <LoadingState label="Carregando projetos…" /> : projects.length === 0 ? (
            <EmptyState title="Nenhum projeto cadastrado" description="Cadastre o primeiro projeto para começar." actionLabel="Novo projeto" onAction={startCreate} />
          ) : <ProjectTable projects={projects} selectedId={selectedProject?.id} onSelect={(project) => { setSelectedProject(project); setEditingId(null) }} />}
        </div>
        <aside className="detail-section">
          {editingId ? <ProjectForm form={form} formError={formError} editingId={editingId} saving={saving} onChange={setForm} onSubmit={saveProject} onCancel={closeEditor} /> : selectedProject ? <ProjectDetails project={selectedProject} onEdit={() => startEdit(selectedProject)} /> : <SelectionPrompt title="Selecione um projeto" description="Escolha uma linha para consultar os detalhes ou crie um novo projeto." actionLabel="Novo projeto" onAction={startCreate} />}
        </aside>
      </div>
    </section>
  )
}

function ProjectTable({ projects, selectedId, onSelect }) {
  return <div className="table-wrap"><table><caption className="sr-only">Projetos cadastrados</caption><thead><tr><th scope="col">Projeto</th><th scope="col">Cliente</th><th scope="col">Período previsto</th><th scope="col">Localidades</th></tr></thead><tbody>{projects.map((project) => <tr key={project.id} className={selectedId === project.id ? 'is-selected' : ''} tabIndex="0" onClick={() => onSelect(project)} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') onSelect(project) }}><th scope="row">{project.identification}</th><td>{project.client || '—'}</td><td>{formatPeriod(project)}</td><td>{project.localities?.length || 0}</td></tr>)}</tbody></table></div>
}

function ProjectForm({ form, formError, editingId, saving, onChange, onSubmit, onCancel }) {
  const update = (field, value) => onChange((current) => ({ ...current, [field]: value }))
  const updateNested = (collection, index, field, value) => onChange((current) => ({ ...current, [collection]: current[collection].map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item) }))
  const addNested = (collection, emptyItem) => onChange((current) => ({ ...current, [collection]: [...current[collection], { ...emptyItem }] }))

  return <form className="form-layout" onSubmit={onSubmit}><FormHeading eyebrow={editingId === 'new' ? 'NOVO PROJETO' : 'EDIÇÃO'} title={editingId === 'new' ? 'Cadastrar projeto' : 'Editar projeto'} description="Registre as informações disponíveis para este projeto." />{formError && <ErrorMessage>{formError}</ErrorMessage>}<div className="form-group"><h3>Identificação</h3><label>Nome ou identificação<input required maxLength="150" value={form.identification} onChange={(event) => update('identification', event.target.value)} /></label><label>Cliente <span className="field-note">opcional</span><input maxLength="150" value={form.client} onChange={(event) => update('client', event.target.value)} /></label><label>Descrição <span className="field-note">opcional</span><textarea rows="3" maxLength="2000" value={form.description} onChange={(event) => update('description', event.target.value)} /></label></div><div className="form-group"><h3>Período previsto</h3><div className="two-columns"><label>Data prevista de início<input type="date" value={form.plannedStartDate} onChange={(event) => update('plannedStartDate', event.target.value)} /></label><label>Data prevista de término<input type="date" value={form.plannedEndDate} onChange={(event) => update('plannedEndDate', event.target.value)} /></label></div><label>Duração estimada em dias <span className="field-note">manual</span><input min="0" type="number" value={form.estimatedDurationDays} onChange={(event) => update('estimatedDurationDays', event.target.value)} /></label></div><div className="form-group"><h3>Competências</h3><label>Competências necessárias <span className="field-note">opcional · separadas por vírgula</span><input value={form.competencies} onChange={(event) => update('competencies', event.target.value)} /></label><label>Qualificações necessárias <span className="field-note">opcional · separadas por vírgula</span><input value={form.qualifications} onChange={(event) => update('qualifications', event.target.value)} /></label></div><div className="form-group"><h3>Localidades</h3>{form.localities.map((item, index) => <input key={index} placeholder="Identificação da localidade" value={item.identification} onChange={(event) => updateNested('localities', index, 'identification', event.target.value)} />)}<button className="text-button" type="button" onClick={() => addNested('localities', { identification: '' })}>+ Adicionar localidade</button></div><div className="form-group supporting-data"><h3>Serviços de apoio</h3><p className="form-description">Informações auxiliares atualmente mantidas no cadastro do projeto.</p>{form.services.map((item, index) => <div className="nested-group" key={index}><label>Identificação<input maxLength="150" value={item.identification} onChange={(event) => updateNested('services', index, 'identification', event.target.value)} /></label><label>Descrição<textarea rows="2" maxLength="2000" value={item.description} onChange={(event) => updateNested('services', index, 'description', event.target.value)} /></label><div className="two-columns"><label>Dias de serviço<input min="0" type="number" value={item.serviceDays} onChange={(event) => updateNested('services', index, 'serviceDays', event.target.value)} /></label><label>Duração estimada<input min="0" type="number" value={item.estimatedDurationDays} onChange={(event) => updateNested('services', index, 'estimatedDurationDays', event.target.value)} /></label></div><label>Competências necessárias<input value={item.competencies} onChange={(event) => updateNested('services', index, 'competencies', event.target.value)} /></label><label>Qualificações necessárias<input value={item.qualifications} onChange={(event) => updateNested('services', index, 'qualifications', event.target.value)} /></label></div>)}<button className="text-button" type="button" onClick={() => addNested('services', EMPTY_SERVICE)}>+ Adicionar serviço</button></div><FormActions saving={saving} onCancel={onCancel} /></form>
}

function ProjectDetails({ project, onEdit }) {
  return <div className="detail-content"><div className="detail-header"><FormHeading eyebrow="DETALHES DO PROJETO" title={project.identification} /><button className="secondary-button" type="button" onClick={onEdit}>Editar</button></div><dl className="facts-list"><div><dt>Início previsto</dt><dd>{project.plannedStartDate || 'Não informado'}</dd></div><div><dt>Término previsto</dt><dd>{project.plannedEndDate || 'Não informado'}</dd></div></dl>{project.client && <div className="detail-block"><h3>Cliente</h3><p className="muted">{project.client}</p></div>}<div className="detail-block"><h3>Descrição</h3><p className="muted">{project.description || 'Não informada.'}</p></div><DetailList title="Localidades" values={(project.localities || []).map((item) => item.identification)} empty="Nenhuma localidade informada." /><div className="detail-block"><h3>Serviços de apoio</h3>{project.services?.length ? <div className="supporting-list">{project.services.map((item) => <div className="supporting-item" key={item.identification}><strong>{item.identification}</strong><p>{item.description || 'Sem descrição.'}</p><small>{item.serviceDays ?? '—'} dias de serviço · duração estimada: {item.estimatedDurationDays ?? '—'} dias</small></div>)}</div> : <p className="muted">Nenhum serviço informado.</p>}</div></div>
}

function DetailList({ title, values = [], empty }) { return <div className="detail-block"><h3>{title}</h3>{values.length ? <ul className="value-list">{values.map((value) => <li key={value}>{value}</li>)}</ul> : <p className="muted">{empty}</p>}</div> }
function SelectionPrompt({ title, description, actionLabel, onAction }) { return <div className="selection-prompt"><p className="eyebrow">DETALHES</p><h2>{title}</h2><p>{description}</p><button className="secondary-button" type="button" onClick={onAction}>{actionLabel}</button></div> }
function formatPeriod(project) { if (project.plannedStartDate && project.plannedEndDate) return `${project.plannedStartDate} — ${project.plannedEndDate}`; return project.plannedStartDate || project.plannedEndDate || 'Não informado' }
function listValues(value) { return value.split(',').map((item) => item.trim()).filter(Boolean) }
function optionalNumber(value) { return value === '' ? null : Number(value) }
function projectToForm(project) { return { identification: project.identification || '', client: project.client || '', description: project.description || '', plannedStartDate: project.plannedStartDate || '', plannedEndDate: project.plannedEndDate || '', estimatedDurationDays: project.estimatedDurationDays ?? '', competencies: (project.competencies || []).join(', '), qualifications: (project.qualifications || []).join(', '), localities: project.localities?.length ? project.localities.map((item) => ({ identification: item.identification })) : [{ identification: '' }], services: project.services?.length ? project.services.map((item) => ({ identification: item.identification || '', description: item.description || '', serviceDays: item.serviceDays ?? '', estimatedDurationDays: item.estimatedDurationDays ?? '', competencies: (item.competencies || []).join(', '), qualifications: (item.qualifications || []).join(', ') })) : [{ ...EMPTY_SERVICE }] } }
function projectPayload(form) { return { identification: form.identification, client: form.client || null, description: form.description || null, plannedStartDate: form.plannedStartDate || null, plannedEndDate: form.plannedEndDate || null, estimatedDurationDays: optionalNumber(form.estimatedDurationDays), competencies: listValues(form.competencies), qualifications: listValues(form.qualifications), localities: form.localities.filter((item) => item.identification.trim()).map((item) => ({ identification: item.identification.trim() })), services: form.services.filter((item) => item.identification.trim()).map((item) => ({ identification: item.identification.trim(), description: item.description || null, serviceDays: optionalNumber(item.serviceDays), estimatedDurationDays: optionalNumber(item.estimatedDurationDays), competencies: listValues(item.competencies), qualifications: listValues(item.qualifications) })) } }
