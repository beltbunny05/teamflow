export function PageHeader({ eyebrow = 'CADASTRO', title, description, actionLabel, onAction }) {
  return (
    <div className="page-header">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p className="page-description">{description}</p>
      </div>
      <button className="primary-button" type="button" onClick={onAction}>{actionLabel}</button>
    </div>
  )
}
