export default function Loading() {
  return (
    <div className="flex justify-center py-16" role="status" aria-label="불러오는 중">
      <span className="h-7 w-7 animate-spin rounded-full border-[3px] border-ink-100 border-t-brand-500" />
    </div>
  )
}
