export default function ComingSoon({ title }: { title: string }) {
  return (
    <div className="py-16 text-center">
      <h1 className="text-xl font-bold">{title}</h1>
      <p className="mt-2 text-sm text-gray-500">다음 단계에서 만들 화면이에요.</p>
    </div>
  )
}
