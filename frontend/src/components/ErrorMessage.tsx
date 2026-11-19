export default function ErrorMessage({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700 whitespace-pre-line">
      {message}
      {onRetry && (
        <button onClick={onRetry} className="ml-3 underline">
          다시 시도
        </button>
      )}
    </div>
  )
}
