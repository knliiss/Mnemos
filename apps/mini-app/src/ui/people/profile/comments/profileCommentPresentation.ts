export type ProfileCommentTone = "lime" | "plum" | "amber"

export function profileCommentTone(author: string): ProfileCommentTone {
  const tones: ProfileCommentTone[] = ["lime", "plum", "amber"]
  const score = Array.from(author).reduce((sum, character) => sum + character.charCodeAt(0), 0)

  return tones[score % tones.length]
}
