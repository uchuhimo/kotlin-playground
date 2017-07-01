package com.uchuhimo.konf

sealed class ConfigTree {
    abstract val path: List<String>
    fun <T> visit(
            context: T,
            onEnterNode: (T, ConfigNode) -> Unit = { _, _ -> },
            onLeaveNode: (T, ConfigNode) -> Unit = { _, _ -> },
            onEnterLeaf: (T, ConfigLeaf<*>) -> Unit = { _, _ -> }) {
        when (this) {
            is ConfigNode -> {
                onEnterNode(context, this)
                for (child in children) {
                    child.visit(context, onEnterNode, onLeaveNode, onEnterLeaf)
                }
                onLeaveNode(context, this)
            }
            is ConfigLeaf<*> -> {
                onEnterLeaf(context, this)
            }
        }
    }

    val items: Iterable<Item<*>> get() {
        val items = mutableListOf<Item<*>>()
        visit(
                items,
                onEnterLeaf = { items, leaf ->
                    items += leaf.item
                })
        return items
    }
}

class ConfigLeaf<T : Any>(
        override val path: List<String>,
        val item: Item<T>
) : ConfigTree()

class ConfigNode(
        override val path: List<String>,
        val children: MutableList<ConfigTree>
) : ConfigTree()
