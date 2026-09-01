package com.spacca.app.data

import io.ktor.client.engine.HttpClientEngine

expect fun createPlatformEngine(): HttpClientEngine
