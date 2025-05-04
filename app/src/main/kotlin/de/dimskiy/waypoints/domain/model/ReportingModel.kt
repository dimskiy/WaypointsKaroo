package de.dimskiy.waypoints.domain.model

sealed class ReportingModel(val key: String, val params: Map<String, String>? = null) {

    data class PerformSearch(val withLocation: Boolean = false) : ReportingModel(
        key = "search_something",
        params = mapOf(
            "search_type" to if (withLocation) "with_gps" else "global"
        )
    )

    data object SearchResultClick : ReportingModel("click_some_result")

    data object SearchItemBookmarked : ReportingModel("bookmark_search_item")

    data object SearchItemDeleted : ReportingModel("delete_search_item")

    data class SearchApiResult(
        val isSuccess: Boolean,
        val errorMessage: String? = null
    ) : ReportingModel(
        key = "search_photon_result",
        params = mutableMapOf<String, String>().apply {
            set("result_type", if (isSuccess) "success" else "error")
            errorMessage?.let {
                set("error_message", it.take(50))
            }
        }
    )
}