package com.mauromarod.spaceflightnews.core.domain.model

class ArticleNotFoundException(id: Int) : Exception("Article with id $id not found")
