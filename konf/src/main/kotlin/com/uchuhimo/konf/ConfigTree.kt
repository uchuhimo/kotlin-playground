package com.uchuhimo.konf

sealed class ConfigTree {
    abstract val path: List<String>

    abstract fun deepCopy(): ConfigTree

    fun visit(
            onEnterNode: (ConfigNode) -> Unit = { _ -> },
            onLeaveNode: (ConfigNode) -> Unit = { _ -> },
            onEnterLeaf: (ConfigLeaf<*>) -> Unit = { _ -> }) {
        when (this) {
            is ConfigNode -> {
                onEnterNode(this)
                for (child in children) {
                    child.visit(onEnterNode, onLeaveNode, onEnterLeaf)
                }
                onLeaveNode(this)
            }
            is ConfigLeaf<*> -> {
                onEnterLeaf(this)
            }
        }
    }

    val items: Iterable<Item<*>> get() {
        val items = mutableListOf<Item<*>>()
        visit(
                onEnterLeaf = { leaf ->
                    items += leaf.item
                })
        return items
    }
}

class ConfigLeaf<T : Any>(
        override val path: List<String>,
        val item: Item<T>
) : ConfigTree() {
    override fun deepCopy(): ConfigLeaf<T> = ConfigLeaf(path, item)
}

class ConfigNode(
        override val path: List<String>,
        val children: MutableList<ConfigTree>
) : ConfigTree() {
    override fun deepCopy(): ConfigNode =
            ConfigNode(path, children.mapTo(mutableListOf(), ConfigTree::deepCopy))
}
