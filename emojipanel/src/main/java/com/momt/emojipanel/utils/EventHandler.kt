package com.momt.emojipanel.utils

class EventHandler<TSender, TEventArgs> {
    private val handlers: ArrayList<(TSender, TEventArgs) -> Unit> = ArrayList()

    operator fun plusAssign(handler: EventHandler<TSender, TEventArgs>) {
        handlers.add { sender, e -> handler(sender, e) }
    }

    operator fun plusAssign(handler: (TSender, TEventArgs) -> Unit) {
        handlers.add(handler)
    }

    operator fun invoke(sender: TSender, e: TEventArgs) {
        handlers.forEach { it.invoke(sender, e) }
    }
}