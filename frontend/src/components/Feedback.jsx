export function ErrorMessage({ children }) {
  return <div className="feedback error-feedback" role="alert">{children}</div>
}

export function LoadingState({ label = 'Carregando registros…' }) {
  return <p className="loading-state" role="status">{label}</p>
}

export function EmptyState({ title, description, actionLabel, onAction }) {
  return (
    <div className="empty-state">
      <p className="empty-kicker">SEM REGISTROS</p>
      <h2>{title}</h2>
      <p>{description}</p>
      <button className="secondary-button" type="button" onClick={onAction}>{actionLabel}</button>
    </div>
  )
}
