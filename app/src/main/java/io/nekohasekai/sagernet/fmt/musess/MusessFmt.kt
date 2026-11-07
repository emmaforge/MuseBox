package io.nekohasekai.sagernet.fmt.musess

import io.nekohasekai.sagernet.ktx.blankAsNull
import io.nekohasekai.sagernet.ktx.linkBuilder
import io.nekohasekai.sagernet.ktx.toLink
import io.nekohasekai.sagernet.ktx.urlSafe
import moe.matsuri.nb4a.SingBoxOptions
import moe.matsuri.nb4a.utils.listByLineOrComma
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

// musess://<uuid>@<host>:<port>?secret=<url-encoded>&public_key=<url-encoded>
//   &server_name=<dest>&warm_pool=2#<name>
// Optional parameters (only written when set): mux=1, mux_bypass=<comma list>,
// mux_adaptive_bypass=1, deshell=1, fec=auto|on. warm_pool is omitted when
// unset (the core defaults it to 2); warm_pool=0 disables the pool.
fun parseMusess(url: String): MusessBean {
    val link = url.replace("musess://", "https://").toHttpUrlOrNull()
        ?: error("invalid musess link $url")
    return MusessBean().apply {
        serverAddress = link.host
        serverPort = link.port
        name = link.fragment ?: ""
        uuid = link.username
        secret = link.queryParameter("secret") ?: ""
        publicKey = link.queryParameter("public_key") ?: ""
        serverName = link.queryParameter("server_name") ?: ""
        link.queryParameter("warm_pool")?.toIntOrNull()?.let { warmPool = it }
        link.queryParameter("mux")?.let { mux = it == "1" || it == "true" }
        link.queryParameter("mux_bypass")?.let { muxBypass = it.listByLineOrComma() }
        link.queryParameter("mux_adaptive_bypass")?.let {
            muxAdaptiveBypass = it == "1" || it == "true"
        }
        link.queryParameter("deshell")?.let { deshell = it == "1" || it == "true" }
        link.queryParameter("fec")?.let {
            if (it == "off" || it == "auto" || it == "on") fec = it
        }
    }
}

fun MusessBean.toUri(): String {
    val builder = linkBuilder()
        .username(uuid ?: "")
        .host(serverAddress)
        .port(serverPort)
    if (secret.isNotBlank()) {
        builder.addQueryParameter("secret", secret)
    }
    if (publicKey.isNotBlank()) {
        builder.addQueryParameter("public_key", publicKey)
    }
    if (serverName.isNotBlank()) {
        builder.addQueryParameter("server_name", serverName)
    }
    warmPool?.let {
        builder.addQueryParameter("warm_pool", it.toString())
    }
    if (mux == true) {
        builder.addQueryParameter("mux", "1")
    }
    if (muxBypass.orEmpty().isNotEmpty()) {
        builder.addQueryParameter("mux_bypass", muxBypass.joinToString(","))
    }
    if (muxAdaptiveBypass == true) {
        builder.addQueryParameter("mux_adaptive_bypass", "1")
    }
    if (deshell == true) {
        builder.addQueryParameter("deshell", "1")
    }
    if (fec != null && fec != "off") {
        builder.addQueryParameter("fec", fec)
    }
    if (name.isNotBlank()) {
        builder.encodedFragment(name.urlSafe())
    }
    return builder.toLink("musess")
}

// Every JSON key emitted here exists in the core option struct
// MusessOutboundOptions (impl/sing-box/option/musess.go). Gson skips null
// fields, so optional values are only sent when set. early_request is
// intentionally never sent: the core defaults it to on, and only an
// explicit false disables it.
fun buildSingBoxOutboundMusessBean(bean: MusessBean): SingBoxOptions.Outbound_MusessOptions {
    return SingBoxOptions.Outbound_MusessOptions().apply {
        type = "musess"
        server = bean.serverAddress
        server_port = bean.serverPort
        server_name = bean.serverName.blankAsNull()
        public_key = bean.publicKey.blankAsNull()
        uuid = bean.uuid.blankAsNull()
        secret = bean.secret.blankAsNull()
        if (bean.mux == true) mux = true
        bean.muxBypass.orEmpty().takeIf { it.isNotEmpty() }?.let { mux_bypass = it }
        if (bean.muxAdaptiveBypass == true) mux_adaptive_bypass = true
        bean.warmPool?.let { warm_pool = it }
        if (bean.deshell == true) deshell = true
        if (bean.fec != null && bean.fec != "off") fec = bean.fec
    }
}
