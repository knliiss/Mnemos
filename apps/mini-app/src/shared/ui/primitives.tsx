import type {ButtonHTMLAttributes,HTMLAttributes,ReactNode} from "react"
export function Card({className="",...p}:HTMLAttributes<HTMLDivElement>){return <div className={`card ${className}`} {...p}/>}
export function IconButton({children,className="",...p}:ButtonHTMLAttributes<HTMLButtonElement>&{children:ReactNode}){return <button className={`icon-button ${className}`} {...p}>{children}</button>}
export function Segmented<T extends string>({value,options,onChange}:{value:T;options:{value:T;label:string}[];onChange(v:T):void}){return <div className="segmented">{options.map(o=><button key={o.value} className={value===o.value?"active":""} onClick={()=>onChange(o.value)}>{o.label}</button>)}</div>}
export function Empty({children}:{children:ReactNode}){return <div className="empty">{children}</div>}
