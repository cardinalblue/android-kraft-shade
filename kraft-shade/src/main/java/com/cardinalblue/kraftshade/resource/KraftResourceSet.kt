package com.cardinalblue.kraftshade.resource

class KraftResourceSet {
    private val resources: MutableSet<KraftResource> = mutableSetOf()

    fun add(resource: KraftResource) {
        resources.add(resource)
    }

    suspend fun clear() {
        for (resource in resources) {
            resource.delete()
        }
        resources.clear()
    }
}