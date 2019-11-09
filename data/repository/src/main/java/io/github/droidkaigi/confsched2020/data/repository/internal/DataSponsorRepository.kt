package io.github.droidkaigi.confsched2020.data.repository.internal

import io.github.droidkaigi.confsched2020.data.api.DroidKaigiApi
import io.github.droidkaigi.confsched2020.data.db.SponsorDatabase
import io.github.droidkaigi.confsched2020.data.db.entity.SponsorEntity
import io.github.droidkaigi.confsched2020.data.repository.SponsorRepository
import io.github.droidkaigi.confsched2020.model.Sponsor
import io.github.droidkaigi.confsched2020.model.SponsorCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataSponsorRepository @Inject constructor(
    private val api: DroidKaigiApi,
    private val sponsorDatabase: SponsorDatabase
) : SponsorRepository {
    override fun sponsors(): Flow<List<SponsorCategory>> {
        return sponsorDatabase
            .sponsors()
            .map {
                it.groupBy { sponsorEntity -> sponsorEntity.categoryIndex }
                    .mapNotNull { (categoryIndex, sponsors) ->
                        val category = SponsorCategory.Category.from(sponsors.first().category)
                            ?: return@mapNotNull null
                        SponsorCategory(
                            category,
                            categoryIndex,
                            sponsors.map(SponsorEntity::toSponsor)
                        )
                    }
            }
    }

    override suspend fun refresh() {
        val response = api.getSponsors()
        sponsorDatabase.save(response)
    }
}

private fun SponsorEntity.toSponsor(): Sponsor = Sponsor(name, url, image)