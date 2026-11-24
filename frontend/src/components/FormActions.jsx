export function FormActions({ saving, onCancel }) {
  return (
    <div className="form-actions">
      <button className="secondary-button" type="button" onClick={onCancel}>Cancelar</button>
      <button className="primary-button" disabled={saving} type="submit">
        {saving ? 'Salvando…' : 'Salvar'}
      </button>
    </div>
  )
}

export function FormHeading({ eyebrow, title, description }) {
  return (
    <div className="form-heading">
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
      {description && <p className="form-description">{description}</p>}
    </div>
  )
}
