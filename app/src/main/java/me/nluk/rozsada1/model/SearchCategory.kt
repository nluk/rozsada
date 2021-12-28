package me.nluk.rozsada1.model

data class SearchCategory(
    val id : Int,
    val title : String
)

data class SearchCategoryData(
    val categories : List<SearchCategory>
)
