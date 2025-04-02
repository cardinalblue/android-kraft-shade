package com.cardinalblue.kraftshade.resource

interface KraftLifecycleOwner : KraftResource {
    fun bindResource(resource: KraftResource)
}
