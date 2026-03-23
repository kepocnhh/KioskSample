package test.android.kiosk.provider

import kotlinx.coroutines.CoroutineDispatcher

internal class Contexts(
    val main: CoroutineDispatcher,
    val default: CoroutineDispatcher,
)
