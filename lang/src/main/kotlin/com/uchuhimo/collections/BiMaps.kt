package com.uchuhimo.collections

import com.google.common.collect.HashBiMap

interface BiMap<K, V> : Map<K, V> {
    val inverse: BiMap<V, K>

    override val values: Set<V>
}

interface MutableBiMap<K, V> : MutableMap<K, V>, BiMap<K, V> {
    override val inverse: MutableBiMap<V, K>

    override val values: MutableSet<V>
}

private class BiMapImpl<K, V> private constructor(delegate: Map<K, V>) :
        BiMap<K, V>, Map<K, V> by delegate {
    constructor(forward: Map<K, V>, backward: Map<V, K>) : this(forward) {
        _inverse = BiMapImpl(backward, this)
    }

    private constructor(backward: Map<K, V>, forward: BiMap<V, K>) : this(backward) {
        _inverse = forward
    }

    private lateinit var _inverse: BiMap<V, K>

    override val inverse: BiMap<V, K> get() = _inverse

    override val values: Set<V> = inverse.keys
}

private class MutableBiMapImpl<K, V> private constructor(private val delegate: MutableMap<K, V>) :
        MutableBiMap<K, V>, Map<K, V> by delegate {
    constructor(forward: MutableMap<K, V>, backward: MutableMap<V, K>) : this(forward) {
        _inverse = MutableBiMapImpl(backward, this)
    }

    private constructor(backward: MutableMap<K, V>, forward: MutableBiMapImpl<V, K>) : this(backward) {
        _inverse = forward
    }

    private lateinit var _inverse: MutableBiMapImpl<V, K>

    private val inverseDelegate = inverse.delegate

    override val inverse: MutableBiMapImpl<V, K> get() = _inverse

    override fun containsValue(value: V): Boolean = inverseDelegate.containsKey(value)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
            object : MutableSet<MutableMap.MutableEntry<K, V>> by delegate.entries {
                override fun clear() {
                    this@MutableBiMapImpl.clear()
                }

                override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> =
                        object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                            override fun remove() {
                                TODO("not implemented")
                            }

                            override fun hasNext(): Boolean {
                                TODO("not implemented")
                            }

                            override fun next(): MutableMap.MutableEntry<K, V> {
                                TODO("not implemented")
                            }
                        }

                override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
                    inverseDelegate.remove(element.value, element.key)
                    return delegate.remove(element.key, element.value)
                }
            }

    override val keys: MutableSet<K>
        get() = TODO("not implemented")
    override val values: MutableSet<V>
        get() = TODO("not implemented")

    override fun clear() {
        delegate.clear()
        inverseDelegate.clear()
    }

    override fun put(key: K, value: V): V? {
        inverseDelegate.put(value, key)?.also { oldKey -> delegate.remove(oldKey) }
        return delegate.put(key, value)?.also { oldValue -> inverseDelegate.remove(oldValue) }
    }

    override fun putAll(from: Map<out K, V>) {
        from.forEach { key, value -> this[key] = value }
    }

    override fun remove(key: K): V? {
        return delegate.remove(key).also { value ->
            if (value != null) {
                inverseDelegate.remove(value)
            }
        }
    }
}

typealias GuavaBiMap<K, V> = com.google.common.collect.BiMap<K, V>

class GuavaBiMapWrapper<K, V>(private val delegate: GuavaBiMap<K, V>) :
        MutableBiMap<K, V>, MutableMap<K, V> by delegate {
    override val inverse: MutableBiMap<V, K> = InverseWrapper(this)

    override val values: MutableSet<V> = delegate.values

    companion object {
        private class InverseWrapper<K, V>(wrapper: GuavaBiMapWrapper<V, K>) :
                MutableBiMap<K, V>, MutableMap<K, V> by wrapper.delegate.inverse() {
            override val inverse: MutableBiMap<V, K> = wrapper

            override val values: MutableSet<V> = wrapper.delegate.inverse().values
        }
    }
}

private val emptyBiMap = BiMapImpl<Any?, Any?>(emptyMap(), emptyMap())

fun <K, V> emptyBiMap(): BiMap<K, V> = @Suppress("UNCHECKED_CAST") (emptyBiMap as BiMap<K, V>)

fun <K, V> biMapOf(vararg pairs: Pair<K, V>): BiMap<K, V> =
        if (pairs.isNotEmpty()) {
            val inversePairs = Array(pairs.size, { i -> pairs[i].second to pairs[i].first })
            BiMapImpl(mapOf(*pairs), mapOf(*inversePairs))
        } else {
            emptyBiMap()
        }

@Suppress("NOTHING_TO_INLINE")
inline fun <K, V> biMapOf(): Map<K, V> = emptyBiMap()

fun <K, V> biMapOf(pair: Pair<K, V>): Map<K, V> =
        BiMapImpl(mapOf(pair), mapOf(pair.second to pair.first))

@Suppress("NOTHING_TO_INLINE")
inline fun <K, V> mutableBiMapOf(): MutableBiMap<K, V> = GuavaBiMapWrapper(HashBiMap.create())

fun <K, V> mutableBiMapOf(vararg pairs: Pair<K, V>): MutableBiMap<K, V>
        = GuavaBiMapWrapper<K, V>(HashBiMap.create(pairs.size)).apply { putAll(pairs) }
