import { request, type CategoryTotal } from './activities'

export type Mentor = { id: string; email: string }
export type SummaryContent = { date: string; totals: CategoryTotal[]; comment: string | null }
export type SummaryPreview = { previewId: string; mentor: Mentor; content: SummaryContent }
export type SummaryShare = { id: string; summaryId: string; date: string; mentor: Mentor; createdAt: string; revokedAt: string | null }
export const getMentors = () => request<Mentor[]>('summaries/mentors')
export const getShares = () => request<SummaryShare[]>('summaries/shares')
export const previewSummary = (date: string, mentorId: string, comment: string) => request<SummaryPreview>('summaries/preview', {
  date, mentorId, comment, zone: Intl.DateTimeFormat().resolvedOptions().timeZone,
})
export const confirmSummary = (previewId: string) => request<SummaryShare>('summaries/shares', { previewId, confirmed: true })
export const revokeSummary = (id: string) => request(`summaries/shares/${id}`, undefined, 'DELETE')
