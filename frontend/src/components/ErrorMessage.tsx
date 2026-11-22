export default function ErrorMessage({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="flex items-start gap-3 rounded-2xl bg-danger-50 px-4 py-3.5 text-sm text-danger-600">
      <span className="whitespace-pre-line">{message}</span>
      {onRetry && (
        <button type="button" onClick={onRetry} className="ml-auto shrink-0 font-semibold underline-offset-2 hover:underline">
          다시 시도
        </button>
      )}
    </div>
  )
}
