package net.biodiscus.androidcontroltv.core.debug

/** A named chain of steps that runs on the TV in the given order and stops at the first step that fails. */
data class DebugAction(val name: String, val description: String, val steps: List<DebugStep>)
