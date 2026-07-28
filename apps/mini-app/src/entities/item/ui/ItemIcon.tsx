import {CircleDot,Feather,Gem,Shield,Slash} from "lucide-react"
import type {ItemKind} from "@/shared/types/domain"
export function ItemIcon({kind}:{kind:ItemKind}){const P=kind==="weapon"?Slash:kind==="wing"?Feather:kind==="orb"?CircleDot:kind==="totem"?Shield:Gem;return <div className="item-icon"><P size={18}/></div>}