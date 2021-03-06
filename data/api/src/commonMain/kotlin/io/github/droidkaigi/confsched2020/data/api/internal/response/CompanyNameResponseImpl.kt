package io.github.droidkaigi.confsched2020.data.api.internal.response

import io.github.droidkaigi.confsched2020.data.api.response.CompanyNameResponse
import kotlinx.serialization.Serializable

@Serializable
internal data class CompanyNameResponseImpl(
    override val ja: String,
    override val en: String
) : CompanyNameResponse
