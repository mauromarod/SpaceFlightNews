package com.mauromarod.spaceflightnews.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object NewsList : Screen("news_list")
    data object Profile : Screen("profile")
    data object ArticleDetail : Screen("article_detail/{articleId}") {
        const val ARG_ARTICLE_ID = "articleId"
        fun createRoute(id: Int) = "article_detail/$id"
    }
}
